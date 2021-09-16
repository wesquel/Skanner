package sample;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ControllerS extends Main implements Initializable {

    // Variaveis do FXML
    @FXML private TableView<Person> tableViewPerson;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> idColumn;
    @FXML private TableColumn<Person, String> codeColumn;
    @FXML private Button buttonIniciar;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldId;
    @FXML private TextField textFieldCode;
    @FXML private TextField textFieldNameEditPerson;
    @FXML private TextField textFieldIdEditPerson;
    @FXML private TextField textFieldCodeEditPerson;
    @FXML private Button addButton;
    @FXML private Button endButton;
    @FXML private ToggleButton getJsonButton;
    @FXML private ImageView imageViewDocument;
    @FXML private AnchorPane anchorPaneMain;
    @FXML private VBox vBoxPod;
    @FXML private VBox vBoxInitial;
    @FXML private Label labelTotalDocumentosAnalisados;
    @FXML private Label labelTotalDeDocumentos;
    @FXML private ProgressBar progressBarJson;
    @FXML private Hyperlink cancelGenerateJson;
    @FXML private VBox vBoxEditPerson;
    @FXML private String imagenName;
    @FXML private Image imagenAtual;

    // Variaveis
    private int rotateAxi;
    private String[] splited;
    JSONObject jsonObject;
    JSONParser parser = new JSONParser();
    private int localizacao_atual = 0;
    private int totalDoumentosAnalisados = 0;
    private int totalDeDocumentos = 0;
    private volatile Thread jsonGenerateThread;
    private String pathToImage;
    private Boolean ThreadJson = false;
    private Person personEditable;
    ObservableList<Person> items = FXCollections.observableArrayList();

    // Definir Paramentros e Variaveis na inicialização do programa.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Process process;
        nameColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("id"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("code"));
//        tableViewPerson.setRowFactory(tv ->{
//            // Define our new TableRow
//            TableRow<Person> row = new TableRow<>();
//            row.setOnMouseClicked(event -> {
//                System.out.println("Do your stuff here!");
//            });
//            return row;
//        });
        tableViewPerson.setRowFactory(e ->{
            TableRow<Person> row =  new TableRow<Person>();
            row.setOnMouseClicked(c ->{
                if(!row.isEmpty()){
                    showEditPerson(row.getItem());
                }

            });
            return row;
        });
//        nameColumn.setCellFactory(tc -> {
//            TableCell<Person, String> cell = new TableCell<Person, String>() {
//                @Override
//                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty) ;
//                    setText(empty ? null : item);
//                }
//            };
//            cell.setOnMouseClicked(e -> {
//                if (!cell.isEmpty()) {
//                    String userId = cell.getItem();
//                    System.out.println(cell.getItem());
//                    // do something with id...
//                }
//            });
//            return cell ;
//        });
    }

    // Função de ação ao aperta ENTER dentro de um TextField para otimização de tempo do usuario.
    public void keyEnterPressed(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER) {
            if (textFieldCode.isFocused()) {
                textFieldName.requestFocus();
                CheckEmpyField(textFieldCode);
            }
            else if(textFieldName.isFocused()){
                textFieldId.requestFocus();
                CheckEmpyField(textFieldName);
            }else{
                textFieldCode.requestFocus();
                addPerson(new ActionEvent());
            }
        }
    }

    // Checar se o TextInput está vazio.
    public boolean CheckEmpyField(TextField t)    {
        if (t.getText().isEmpty()) {
            t.setStyle("-fx-text-box-border: #B22222; -fx-focus-color: #B22222;");
            t.requestFocus();
            return true;
        }
        t.setStyle("");
        return false;
    }

    // Atualizar quantidade de documentos analisados.
    public void updateDocumentosAnalisados(){
        if (totalDeDocumentos <= totalDoumentosAnalisados){
            return;
        }
        totalDoumentosAnalisados += 1;
        labelTotalDocumentosAnalisados.setText("Documentos analisados: "+totalDoumentosAnalisados);
    }

    // Ação do botão iniciar.
    public void InitialAction(ActionEvent event){
        vBoxInitial.setVisible(false);
        vBoxPod.setVisible(true);
        loadJson();
        totalDeDocumentos = splited.length;
        labelTotalDeDocumentos.setText("Total de Documentos: "+totalDeDocumentos);
        nextImageAndCode();
        event.consume();
    }

    // Ação do Botão Finalizar.
    public void endAction(ActionEvent event){
        if (totalDeDocumentos != totalDoumentosAnalisados){
            return;
        }
        vBoxPod.setVisible(false);
        event.consume();
    }

    // Atualiza para o proximo codigo e imagen do documento.
    public void nextImageAndCode(){
        if (totalDeDocumentos <= totalDoumentosAnalisados){
            imageViewDocument.setImage(null);
            this.imagenAtual = null;
            return;
        }
        String barCode;
        //Salva no objeto JSONObject o que o parse tratou do arquivo
        String[] splitred = splited[localizacao_atual].split(":");
        this.imagenName = splitred[0];
        try {
            barCode = splitred[1];
        }catch (ArrayIndexOutOfBoundsException a){
            barCode = "";
        }
        textFieldCode.setText(barCode);
        Image image = new Image(Objects.requireNonNull(getClass().getResource("imagens/"+imagenName)).toExternalForm());
        resetImageRotate(new ActionEvent());
        imageViewDocument.setImage(image);
        this.imagenAtual = image;
        imageViewDocument.setRotate(rotateAxi);
        localizacao_atual+=1;
    }

    // Função para adicionar o Documento ao Observer List e TableView.
    public void addPerson(ActionEvent event){
        if (totalDeDocumentos <= totalDoumentosAnalisados || CheckEmpyField(textFieldCode) || CheckEmpyField(textFieldName) || CheckEmpyField(textFieldId)){
            return;
        }
        items.add(new Person(textFieldId.getText(),textFieldName.getText(),textFieldCode.getText().toUpperCase(Locale.ROOT),this.imagenName));
        tableViewPerson.setItems(items);
        updateDocumentosAnalisados();
        textFieldCode.clear();
        textFieldId.clear();
        textFieldName.clear();
        nextImageAndCode();
        event.consume();
    }

    public void showEditPerson(Person p){
        vBoxPod.setVisible(false);
        vBoxEditPerson.setVisible(true);
        textFieldCodeEditPerson.setText(p.getCode());
        textFieldNameEditPerson.setText(p.getName());
        textFieldIdEditPerson.setText(p.getId());
        resetImageRotate(new ActionEvent());
        imageViewDocument.setImage(new Image(Objects.requireNonNull(getClass().getResource("imagens/"+p.getImageReference())).toExternalForm()));
        this.personEditable = p;
    }

    public void backToPod(ActionEvent event){
        vBoxEditPerson.setVisible(false);
        vBoxPod.setVisible(true);
        imageViewDocument.setImage(imagenAtual);
        resetImageRotate(new ActionEvent());
        event.consume();
    }

    public void setChangedEditPerson(ActionEvent event){
        personEditable.setCode(textFieldCodeEditPerson.getText());
        personEditable.setId(textFieldIdEditPerson.getText());
        personEditable.setName(textFieldNameEditPerson.getText());
        tableViewPerson.refresh();
        event.consume();
    }

    // Ação do botão Caixa de Mensagens, feito para reduzir o tempo.
    public void addToHouse(ActionEvent event){
        if (CheckEmpyField(textFieldCode) || totalDeDocumentos <= totalDoumentosAnalisados){
            return;
        }
        items.add(new Person("0000","Caixa de Correio",textFieldCode.getText(),this.imagenName));
        tableViewPerson.setItems(items);
        updateDocumentosAnalisados();
        textFieldCode.clear();
        textFieldId.clear();
        textFieldName.clear();
        nextImageAndCode();
        textFieldCode.requestFocus();
        event.consume();
    }

    // Image Propiedades.

    // Reseta rotação da imagem.
    public void resetImageRotate (ActionEvent event){
        rotateAxi = 0;
        imageViewDocument.setRotate(0);
        event.consume();
    }

    // Girar imagem para direita.
    public void rotateRight(ActionEvent event){
        imageViewDocument.setRotate(rotateAxi -= 5);
        event.consume();
    }

    // Girar Imagen para esquerda.
    public void rotateLeft(ActionEvent event){
        imageViewDocument.setRotate(rotateAxi += 5);
        event.consume();
    }

    // Carregar o JSON na memoria com split.
    public void loadJson(){
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(new File("src").getAbsolutePath()+"\\sample\\barCodeRead\\db.json"));
            splited = jsonObject.toJSONString().replace("{","").replace("}","").replace("\"","").split(",");
        }
        //Trata as exceptions que podem ser lançadas no decorrer do processo
        catch (ParseException | IOException e) {
            e.printStackTrace();
        }

    }

    //Gerar Json
    public void killPrompt(){
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, "C:\\Windows\\system32\\cmd.exe"); // window title
        WinDef.HWND hwnd1 = User32.INSTANCE.FindWindow(null,"C:\\Windows\\system32\\cmd.exe - python  read.py db "+this.pathToImage+"/");
        if (hwnd != null) {
            User32.INSTANCE.PostMessage(hwnd, WinUser.WM_CLOSE, null, null);  // can be WM_QUIT in some occasion
        }
        if(hwnd1 != null){
            User32.INSTANCE.PostMessage(hwnd1, WinUser.WM_CLOSE, null, null);
        }
    }

    // Thread to Generate JSON
    public void readPorcentInTxt(){
        try {
            String path = new File("src").getAbsolutePath()+"\\sample\\barCodeRead\\porcentagem.txt";
            FileWriter fw = new FileWriter( path );
            BufferedWriter bw = new BufferedWriter( fw );
            bw.write( "0" );
            bw.close();
            fw.close();
            while (ThreadJson){
                FileInputStream stream = new FileInputStream(path);
                InputStreamReader reader = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(reader);
                String porcentagemTXT = br.readLine();
                if (porcentagemTXT != null && porcentagemTXT.contains("1.0")){
                    progressBarJson.setProgress(1.0);
                    buttonIniciar.setVisible(true);
                    ThreadJson = false;
                    killPrompt();
                }
                else if(porcentagemTXT != null && !porcentagemTXT.isEmpty() && progressBarJson.getProgress() != Double.parseDouble(porcentagemTXT)){
                    progressBarJson.setProgress(Double.parseDouble(porcentagemTXT));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void cancelGenerateAction(ActionEvent event) {
        cancelGenerateJson.setVisible(false);
        buttonIniciar.setVisible(false);
        killPrompt();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ThreadJson = false;
        jsonGenerateThread = null;
        getJsonButton.setSelected(false);
        progressBarJson.setProgress(0.00);
        event.consume();
    }

    //Action on click button Gerar Json.
    public void generateJson(ActionEvent event){
        if (ThreadJson || cancelGenerateJson.isVisible()){
            getJsonButton.setSelected(true);
            return;
        }
        this.pathToImage = selectDirectory();
        String pathLocal = new File("src").getAbsolutePath();
        pathLocal = pathLocal+"\\sample\\barCodeRead\\";
        if (pathToImage == null){
            return;
        }
        cancelGenerateJson.setVisible(true);
        try {
            Runtime.getRuntime().exec("cmd.exe /c cd \"" + pathLocal + "\" & start /min cmd.exe /k \"python read.py db " + pathToImage + "/\"");
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeJsonGenerate();
        event.consume();
    }

    public void initializeJsonGenerate(){
        ThreadJson = true;
        if (jsonGenerateThread != null) {
            jsonGenerateThread = null;
        } else {
            jsonGenerateThread = new Thread(this::readPorcentInTxt);
            jsonGenerateThread.start();
        }
    }


    // Selecionar Diretorio das Imagens.
    public String selectDirectory(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) anchorPaneMain.getScene().getWindow();
        directoryChooser.setInitialDirectory(new File("src"));
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null){
            return selectedDirectory.getAbsolutePath();
        }
        return null;
    }

}

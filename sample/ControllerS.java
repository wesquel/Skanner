package sample;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class ControllerS extends Main implements Initializable {

    // Variaveis do FXML
    @FXML private TableView<Person> tableViewPerson;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> idColumn;
    @FXML private TableColumn<Person, String> codeColumn;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldId;
    @FXML private TextField textFieldCode;
    @FXML private Button addButton;
    @FXML private Button endButton;
    @FXML private ImageView imageViewDocument;
    @FXML private TextField selectJsonTextFiled;
    @FXML private AnchorPane anchorPaneMain;
    @FXML private VBox vBoxPod;
    @FXML private VBox vBoxInitial;
    @FXML private Label labelTotalDocumentosAnalisados;
    @FXML private Label labelTotalDeDocumentos;

    // Variaveis
    private int rotateAxi;
    private String[] splited;
    JSONObject jsonObject;
    JSONParser parser = new JSONParser();
    private int localizacao_atual = 0;
    private int totalDoumentosAnalisados = 0;
    private int totalDeDocumentos = 0;
    ObservableList<Person> items = FXCollections.observableArrayList();

    // Definir Paramentros e Variaveis na inicialização do programa.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
                    items.remove(row.getItem());
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
            }
            else if(textFieldName.isFocused()){
                textFieldId.requestFocus();
            }else{
                addPerson(new ActionEvent());
                textFieldCode.requestFocus();
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
    }

    // Ação do Botão Finalizar.
    public void endAction(ActionEvent event){
        if (totalDeDocumentos != totalDoumentosAnalisados){
            return;
        }
        vBoxPod.setVisible(false);
    }

    // Atualiza para o proximo codigo e imagen do documento.
    public void nextImageAndCode(){
        if (totalDeDocumentos <= totalDoumentosAnalisados){
            return;
        }
        String imageName;
        String barCode;
        //Salva no objeto JSONObject o que o parse tratou do arquivo
        String[] splitred = splited[localizacao_atual].split(":");
        imageName = splitred[0];
        try {
            barCode = splitred[1];
        }catch (ArrayIndexOutOfBoundsException a){
            barCode = "";
        }
        textFieldCode.setText(barCode);
        System.out.println(imageName);
        Image image = new Image(Objects.requireNonNull(getClass().getResource("imagens/"+imageName)).toExternalForm());
        resetImageRotate(new ActionEvent());
        imageViewDocument.setImage(image);
        imageViewDocument.setRotate(rotateAxi);
        localizacao_atual+=1;
    }

    // Função para adicionar o Documento ao Observer List e TableView.
    public void addPerson(ActionEvent event){
        if (CheckEmpyField(textFieldCode) || CheckEmpyField(textFieldName) || CheckEmpyField(textFieldId)){
            return;
        }
        items.add(new Person(textFieldId.getText(),textFieldName.getText(),textFieldCode.getText()));
        tableViewPerson.setItems(items);
        updateDocumentosAnalisados();
        textFieldCode.clear();
        textFieldId.clear();
        textFieldName.clear();
        nextImageAndCode();
    }

    // Ação do botão Caixa de Mensagens, feito para reduzir o tempo.
    public void addToHouse(ActionEvent event){
        if (CheckEmpyField(textFieldCode)){
            return;
        }
        items.add(new Person("0000","Caixa de Correio",textFieldCode.getText()));
        tableViewPerson.setItems(items);
        updateDocumentosAnalisados();
        textFieldCode.clear();
        textFieldId.clear();
        textFieldName.clear();
        nextImageAndCode();
        textFieldCode.requestFocus();
    }

    // Image Propiedades.

    // Reseta rotação da imagem.
    public void resetImageRotate (ActionEvent event){
        rotateAxi = 0;
        imageViewDocument.setRotate(0);
    }

    // Girar imagem para direita.
    public void rotateRight(ActionEvent event){
        imageViewDocument.setRotate(rotateAxi -= 5);
    }

    // Girar Imagen para esquerda.
    public void rotateLeft(ActionEvent event){
        imageViewDocument.setRotate(rotateAxi += 5);
    }

    // Carregar o JSON na memoria com split.
    public void loadJson(){
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader("saida.json"));
            splited = jsonObject.toJSONString().replace("{","").replace("}","").replace("\"","").split(",");
        }
        //Trata as exceptions que podem ser lançadas no decorrer do processo
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Select Files and Directorys

    // Selecionar Diretorio das Imagens.
    public void selectDirectory(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) anchorPaneMain.getScene().getWindow();
        directoryChooser.setInitialDirectory(new File("src"));
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null){
            System.out.println(selectedDirectory.getAbsolutePath());
        }
    }

    // Selecionar o arquivo JSON.
    public void selectJsonFile(ActionEvent event){
        Window window = ((Node) (event.getSource())).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File("src"));
        File file = fileChooser.showOpenDialog(window);
        event.consume();
        if (file != null) {
            selectJsonTextFiled.setText(file.getPath());
        }
    }
}

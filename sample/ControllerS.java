package sample;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

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
    @FXML private TableColumn<Person, String> listaColumn;
    @FXML private TableColumn<Person, String> podColumn;
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
    @FXML private Label labelEditPerson;
    @FXML private VBox vBoxInitial;
    @FXML private Label labelTotalDocumentosAnalisados;
    @FXML private Label labelTotalDeDocumentos;
    @FXML private ProgressBar progressBarJson;
    @FXML private Hyperlink cancelGenerateJson;
    @FXML private VBox vBoxEditPerson;
    @FXML private VBox vBoxLogin;
    @FXML private Button loginButton;
    @FXML private TextField userTextFiel;
    @FXML private PasswordField passwordField;
    @FXML private Label labelLogin;
    @FXML private VBox vBoxUp;
    @FXML private ProgressBar progessBarLista;
    @FXML private ProgressBar progessBarPOD;

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
    private String imagenName;
    private Image imagenAtual;
    private volatile Thread loginThread;
    private volatile Thread podThread;
    private String username;
    private String password;
    private WebDriver driver;
    private VBox lastWindow;
    int contadorPod = 0;
    int contadorLista = 0;
    private File selectedDirectory;

    // Definir Paramentros e Variaveis na inicialização do programa.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("id"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("code"));
        listaColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("lista"));
        podColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("pod"));
        textFieldId.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    textFieldId.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        textFieldIdEditPerson.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    textFieldIdEditPerson.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        tableViewPerson.setRowFactory(e ->{
            TableRow<Person> row =  new TableRow<Person>();
            row.setOnMouseClicked(c ->{
                if(!row.isEmpty()){
                    showEditPerson(row.getItem());
                }

            });
            return row;
        });
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


    // Função caso a tecla pressionada sejar enter no textFieldEdit.
    public void keyEnterPressedInEditPerson(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER) {
            if (textFieldCodeEditPerson.isFocused()) {
                textFieldNameEditPerson.requestFocus();
                CheckEmpyField(textFieldCodeEditPerson);
            }
            else if(textFieldNameEditPerson.isFocused()){
                textFieldIdEditPerson.requestFocus();
                CheckEmpyField(textFieldNameEditPerson);
            }else{
                textFieldCodeEditPerson.requestFocus();
                setChangedEditPerson(new ActionEvent());
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
        lastWindow = vBoxPod;
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
        vBoxLogin.setVisible(true);
        lastWindow = vBoxLogin;
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
        Image image = new Image("file:"+selectedDirectory.getAbsolutePath()+"/"+imagenName);
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
        items.add(new Person(textFieldId.getText(),textFieldName.getText(),textFieldCode.getText().toUpperCase(Locale.ROOT),this.imagenName,"x","x"));
        tableViewPerson.setItems(items);
        updateDocumentosAnalisados();
        textFieldCode.clear();
        textFieldId.clear();
        textFieldName.clear();
        nextImageAndCode();
        event.consume();
    }

    // Exibir tela de edição de documento.
    public void showEditPerson(Person p){
        labelEditPerson.setText("Edição dos dados do documento");
        lastWindow.setVisible(false);
        vBoxEditPerson.setVisible(true);
        textFieldCodeEditPerson.setText(p.getCode());
        textFieldNameEditPerson.setText(p.getName());
        textFieldIdEditPerson.setText(p.getId());
        resetImageRotate(new ActionEvent());
        imageViewDocument.setImage(new Image("file:"+selectedDirectory.getAbsolutePath()+"/"+p.getImageReference()));
        this.personEditable = p;
    }

    // Voltar para tela anterior.
    public void backToWindow(ActionEvent event){
        vBoxEditPerson.setVisible(false);
        lastWindow.setVisible(true);
        imageViewDocument.setImage(imagenAtual);
        resetImageRotate(new ActionEvent());
        event.consume();
    }

    // Atualizando as informações com base nos valores alterados na edição do documento.
    public void setChangedEditPerson(ActionEvent event){
        if(CheckEmpyField(textFieldCodeEditPerson) || CheckEmpyField(textFieldNameEditPerson) || CheckEmpyField(textFieldIdEditPerson)){
            return;
        }
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
        items.add(new Person("0000","Caixa de Correio",textFieldCode.getText(),this.imagenName,"x","x"));
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

    //Fechar o prompt de comando ao finalizar sua atividade.
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

    //Função para setar a janela foco. (usada quando tem algum erro na hora de enviar os documentos).
    public static void setFocusToWindowsApp(String applicationTitle, int windowState) {
        int state = windowState;
        switch (state) {
            default:
            case 0:
                state = User32.SW_SHOWNORMAL;
                break;
            case 1:
                state = User32.SW_SHOWMAXIMIZED;
                break;
            case 2:
                state = User32.SW_SHOWMINIMIZED;
                break;
        }
        User32 user32 = User32.INSTANCE;
        WinDef.HWND hWnd = user32.FindWindow(null, applicationTitle);
        if (user32.IsWindowVisible(hWnd)) {
            if (state != User32.SW_SHOWMINIMIZED) {
                user32.ShowWindow(hWnd, User32.SW_SHOWMINIMIZED);
            }
            user32.ShowWindow(hWnd, state);
            user32.SetFocus(hWnd);
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

    // Ação que cancela a geração dos codigos de barras.
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

    //Action on click button Generate Json.
    public void generateJson(ActionEvent event){
        if (ThreadJson || cancelGenerateJson.isVisible()){
            getJsonButton.setSelected(true);
            return;
        }
        this.pathToImage = selectDirectory();
        if (pathToImage == null){
            return;
        }
        String pathLocal = new File("src").getAbsolutePath();
        pathLocal = pathLocal+"\\sample\\barCodeRead\\";
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

    public void initialLogin(){
        if (loginThread != null) {
            loginThread = null;
        } else {
            loginThread = new Thread(this::loginWeb);
            loginThread.start();
        }
    }

    //Ação realizada ao pressionar o botão de login.
    public void loginAction(ActionEvent event){
        this.username = userTextFiel.getText();
        this.password = passwordField.getText();
        initialLogin();
        event.consume();
    }


    public void loginWeb(){
        //&& contadorLista != items.size()
        if (driver != null){
            return;
        }
        try {
            try {
                Platform.runLater(() -> labelLogin.setText("Capturando Driver."));
                System.setProperty("webdriver.chrome.driver", new File("libs").getAbsolutePath() + "\\driver\\chromedriver.exe");
            }catch (IllegalStateException e){
                return;
            }
            Platform.runLater(() -> labelLogin.setText("Abrindo navegador."));
            this.driver = new ChromeDriver();
            driver.get("https://pegasus.flashpegasus.com.br/");
            Platform.runLater(() -> labelLogin.setText("Conectando-se ao site."));
            driver.switchTo().frame("FrameGeral");
            try {
                WebElement usernameInput = driver.findElement(By.id("username"));
                WebElement passwordInput = driver.findElement(By.id("password"));
                WebElement buttonLogin = driver.findElement(By.className("ui-button"));
                usernameInput.sendKeys(this.username);
                passwordInput.sendKeys(this.password);
                buttonLogin.click();
            }catch (NoSuchElementException element){
                driver.quit();
                Platform.runLater(() -> labelLogin.setText("elementos de login não encontrados."));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                driver.quit();
                e.printStackTrace();
            }
            try {
                driver.findElement(By.className("ui-messages-error-icon"));
                driver.quit();
                driver = null;
                Platform.runLater(() -> labelLogin.setText("Usuário ou senha incorreto."));
            } catch (RuntimeException c) {
                Platform.runLater(() -> labelLogin.setText("Login completo."));
                vBoxLogin.setVisible(false);
                vBoxUp.setVisible(true);
                lastWindow = vBoxUp;
            }
        }catch (NoSuchFrameException ignored){
            Platform.runLater(() -> labelLogin.setText("Erro: Frame não encontrado."));
        }
    }

    public void podWeb(ActionEvent event){
        if (podThread != null) {
            podThread = null;
        } else {
            podThread = new Thread(this::initialPodWeb);
            podThread.start();
        }
        event.consume();
    }

    public void initialPodWeb(){
        Person docAtual;
        if (driver == null){
            return;
        }
        while (contadorPod < items.size()) {
            driver.get("https://pegasus.flashpegasus.com.br/FlashPegasus/pages/baixa/baixarPod.xhtml");
            try {
                docAtual = items.get(contadorPod);
                imageViewDocument.setImage(new Image("file:"+selectedDirectory.getAbsolutePath()+"/"+docAtual.getImageReference()));
                WebElement leitorCodigoDeBarras = driver.findElement(By.id("leituraDocumento"));
                leitorCodigoDeBarras.sendKeys(docAtual.getCode());
                leitorCodigoDeBarras.sendKeys(Keys.ENTER);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    WebElement campoNome = driver.findElement(By.id("dadosHawbForm:campoNome"));
                    WebElement campoRg = driver.findElement(By.id("dadosHawbForm:j_idt300"));
                    WebElement campoHora = driver.findElement(By.id("dadosHawbForm:hora"));
                    WebElement campoData = driver.findElement(By.id("dadosHawbForm:data_input"));
                    WebElement buttonEnviarPOD = driver.findElement(By.id("dadosHawbForm:btnGravar"));
                    campoNome.sendKeys(docAtual.getName());
                    campoRg.sendKeys(docAtual.getId());
                    if (campoHora.getText().equals("") || campoData.getText().equals("")) {
                        erroUploadDocument(docAtual, "ERRO: Campo de Data ou Hora Vazio");
                        return;
                    }
                    //buttonEnviarPOD.click();
                    docAtual.setPod("^");
                    tableViewPerson.refresh();
                    contadorPod+=1;
                    progessBarPOD.setProgress((1.0/totalDeDocumentos)*contadorPod);
                }catch (NoSuchElementException element){
                    erroUploadDocument(docAtual,"ERRO NO DOCUMENTO ATUAL!");
                    return;
                }
            }catch (NoSuchElementException element){
                System.out.println("tentei2");
                return;
            }
        }
    }

    public void erroUploadDocument(Person docAtual, String erroMsg){
        showEditPerson(docAtual);
        Platform.runLater(() -> labelEditPerson.setText(erroMsg));
        setFocusToWindowsApp("Skanner",0);
        podThread = null;
    }

    public void listaWeb(ActionEvent event){
        Person docAtual;
        if (driver == null){
            return;
        }

        while(contadorLista < items.size()){
            driver.get("https://pegasus.flashpegasus.com.br/FlashPegasus/pages/listas/baixarLista.xhtml");
            docAtual = items.get(contadorLista);
        }
    }


    // Selecionar Diretorio das Imagens.
    public String selectDirectory(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) anchorPaneMain.getScene().getWindow();
        directoryChooser.setInitialDirectory(new File("src"));
        this.selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null){
            return selectedDirectory.getAbsolutePath();
        }
        return null;
    }

}

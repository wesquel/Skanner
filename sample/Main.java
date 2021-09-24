package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jnativehook.GlobalScreen;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("sample.fxml")));
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setTitle("Skanner");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        LogManager.getLogManager().reset();
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
    }


    public static void main(String[] args) {
        launch(args);
    }
}

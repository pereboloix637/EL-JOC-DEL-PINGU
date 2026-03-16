package controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage stage;

    public static void cambiarEscena(String fxmlPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
        Parent root = loader.load();
        stage.getScene().setRoot(root);
        if (!stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1920, 1080);

        primaryStage.setTitle("El Joc del Pingüí");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
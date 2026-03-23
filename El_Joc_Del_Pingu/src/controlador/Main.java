package controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
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

        // Usar las dimensiones reales de la pantalla para la escena
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.setFill(Color.BLACK);

        primaryStage.setTitle("El Joc del Pingüí");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
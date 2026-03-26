package controlador;

import controlador.AudioManager;
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
    private static java.util.Map<String, Parent> sceneCache = new java.util.HashMap<>();
    private static boolean fullScreenEnabled = true;

    public static boolean isFullScreenEnabled() {
        return fullScreenEnabled;
    }

    public static void setFullScreenEnabled(boolean enabled) {
        fullScreenEnabled = enabled;
        if (stage != null) {
            stage.setFullScreen(enabled);
            if (!enabled) {
                stage.setMaximized(true);
            }
        }
    }

    public static void preCargarEscena(String fxmlPath) {
        try {
            if (!sceneCache.containsKey(fxmlPath)) {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
                Parent root = loader.load();
                sceneCache.put(fxmlPath, root);
            }
        } catch (Exception e) {
            System.err.println("Error pre-cargando escena: " + fxmlPath + " - " + e.getMessage());
        }
    }

    public static void cambiarEscena(String fxmlPath) throws Exception {
        Parent root;
        if (sceneCache.containsKey(fxmlPath)) {
            root = sceneCache.get(fxmlPath);
        } else {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            root = loader.load();
            sceneCache.put(fxmlPath, root);
        }

        if (stage.getScene() == null) {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            scene.setFill(Color.BLACK);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }

        if (fullScreenEnabled && !stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        
        // Configuración inicial del Stage para que sea rápido
        primaryStage.setTitle("El Joc del Pingüí");
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");

        // Cargar primero la pantalla de carga (Splash)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaCarga.fxml"));
        Parent root = loader.load();
        
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.setFill(Color.BLACK);
        
        primaryStage.setScene(scene);
        primaryStage.show();

        // Inicializar audio de forma asíncrona
        AudioManager.getInstance().initAsync();
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Dar un segundo para que cargue
                AudioManager.getInstance().playMusic();
            } catch (Exception e) {}
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package controlador;

import controlador.AudioManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image; // Importación necesaria
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.scene.CacheHint;

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
        cambiarEscena(fxmlPath, false);
    }

    public static void cambiarEscena(String fxmlPath, boolean forceReload) throws Exception {
        if (forceReload) {
            sceneCache.remove(fxmlPath);
        }

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

    public static void cambiarEscenaConCircleWipe(String fxmlPath, boolean forceReload) throws Exception {
        if (forceReload) {
            sceneCache.remove(fxmlPath);
        }

        Parent root;
        if (sceneCache.containsKey(fxmlPath)) {
            root = sceneCache.get(fxmlPath);
        } else {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            root = loader.load();
            sceneCache.put(fxmlPath, root);
        }

        // Asegurarse de que el root sea visible y no tenga clips previos
        root.setClip(null);
        root.setOpacity(1.0);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth();
        double height = screenBounds.getHeight();

        Circle circle = new Circle();
        circle.setCenterX(width / 2);
        circle.setCenterY(height / 2);
        circle.setRadius(0);

        root.setClip(circle);

        if (stage.getScene() == null) {
            Scene scene = new Scene(root, width, height);
            scene.setFill(Color.BLACK);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }

        if (fullScreenEnabled && !stage.isFullScreen()) {
            stage.setFullScreen(true);
        }

        // Optimización: Activar caché de hardware durante la animación
        root.setCache(true);
        root.setCacheHint(CacheHint.SPEED);

        // Calcular el radio final (para cubrir la pantalla desde el centro)
        double maxRadius = Math.sqrt(Math.pow(width/2, 2) + Math.pow(height/2, 2)) * 1.1;

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(circle.radiusProperty(), 0)),
            new KeyFrame(Duration.seconds(1.2), new KeyValue(circle.radiusProperty(), maxRadius, Interpolator.EASE_OUT))
        );
        
        timeline.setOnFinished(e -> {
            root.setClip(null);
            root.setCache(false);
            root.setCacheHint(CacheHint.DEFAULT);
        });

        // Retraso muy breve para permitir que el hilo de la UI se estabilice tras la carga
        PauseTransition delay = new PauseTransition(Duration.millis(150));
        delay.setOnFinished(e -> timeline.play());
        delay.play();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        
        // --- REGISTRO DE FUENTE LOCAL ---
        try {
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf"), 10);
        } catch (Exception e) {
            System.err.println("No se pudo cargar la fuente local: " + e.getMessage());
        }
        
        // --- CONFIGURACIÓN DEL ICONO ---
        try {
            // Buscamos en la carpeta assets que está en la raíz de resources
            Image icono = new Image(getClass().getResourceAsStream("/assets/LogoJ-delPingu.png"));
            primaryStage.getIcons().add(icono);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la barra de tareas: " + e.getMessage());
        }
        // -------------------------------

        // Configuración inicial del Stage
        primaryStage.setTitle("El Joc del Pingüí");
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");

        // Cargar primero la pantalla de carga (Splash)
        // Nota: He mantenido tu ruta tal cual, asegúrate de que "/resources/..." sea correcta en tu estructura
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
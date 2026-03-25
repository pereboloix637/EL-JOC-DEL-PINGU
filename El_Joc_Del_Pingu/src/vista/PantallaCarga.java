package vista;

import controlador.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class PantallaCarga {

    @FXML private StackPane rootPane;
    @FXML private ImageView logoView;
    @FXML private ProgressBar progressBar;
    @FXML private Label loadingLabel;

    @FXML
    public void initialize() {
        // Iniciar el proceso de carga en un hilo separado
        new Thread(() -> {
            try {
                // Paso 1: Inicializar audio o recursos ligeros
                updateStatus("Inicializando audio...", 0.2);
                Thread.sleep(500); // Pequeña pausa para que se vea la pantalla

                // Paso 2: Pre-cargar PantallaMenu (la más pesada después del Splash)
                updateStatus("Cargando menú principal...", 0.5);
                Main.preCargarEscena("/resources/PantallaMenu.fxml");
                Thread.sleep(300); // Pequeña pausa para que se vea la pantalla
                
                // Paso 3: Terminar carga inicial
                updateStatus("¡Listo!", 1.0);
                Thread.sleep(300);

                // Transición al menú principal
                Platform.runLater(this::transitionToMenu);

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> loadingLabel.setText("Error al cargar: " + e.getMessage()));
            }
        }).start();
    }

    private void updateStatus(String status, double progress) {
        Platform.runLater(() -> {
            loadingLabel.setText(status);
            progressBar.setProgress(progress);
        });
    }

    private void transitionToMenu() {
        FadeTransition ft = new FadeTransition(Duration.millis(500), rootPane);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(event -> {
            try {
                Main.cambiarEscena("/resources/PantallaMenu.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ft.play();
    }
}

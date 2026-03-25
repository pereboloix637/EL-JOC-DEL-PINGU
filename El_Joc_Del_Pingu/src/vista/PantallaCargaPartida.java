package vista;

import controlador.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.util.Random;

public class PantallaCargaPartida {

    @FXML private StackPane rootPane;
    @FXML private ProgressBar progressBar;
    @FXML private Label tipLabel;

    private String[] tips = {
        "Los osos te hacen retroceder 3 casillas. ¡Evítalos a toda costa!",
        "Las focas son hábiles competidoras. ¡No subestimes su estrategia!",
        "El primer pingüino en llegar a la casilla 50 será el ganador de la expedición.",
        "Los trineos son tus mejores amigos: ¡te permiten avanzar mucho más rápido!",
        "¡Cuidado con los agujeros! Si caes en uno, podrías perder el turno o retroceder.",
        "Puedes guardar tu partida en cualquier momento desde el menú superior del juego.",
        "Cada casilla de evento es una sorpresa: ¡algunas te ayudan y otras te retan!",
        "La nieve es traicionera, pero un buen pingüino siempre encuentra el camino."
    };

    @FXML
    public void initialize() {
        // Seleccionar un consejo aleatorio
        Random random = new Random();
        tipLabel.setText(tips[random.nextInt(tips.length)]);

        // Animación de la barra de progreso
        new Thread(() -> {
            try {
                // Simulación de carga fluida
                for (double i = 0; i <= 1.0; i += 0.02) {
                    final double progress = i;
                    Platform.runLater(() -> progressBar.setProgress(progress));
                    Thread.sleep(60); // ~1.5 segundos en total
                }
                Thread.sleep(700); // Pausa final para leer el tip
                
                Platform.runLater(this::transitionToGame);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void transitionToGame() {
        FadeTransition ft = new FadeTransition(Duration.millis(500), rootPane);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(event -> {
            try {
                Main.cambiarEscena("/resources/PantallaJuego.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ft.play();
    }
}

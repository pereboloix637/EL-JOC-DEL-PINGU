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
    		"Las bolas de nieve seria mejor usarlas para batallar que para atacar...",
    		"¿Tienes peces? ¡Quizás querrías guardártelos para alimentar al oso!",
    		"¡Las focas no tienen inventario! Pero te sorprenderá saber como actúan.",
    		"A veces la estrategia gana a la fuerza, y los ítems lo saben perfectamente.",
    		"Quizás te sorprenda saber que existe un Pingu y una Foca oculta...",
    		"El dado lento te puede ayudar a caer en casillas cercanas.",
    		"¡El dado rapido puede ayudarte a recorrer muchas casillas!",
    		"Los trineos te hacen avanzar y los abujeros te hacen retroceder.",
    		"¡Que no te peguen las focas! Puede ser la diferencia entre ganar y perder...",
    		"¡El primero en llegar a la meta gana! Sea un Pingu o una Foca...",
    		"PCI Studios mantiene a flote este juego, desde un barco claro.",
    		"¿Los Pingus y las Focas se pintan? Me gustaria saber si les gustaria tener unas gafas.",
    		"Hay hasta 7 Pingus y 7 Focas, si no me crees prueba a poner hasta 6 jugadores y ten mucha paciencia."
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
                // Forzamos la recarga para evitar pantallas negras y asegurar inicialización limpia
                Main.cambiarEscenaConCircleWipe("/resources/PantallaJuego.fxml", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ft.play();
    }
}

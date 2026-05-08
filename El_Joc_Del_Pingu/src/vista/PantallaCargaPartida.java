package vista;

import controlador.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Random;

public class PantallaCargaPartida {

    @FXML private StackPane rootPane;
    @FXML private ProgressBar progressBar;
    @FXML private Label tipLabel;

    private String[] tips = {
    		"Las bolas de nieve seria mejor usarlas para batallar que para atacar...",
    		"¿Tienes peces? ¡Quizás querrías guardártelos para alimentar al oso!",
    		"¡Las focas, con su inventario y sus diferentes ataques pueden hacerte de tu recorrido una pesadilla.",
    		"A veces la estrategia gana a la fuerza, y los ítems lo saben perfectamente.",
    		"Quizás te sorprenda saber que existe un Pingu y una Foca oculta...",
    		"El dado lento te puede ayudar a caer en casillas cercanas.",
    		"¡El dado rapido puede ayudarte a recorrer muchas casillas!",
    		"Los trineos te hacen avanzar y los abujeros te hacen retroceder.",
    		"¡Que no te peguen las focas! Puede ser la diferencia entre ganar y perder...",
    		"¡El primero en llegar a la meta gana! Sea un Pingu o una Foca...",
    		"PCI Studios mantiene a flote este juego, desde un barco claro.",
    		"¿Los Pingus y las Focas se pintan? Me gustaria saber si les gustaria tener unas gafas.",
    		"Con capacidad para hasta 6 jugadores, dispones de un total de 12 figuras (6 Pingus y 6 Focas)",
    		"El oso polar normalmente atacara a los pingüinos, pero es probable que a las focas tambien sean afectadas",
    		"Las bolas de nieve hacen retrodecer al mas cercano",
    		"PCI Studios no se responsabiliza de los conflictos o desacuerdos que surjan durante el transcurso del juego.",
    		"Si te toca una Moto de nieve, avanzaras hasta el proximo trineo. ¡Que bien!",
    		"¿Sabias que en los eventos puedes perder turnos y items? Uy que mal rollo..."
    };

    @FXML
    public void initialize() {
        // Seleccionar un consejo aleatorio
        Random random = new Random();
        tipLabel.setText(tips[random.nextInt(tips.length)]);

        // Animación de la barra de progreso
        Timeline timeline = new Timeline();
        int steps = 50; // 1.0 / 0.02
        for (int step = 0; step <= steps; step++) {
            final double progress = step * 0.02;
            timeline.getKeyFrames().add(
                new KeyFrame(javafx.util.Duration.millis(step * 60), e -> progressBar.setProgress(progress))
            );
        }
        // Pausa final de 700ms para leer el tip
        timeline.getKeyFrames().add(
            new KeyFrame(javafx.util.Duration.millis(steps * 60 + 700), e -> transitionToGame())
        );
        timeline.play();
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

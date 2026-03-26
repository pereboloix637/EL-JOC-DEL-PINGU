package vista;

import controlador.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.net.URL;

public class PantallaCarga {

    @FXML private StackPane rootPane;
    @FXML private ImageView logoView;
    @FXML private ProgressBar progressBar;
    @FXML private Label loadingLabel;
    @FXML private MediaView mediaView;

    private MediaPlayer mediaPlayer;
    private static final int INTRO_DURATION_SECONDS = 5;

    private boolean isReady = false;

    @FXML
    public void initialize() {
        try {
            // Asegurar que el rootPane pueda recibir foco para eventos de teclado
            rootPane.setFocusTraversable(true);
            Platform.runLater(rootPane::requestFocus);

            // Cargar el vídeo de la intro
            URL videoUrl = getClass().getResource("/assets/INTRO.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);
                mediaView.setPreserveRatio(true);

                // IMPORTANTE: Esperar a que el medio esté listo antes de reproducir
                mediaPlayer.setOnReady(() -> {
                    mediaPlayer.play();
                    setupProgressBarSync();
                });

                // Al llegar al final, pausamos en el último frame
                mediaPlayer.setOnEndOfMedia(() -> {
                    // Reproducir el vídeo en bucle infinito
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.play();
                        readyToStart();
                    });
                
                loadingLabel.setText("Iniciando aventura...");
            } else {
                fallbackToNormalLoad();
            }

            // Escuchar clics o teclas para entrar al juego
            rootPane.setOnKeyPressed(event -> handleUserInput());
            rootPane.setOnMouseClicked(event -> handleUserInput());

            // Pre-carga
            new Thread(() -> {
                try {
                    Main.preCargarEscena("/resources/PantallaMenu.fxml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            fallbackToNormalLoad();
        }
    }

    private void setupProgressBarSync() {
        // Usar un contador de tiempo independiente para asegurar la duración exacta definida
        final double duration = INTRO_DURATION_SECONDS;
        final long startTime = System.currentTimeMillis();

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(100), event -> {
                long elapsed = System.currentTimeMillis() - startTime;
                double progress = elapsed / (duration * 1000.0);
                
                if (!isReady) {
                    progressBar.setProgress(Math.min(progress, 1.0));
                    int percent = (int) (Math.min(progress, 1.0) * 100);
                    
                    String baseText = "Cargando...";
                    if (progress < 0.3) baseText = "Cargando glaciar...";
                    else if (progress < 0.6) baseText = "Despertando pingüinos...";
                    else if (progress < 0.9) baseText = "Preparando suministros...";
                    else baseText = "¡Todo listo!";
                    
                    loadingLabel.setText(baseText + " (" + percent + "%)");
                }

                if (progress >= 1.0 && !isReady) {
                    readyToStart();
                }
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void readyToStart() {
        isReady = true;
        Platform.runLater(() -> {
            loadingLabel.setText("PULSA CUALQUIER TECLA PARA EMPEZAR");
            // Animación de pulso para el texto
            FadeTransition pulse = new FadeTransition(Duration.seconds(0.8), loadingLabel);
            pulse.setFromValue(1.0);
            pulse.setToValue(0.3);
            pulse.setCycleCount(FadeTransition.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
        });
    }

    private void handleUserInput() {
        if (isReady) {
            transitionToMenu();
        }
    }

    private void fallbackToNormalLoad() {
        progressBar.setProgress(0);
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i++) {
                    final double p = i / 100.0;
                    Platform.runLater(() -> progressBar.setProgress(p));
                    Thread.sleep(30); 
                }
                readyToStart();
            } catch (Exception e) {}
        }).start();
    }

    private void transitionToMenu() {
        isReady = false; // Evitar múltiples triggers
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        
        Platform.runLater(() -> {
            FadeTransition ft = new FadeTransition(Duration.millis(100), rootPane);
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
        });
    }
}

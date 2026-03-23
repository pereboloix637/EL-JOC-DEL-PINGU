package controlador;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URL;

/**
 * Gestor de audio para la música de fondo del juego.
 * Implementa el patrón Singleton para asegurar que la música sea continua entre pantallas.
 */
public class AudioManager {
    private static AudioManager instance;
    private MediaPlayer mediaPlayer;
    private boolean muted = false;

    private AudioManager() {
        try {
            URL resource = getClass().getResource("/assets/soundtrack_menu_pingu.mp3");
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(0.5); // Volumen inicial al 50%
            } else {
                System.err.println("No se ha encontrado el archivo de música: soundtrack_menu_pingu.mp3");
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar el reproductor de audio: " + e.getMessage());
        }
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void playMusic() {
        if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            mediaPlayer.play();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void toggleMute() {
        muted = !muted;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(muted);
        }
    }

    public boolean isMuted() {
        return muted;
    }
    
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(muted);
        }
    }
}

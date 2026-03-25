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
    private boolean musicMuted = false;
    private boolean sfxMuted = false;
    private double musicVolume = 0.5;
    private double sfxVolume = 0.5;

    private AudioManager() {
        try {
            URL resource = getClass().getResource("/assets/soundtrack_menu_pingu.mp3");
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(musicVolume); 
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

    public void toggleMusicMute() {
        musicMuted = !musicMuted;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(musicMuted);
        }
    }

    public void toggleSfxMute() {
        sfxMuted = !sfxMuted;
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }
    
    public boolean isSfxMuted() {
        return sfxMuted;
    }
    
    public void setMusicMuted(boolean muted) {
        this.musicMuted = muted;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(muted);
        }
    }

    public void setSfxMuted(boolean muted) {
        this.sfxMuted = muted;
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(double volume) {
        this.musicVolume = volume;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = volume;
    }

    /**
     * Reproduce un efecto de sonido corto una sola vez.
     * @param resourcePath Ruta al recurso de audio (ej: "/assets/hover.mp3")
     */
    public void playSound(String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                MediaPlayer sfxPlayer = new MediaPlayer(media);
                sfxPlayer.setMute(sfxMuted);
                sfxPlayer.setVolume(sfxVolume);
                sfxPlayer.play();
                
                // Limpieza automática cuando termina el sonido
                sfxPlayer.setOnEndOfMedia(() -> {
                    sfxPlayer.dispose();
                });
            } else {
                System.err.println("No se ha encontrado el efecto de sonido: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error al reproducir efecto de sonido: " + e.getMessage());
        }
    }
}

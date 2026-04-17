package vista;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Random;

/**
 * Efecto visual de nieve cayendo usando un Canvas ligero.
 * Los copos son cuadraditos (pixel-art) blancos/cian que caen
 * con un leve movimiento horizontal sinusoidal.
 */
public class EfectoNieve {

    private static final int NUM_COPOS = 120;
    private static final Random RNG = new Random();

    private final Canvas canvas;
    private final ArrayList<Copo> copos = new ArrayList<>();
    private AnimationTimer timer;

    /**
     * Crea el efecto de nieve y lo añade al Pane contenedor.
     * El canvas se redimensiona automáticamente con el padre.
     *
     * @param parent Pane sobre el cual superponer la nieve
     */
    public EfectoNieve(Pane parent) {
        canvas = new Canvas();
        canvas.setMouseTransparent(true);   // No bloquea clics
        canvas.setOpacity(0.85);

        // Hacer que el canvas llene todo el padre
        canvas.widthProperty().bind(parent.widthProperty());
        canvas.heightProperty().bind(parent.heightProperty());

        parent.getChildren().add(canvas);

        // Generar copos iniciales distribuidos por toda la pantalla
        for (int i = 0; i < NUM_COPOS; i++) {
            copos.add(crearCopo(true));
        }

        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                // Limitar a ~60 fps
                if (now - lastUpdate < 16_000_000L) {
                    return;
                }
                lastUpdate = now;
                actualizar();
                dibujar();
            }
        };
        timer.start();
    }

    /**
     * Detiene la animación y limpia recursos.
     */
    public void detener() {
        if (timer != null) {
            timer.stop();
        }
    }

    // ─── Modelo de un copo de nieve ───────────────────────────────
    private static class Copo {
        double x, y;
        double velocidadY;      // Velocidad vertical (pixels/frame)
        double amplitudX;       // Amplitud del vaivén horizontal
        double frecuenciaX;     // Frecuencia del vaivén
        double fase;            // Fase inicial del seno
        double tamaño;          // Tamaño del cuadradito (pixel-art)
        double opacidad;
        Color color;
    }

    private Copo crearCopo(boolean distribuido) {
        Copo c = new Copo();
        double w = canvas.getWidth() > 0 ? canvas.getWidth() : 1920;
        double h = canvas.getHeight() > 0 ? canvas.getHeight() : 1080;

        c.x = RNG.nextDouble() * w;
        c.y = distribuido ? RNG.nextDouble() * h : -RNG.nextDouble() * 40;
        c.velocidadY = 0.5 + RNG.nextDouble() * 1.8;
        c.amplitudX = 0.3 + RNG.nextDouble() * 0.8;
        c.frecuenciaX = 0.005 + RNG.nextDouble() * 0.015;
        c.fase = RNG.nextDouble() * Math.PI * 2;
        c.tamaño = 2 + RNG.nextInt(4);          // 2-5 px (pixel-art)
        c.opacidad = 0.3 + RNG.nextDouble() * 0.6;

        // Variación de color: blanco puro o tonos cian helado
        double tipo = RNG.nextDouble();
        if (tipo < 0.6) {
            c.color = Color.web("#FFFFFF");       // Blanco nieve
        } else if (tipo < 0.85) {
            c.color = Color.web("#C8E6F0");       // Cian pálido
        } else {
            c.color = Color.web("#7FD4F0");       // Cian helado
        }

        return c;
    }

    private void actualizar() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        for (int i = 0; i < copos.size(); i++) {
            Copo c = copos.get(i);
            c.y += c.velocidadY;
            c.x += Math.sin(c.y * c.frecuenciaX + c.fase) * c.amplitudX;

            // Reciclar copo si sale por abajo o por los lados
            if (c.y > h + 10 || c.x < -10 || c.x > w + 10) {
                copos.set(i, crearCopo(false));
            }
        }
    }

    private void dibujar() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w == 0 || h == 0) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        for (Copo c : copos) {
            gc.setGlobalAlpha(c.opacidad);
            gc.setFill(c.color);
            // Cuadraditos sin redondear = estilo pixel-art
            gc.fillRect(
                Math.floor(c.x),
                Math.floor(c.y),
                c.tamaño,
                c.tamaño
            );
        }

        gc.setGlobalAlpha(1.0);
    }
}

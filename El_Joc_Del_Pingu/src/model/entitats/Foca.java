package model.entitats;

import model.items.Peix;
import model.items.BolaNeu;
import model.items.Dau;
import model.caselles.Casella;
import model.caselles.Forat;
import model.core.Partida;

public class Foca extends Jugador {

    /// ATRIBUTOS
	private boolean soborno;
    private int bloqueix;

    /// CONSTRUCTOR
	// CONSTRUCTOR CON TODOS LOS PARÁMETROS
	public Foca(String nickname, String color, boolean soborno, int bloqueix) {
        super(nickname, color); // LLAMADA OBLIGATORIA
        this.soborno = soborno;
        this.bloqueix = bloqueix;
    }

    // CONSTRUCTOR PARA PONER A LA FOCA SIN SOBORNOS NI BLOQUEOS
    public Foca(String nickname, String color) {
        super(nickname, color); // LLAMADA OBLIGATORIA
        this.soborno = false;
        this.bloqueix = 0;
    }

    /// GETTERS Y SETTERS
	public boolean isSoborno() {
        return soborno;
    }

    public void setSoborno(boolean soborno) {
        this.soborno = soborno;
    }

    public int getBloqueix() {
        return bloqueix;
    }

    public void setBloqueix(int bloqueix) {
        this.bloqueix = bloqueix;
    }

    /// MÉTODOS
	public void aplastarPingu(Pinguino p) { // La foca aplasta al pingüino que toque
        // Si NO ha sido sobornada y NO está bloqueada, ataca
        if (!this.soborno && this.bloqueix == 0) {
            int deleteBoles = p.getInventari().getBoles() / 2;
            int deletePeixos = p.getInventari().getPeixos() / 2;
            int deleteDaus = p.getInventari().getDausEspecials() / 2;
            
            System.out.println("¡A " + p.getNickname() + " le van a dar un buen repaso y va a perder la mitad de su inventario!");

            if (deleteBoles > 0) p.getInventari().retirarQuantitat(BolaNeu.class, deleteBoles);
            if (deletePeixos > 0) p.getInventari().retirarQuantitat(Peix.class, deletePeixos);
            if (deleteDaus > 0) p.getInventari().retirarQuantitat(model.items.Dau.class, deleteDaus);

            vista.PantallaJuego.registrarEventoEstatico("¡" + p.getNickname() + " ha sido aplastado por la foca y ha perdido la mitad de sus ítems!", "log-warning");
            System.out.println("=====================================================");
        }
    }

    public void pegarPingu(Pinguino jugador, Partida partida) { // La foca atacará al pingüino que toque
        // Si ha sido sobornada o está bloqueada, no ataca
        if (this.soborno || this.bloqueix > 0) {
            System.out.println("La foca " + this.getNickname() + " está tranquila o bloqueada.");
            return;
        }

        // Si no está tranquila, ataca directamente
        aplicarPegarPingu(jugador, partida);
    }

    private void aplicarPegarPingu(Pinguino jugador, Partida partida) {
        // Si no tiene peces, la foca le pega y lo manda a un agujero anterior.
        int posActual = jugador.getPosicio();
        int foratAnterior = -1;
        Casella casellaDestino = null;

        for (Casella casella : partida.getTaulell().getCaselles()) {
            if (casella instanceof model.caselles.Forat && casella.getPosicio() < posActual) {
                if (casella.getPosicio() > foratAnterior) {
                    foratAnterior = casella.getPosicio();
                    casellaDestino = casella;
                }
            }
        }

        if (casellaDestino != null) {
            jugador.setPosicio(casellaDestino.getPosicio()); // Movemos al jugador a su posición absoluta
            System.out.println("El jugador no tenía peces, ha sido enviado al agujero anterior.");
            vista.PantallaJuego.registrarEventoEstatico("¡" + jugador.getNickname() + " ha sido golpeado y enviado al agujero anterior!", "log-warning");
        } else {
            System.out.println("El jugador no tenía peces, pero no hay ningún agujero anterior.");
            vista.PantallaJuego.registrarEventoEstatico("¡La foca ha golpeado a " + jugador.getNickname() + ", pero no hay agujeros donde caer!", "log-info");
        }
    }

    public void sobornarFoca(Pinguino p) { // Permite sobornar a la foca
        // Si ha sido alimentada recientemente, no volvemos a preguntar
        if (this.bloqueix > 0 || this.soborno) {
            return;
        }

        if (p.getInventari().getPeixos() >= 1) {
            // Preguntamos al usuario si quiere usar el pez para sobornar/alimentar
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            vista.PantallaJuego.estilarAlerta(alert); // Aplicamos el estilo polar
            alert.setTitle("¡Encuentro con la foca!");
            alert.setHeaderText("¡Una foca te corta el paso!");
            alert.setContentText("Tienes un pez en el inventario. ¿Quieres usarlo para alimentar a la foca?");

            javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Sí, alimentar (2 turnos)");
            javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No, arriesgarse");
            alert.getButtonTypes().setAll(btnYes, btnNo);

            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == btnYes) {
                System.out.println(p.getNickname() + " ha alimentado a la foca " + this.getNickname());
                this.soborno = true;
                this.bloqueix = 2; // Queda bloqueada por 2 turnos
                p.getInventari().eliminarItemsPerTipus(Peix.class); // Se le resta un peix
            }
        } else {
            System.out.println("No tienes peces para alimentar a la foca.");
        }
    }

    // Mediante probabilidades y de tus acciones la foca querrá aplastarte o pegarte
    public void AccionesFoca(Pinguino p, Partida partida, Runnable onComplete) {
        if (this.soborno || this.bloqueix > 0) {
            vista.PantallaJuego.registrarEventoEstatico("La foca " + this.getNickname() + " está tranquila o bloqueada y no atacará.", "log-info");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        int totalItems = p.getInventari().getBoles() + p.getInventari().getPeixos() + p.getInventari().getDausEspecials();

        java.util.Random rand = new java.util.Random();

        int chancePegar = 50;
        int chanceAplastar = 50;

        // Determinar probabilidades según el contexto
        if (p.getPosicio() >= 40) {
            chancePegar = 10;
            chanceAplastar = 25;
        } else if (totalItems > 3) {
            chancePegar = 25;
            chanceAplastar = 75;
        }

        // Si las probabilidades son iguales (50/50), activamos la Ruleta Malvada
        if (chancePegar == 50 && chanceAplastar == 50) {
            int actionIndex = rand.nextInt(2); // 0 = Pegar, 1 = Aplastar
            vista.PantallaJuego.mostrarRuletaMalvadaEstatico(p, actionIndex, () -> {
                if (actionIndex == 0) {
                    pegarPingu(p, partida);
                } else {
                    aplastarPingu(p);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        } else {
            // Se hace la acción directamente con un mensaje siguiendo las probabilidades
            int roll = rand.nextInt(100);
            if (roll < chanceAplastar) {
                if (totalItems > 3) {
                    vista.PantallaJuego.registrarEventoEstatico("Foca: \"Veo que tienes muchas cosas... ¡Diles adios!\"", "log-warning");
                } else {
                    vista.PantallaJuego.registrarEventoEstatico("¡La foca ha decidido APLASTAR a " + p.getNickname() + "!", "log-warning");
                }
                aplastarPingu(p);
            } else {
                if (p.getPosicio() >= 40) {
                    vista.PantallaJuego.registrarEventoEstatico("Foca: \"Ja! Creiste que ibas a ganar. ¡pues no mientras este aqui!\"", "log-warning");
                } else {
                    vista.PantallaJuego.registrarEventoEstatico("¡La foca ha decidido PEGAR a " + p.getNickname() + "!", "log-warning");
                }
                pegarPingu(p, partida);
            }
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * Lógica de reacción cuando la foca cae en una casilla de Oso. Tiene un 50%
     * de probabilidades de escapar o volver al inicio.
     */
    public void reaccionarAOs(Partida partida) {
        java.util.Random rand = new java.util.Random();
        if (rand.nextInt(100) < 50) {
            // Escapa (50%)
            System.out.println("La foca " + this.getNickname() + " ha esquivado el ataque del oso!");
            vista.PantallaJuego.registrarEventoEstatico("La foca " + this.getNickname() + " ha conseguido escapar del oso!", "log-info");
        } else {
            // No escapa (50%)
            this.setPosicio(0);
            System.out.println("La foca " + this.getNickname() + " ha sido cazada por el oso y vuelve al inicio.");
            vista.PantallaJuego.registrarEventoEstatico("La foca " + this.getNickname() + " ha sido atacada por un oso y vuelve al inicio!", "log-warning");
        }
    }

}

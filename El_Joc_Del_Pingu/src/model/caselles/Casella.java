package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

public abstract class Casella {

    // Atributs
    private int posicio;

    // Constructor
    public Casella(int posicio) {
        this.posicio = posicio;
    }

    // Getters
    public int getPosicio() {
        return posicio;
    }

    // Setters
    public void setPosicio(int posicio) {
        this.posicio = posicio;
    }

    // Mètode abstracte
    public abstract void realitzarAccio(Partida partida, Jugador jugador);
}

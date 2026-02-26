package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

/** Casella abstracta del taulell. Cada subclasse defineix el seu efecte. */
public abstract class Casella {

    // Atributs
    private int posicio;

    // Constructor
    public Casella(int posicio) {
        this.posicio = posicio;
    }

    // Getters i Setters
    public int  getPosicio() { return posicio; }
    public void setPosicio(int posicio) { this.posicio = posicio; }

    // Mètode abstracte – cada subclasse implementa la seva acció
    public abstract void realitzarAccio(Partida partida, Jugador jugador);
}

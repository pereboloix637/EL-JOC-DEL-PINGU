package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

/** Casella Trencadís: el terra es trenca i el jugador torna a l'inici. */
public class Trencadis extends Casella {

    // Constructor
    public Trencadis(int posicio) {
        super(posicio);
    }

    // El jugador torna directament a la posició 0
    @Override
    public void realitzarAccio(Partida partida, Jugador jugador) {
        jugador.setPosicio(0);
        System.out.println(jugador.getNickname()
                + " ha trepitjat un sòl trencat! Torna a l'inici.");
    }
}
package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

public class Trencadis extends Casella {

    public Trencadis(int posicio) {
        super(posicio);
    }

    @Override
    public void realitzarAccio(Partida partida, Jugador jugador) {
        jugador.setPosicio(0);
        System.out.println(jugador.getNickname() + " ha trepitjat un sòl trencat! Torna a l'inici.");
    }
}
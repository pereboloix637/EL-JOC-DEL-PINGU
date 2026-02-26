package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

/** Casella Trineu: el jugador avança fins al següent trineu del taulell. */
public class Trineu extends Casella {

    // Constructor
    public Trineu(int posicio) {
        super(posicio);
    }

    // Cerca el trineu més proper per davant i hi mou el jugador
    @Override
    public void realitzarAccio(Partida partida, Jugador jugador) {
        int posActual     = jugador.getPosicio();
        int seguentTrineu = -1;

        for (Casella casella : partida.getTaulell().getCaselles()) {
            if (casella instanceof Trineu && casella.getPosicio() > posActual) {
                if (seguentTrineu == -1 || casella.getPosicio() < seguentTrineu) {
                    seguentTrineu = casella.getPosicio();
                }
            }
        }

        if (seguentTrineu != -1) {
            jugador.setPosicio(seguentTrineu);
            System.out.println(jugador.getNickname()
                    + " ha agafat un trineu! Avança a la posició " + seguentTrineu + ".");
        } else {
            System.out.println(jugador.getNickname()
                    + " ha agafat un trineu però no n'hi ha cap més endavant.");
        }
    }
}

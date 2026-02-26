package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

public class Trineu extends Casella {

    public Trineu(int posicio) {
        super(posicio);
    }

    @Override
    public void realitzarAccio(Partida partida, Jugador jugador) {
        int posActual = jugador.getPosicio();
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
            System.out.println(jugador.getNickname() + " ha agafat un trineu! Avança a la posició " + seguentTrineu);
        } else {
            System.out.println(jugador.getNickname() + " ha agafat un trineu però no n'hi ha cap més endavant.");
        }
    }
}

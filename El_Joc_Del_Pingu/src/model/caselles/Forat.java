package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

/** Casella Forat: envia el jugador al forat anterior, o a l'inici si no n'hi ha cap. */
public class Forat extends Casella {

    // Constructor
    public Forat(int posicio) {
        super(posicio);
    }

    // Cerca el forat més proper per sota i hi mou el jugador
    @Override
    public void realitzarAccio(Partida partida, Jugador jugador) {
        int posActual     = jugador.getPosicio();
        int foratAnterior = -1;

        for (Casella casella : partida.getTaulell().getCaselles()) {
            if (casella instanceof Forat && casella.getPosicio() < posActual) {
                if (casella.getPosicio() > foratAnterior) {
                    foratAnterior = casella.getPosicio();
                }
            }
        }

        if (foratAnterior != -1) {
            jugador.setPosicio(foratAnterior);
            System.out.println(jugador.getNickname()
                    + " ha caigut en un forat! Va a la posició " + foratAnterior + ".");
        } else {
            jugador.setPosicio(0);
            System.out.println(jugador.getNickname()
                    + " ha caigut en un forat! No hi ha forat anterior, torna a l'inici.");
        }
    }
}

package controlador;

import java.util.ArrayList;
import model.caselles.Casella;
import model.core.Partida;
import model.core.Taulell;
import model.entitats.Jugador;
import model.entitats.Pingui;

public class GestorTaulell {

	/**
	 * Executa l'acció de la casella sobre el pingüí.
	 */
	public void executarCasella(Partida partida, Pingui pingui, Casella casella) {
		casella.realitzarAccio(partida, pingui);
	}

	/**
	 * Comprova si la partida ha finalitzat i estableix el guanyador.
	 */
	public void comprovarFiTorn(Partida partida) {
		Jugador jugadorActual = partida.getJugadorActual();
		Taulell taulell = partida.getTaulell();

		int posicio = jugadorActual.getPosicio();
		int totalCaselles = taulell.getCaselles().size();

		if (posicio >= totalCaselles - 1) {
			partida.setFinalitzada(true);
			partida.setGuanyador(jugadorActual);
		}
	}
}

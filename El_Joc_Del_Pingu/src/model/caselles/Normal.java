package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

/** Casella Normal: no té cap efecte especial. */
public class Normal extends Casella {

	// Constructor
	public Normal(int posicio) {
		super(posicio);
	}

	// La casella normal no fa res
	@Override
	public void realitzarAccio(Partida partida, Jugador jugador) {
		// Cap efecte addicional
		System.out.println(jugador.getNickname() + " ha caigut en una casella normal.");
	}
}

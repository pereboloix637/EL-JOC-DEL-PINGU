package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;

/** Casella Trineu: el jugador avança fins al següent trineu del taulell. */
public class Trineu extends Casella {

	// Constructor
	public Trineu(int posicio) {
		super(posicio);
	}

	/**
	 * Cerca el trineu més proper per davant de la posició actual del jugador i hi
	 * mou el jugador si en troba un.
	 *
	 * @param partida la partida actual, d'on s'obté el taulell i les caselles
	 * @param jugador el jugador que ha activat l'acció
	 */
	@Override
	public void realitzarAccio(Partida partida, Jugador jugador) {
		int posActual = jugador.getPosicio(); // Posició actual del jugador
		int seguentTrineu = -1; // -1 indica que encara no s'ha trobat cap trineu

		// Recorrem totes les caselles del taulell per trobar el trineu més proper
		for (Casella casella : partida.getTaulell().getCaselles()) {

			// Només ens interessen caselles de tipus Trineu que estiguin per davant
			if (casella instanceof Trineu && casella.getPosicio() > posActual) {

				// Actualitzem el candidat si és el primer trineu trobat
				// o si és més proper que l'anterior candidat
				if (seguentTrineu == -1 || casella.getPosicio() < seguentTrineu) {
					seguentTrineu = casella.getPosicio();
				}
			}
		}

		// Si s'ha trobat algun trineu per davant, hi movem el jugador
		if (seguentTrineu != -1) {
			jugador.setPosicio(seguentTrineu);
			System.out.println(
					jugador.getNickname() + " ha agafat un trineu! Avança a la posició " + seguentTrineu + ".");
		} else {
			// No hi ha cap trineu per davant: el jugador es queda on és
			System.out.println(jugador.getNickname() + " ha agafat un trineu però no n'hi ha cap més endavant.");
		}
	}
}
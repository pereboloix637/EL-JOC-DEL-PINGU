package model.entitats;

import model.items.Inventari;

public class Pingui extends Jugador {
/// ATRIBUTS
private Inventari inventari;
// Aixo es una expansio de Jugador

/// CONSTRUCTOR
public Pingui(String nickname, String color, Inventari inventari) {
	super(nickname, color);   // OBLIGATORIO
	this.inventari = inventari;
	}

/// GETTERS
public Inventari getInventari() {
	return inventari;
	}

/// METODES
// PROXIMAMENT

}

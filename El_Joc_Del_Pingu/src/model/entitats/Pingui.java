package model.entitats;

import model.items.Inventari;
import model.items.Item;

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
// METODE PER BATALLAR AMB UN RIVAL
public void gestionarBatalla (Pingui pingu) {
int num = (int)(Math.random() * 2) + 1;
if (pingu == null) { // O que no estigui en la BD -> = SE NECESITA FER =
System.out.println("ERROR: PINGU INVALID");	
} else if (num == 1) { // Si el random favoreix al Pingu que ataca
System.out.println(this.getNickname() + " guanya!");
} else if (num == 2) { // Sino, si favoreix al atacat
System.out.println(pingu.getNickname() + " guanya!");
		}
	}
public void usarItem (Item i) {
// PER FER
}

public void agregarItem (Item i) {
// PER FER
}

public void trureItem (Item i) {
// PER FER
}


}

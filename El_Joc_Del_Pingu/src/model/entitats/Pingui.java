package model.entitats;

import model.items.Inventari;
import model.items.Item;
import model.items.BolaNeu;


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
// CONTADORS PER AL BUCLE
int contJ1 = this.getInventari().getBoles();
int contJ2 = pingu.getInventari().getBoles();
	
// CONDICIONS
if (pingu == null || this.getInventari().getBoles() < 0 || pingu.getInventari().getBoles() < 0) { // O que no estigui en la BD -> = SE NECESITA FER =
System.out.println("ERROR: PINGU INVALID");

} else if (this.getInventari().getBoles() > pingu.getInventari().getBoles()) { // Si el random favoreix al Pingu que ataca
System.out.println(this.getNickname() + " guanya!");

// OPERACIO PER A SUMAR CASELLES I LA DIFERENCIA DE BOLES
this.posicio = posicio + (this.getInventari().getBoles() - pingu.getInventari().getBoles());

} else if (this.getInventari().getBoles() < pingu.getInventari().getBoles()) { // Sino, si favoreix al atacat
System.out.println(pingu.getNickname() + " guanya!");

//OPERACIO PER A SUMAR CASELLES I LA DIFERENCIA DE BOLES
pingu.posicio = posicio + (this.getInventari().getBoles() - pingu.getInventari().getBoles());

} else if (this.getInventari().getBoles() == pingu.getInventari().getBoles()) { // Peró, si empaten, els restara a ambos les Boles de Neu

// BUCLES PER ELIMINAR BOLES DE NEU
for (int i = 0; i > contJ1; i++) {	
this.getInventari().eliminarItem(BolaNeu);
	}
for (int i = 0; i > contJ2; i++) {	
pingu.getInventari().eliminarItem(BolaNeu);
	}
}
	}
// Usa un ítem de l'inventari (consumeix 1 unitat)
public void usarItem(Item i) {
	inventari.usarItem(i);
}

// Afegeix un ítem a l'inventari respectant els límits màxims
public void agregarItem(Item i) {
	inventari.afegirItem(i);
}

// Treu (descarta) un ítem de l'inventari sense usar-lo
public void trureItem(Item i) {
	inventari.tirarItem(i);
}


}

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
		super(nickname, color); // OBLIGATORIO
		this.inventari = inventari;
	}

/// GETTERS
	public Inventari getInventari() {
		return inventari;
	}

/// METODES
	// METODE PER BATALLAR AMB UN RIVAL
	public void gestionarBatalla(Pingui pingu) {

	    // Comprovació previa
	    if (pingu == null) {
	        System.out.println("ERROR: OPERACIO INVALIDA (JUGADOR BUIT)");
	        return;
	    }

	    int bolesJ1 = this.getInventari().getBoles();
	    int bolesJ2 = pingu.getInventari().getBoles();



	    if (bolesJ1 < 0 || bolesJ2 < 0) {
	        System.out.println("ERROR: OPERACIO INVALIDA AMB LES BOLES DE NEU");
	        return;
	    }
	    
	    // MESURAR DISTANCIA ENTRE LES BOLES DE NEU DEL ATACANT I DEL OPONENT
        int diferencia = bolesJ1 - bolesJ2;
        
	    // CAS 1: Guanya el atacant
	    if (bolesJ1 > bolesJ2) {

	        System.out.println(this.getNickname() + " guanya!");

	        this.mourePosicio(diferencia);

	    // CAS 2: Guanya el contrincant
	    } else if (bolesJ1 < bolesJ2) {

	        System.out.println(pingu.getNickname() + " guanya!");

	        pingu.mourePosicio(diferencia);

	    // CAS 3: Empat
	    } else {

	        System.out.println("Empat! Es perden totes les boles de neu.");

	        // Eliminem totes les boles del jugador atacant
	        for (int i = 0; i < bolesJ1; i++) {
	        	this.getInventari().eliminarItemsPerTipus(BolaNeu.class);
	        }

	        // Eliminem totes les boles del rival
	        for (int i = 0; i < bolesJ2; i++) {
	        	pingu.getInventari().eliminarItemsPerTipus(BolaNeu.class);
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

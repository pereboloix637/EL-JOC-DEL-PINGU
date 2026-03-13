package model.entitats;

import model.items.Inventari;
import model.items.Item;
import model.items.BolaNeu;

public class Pinguino extends Jugador {
/// ATRIBUTS
	public Inventari inventari;
	private String contrasenya; // Para el ranking/login
	private int victories; // Para el ranking
// Aixo es una expansio de Jugador

/// CONSTRUCTOR
	public Pinguino(String nickname, String color, Inventari inventari) {
		super(nickname, color); // OBLIGATORIO
		this.inventari = inventari;
		this.contrasenya = "";
		this.victories = 0;
	}

/// GETTERS
	public Inventari getInventari() {
		return inventari;
	}

	public String getContrasenya() {
		return contrasenya;
	}

	public void setContrasenya(String contrasenya) {
		this.contrasenya = contrasenya;
	}

	public int getVictories() {
		return victories;
	}

	public void setVictories(int victories) {
		this.victories = victories;
	}

/// METODES
	// METODE PER BATALLAR AMB UN RIVAL
	public void gestionarBatalla(Pinguino pingu) {
		int bolesJ1 = this.getInventari().getBoles();
		int bolesJ2 = (pingu != null) ? pingu.getInventari().getBoles() : -1;

		// Validamos: Rival no nulo y que las cantidades de bolas sean lógicas
		if (pingu != null && bolesJ1 >= 0 && bolesJ2 >= 0) {
			int diferencia = bolesJ1 - bolesJ2;

			if (bolesJ1 > bolesJ2) {
				// CAS 1: Guanya el atacant
				System.out.println(this.getNickname() + " guanya!");
				this.mourePosicio(diferencia);
				System.out.println("El rival retrocedira " + diferencia + " caselles...");
			} else if (bolesJ1 < bolesJ2) {
				// CAS 2: Guanya el contrincant
				System.out.println(pingu.getNickname() + " guanya!");
				pingu.mourePosicio(diferencia);
				System.out.println("El rival retrocedira " + diferencia + " caselles...");
			} else {
				// CAS 3: Empat - Se pierden todas las bolas
				System.out.println("Empat! Es perden totes les boles de neu.");
				System.out.println("--------------------------------------------------------------------");

				System.out.println("Inventari del atacant: " + this.getNickname());
				System.out.println("Se li treuran " + bolesJ1 + " Boles de Neu.");
				this.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				System.out.println("__________________________________________________________________________");

				System.out.println("Inventari del rival: " + pingu.getNickname());
				System.out.println("Se li treuran " + bolesJ2 + " Boles de Neu.");
				pingu.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				System.out.println("__________________________________________________________________________");
			}
		} else {
			// Gestión de errores unificada
			if (pingu == null) {
				System.out.println("ERROR: OPERACIO INVALIDA (JUGADOR BUIT)");
			} else {
				System.out.println("ERROR: OPERACIO INVALIDA AMB LES BOLES DE NEU");
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

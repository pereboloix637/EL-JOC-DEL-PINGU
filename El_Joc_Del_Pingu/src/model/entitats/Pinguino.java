package model.entitats;

import model.items.Inventari;
import model.items.Item;
import model.items.BolaNeu;

public class Pinguino extends Jugador {
/// ATRIBUTS
	public Inventari inventari;
	private String contrasenya; // Per al rànquing/login
	private int victories; // Per al rànquing


/// CONSTRUCTOR
	public Pinguino(String nickname, String color, Inventari inventari) {
		super(nickname, color); // OBLIGATORI
		this.inventari = inventari;
		this.contrasenya = "";
		this.victories = 0;
	}

/// GETTERS I SETTERS
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

		// Validem: Rival no nul i que les quantitats de boles siguin lògiques
		if (pingu != null && bolesJ1 >= 0 && bolesJ2 >= 0) {
			// Mostrar overlay de batalla (via helper estático)
			vista.PantallaJuego.mostrarOverlayBatallaEstatico();

			// Guardar posiciones anteriores para animaciones
			int posJ1Abans = this.getPosicio();
			int posJ2Abans = pingu.getPosicio();

			int diferencia = bolesJ1 - bolesJ2;
			System.out.println("¡La lucha empieza! Ambos lanzan sus bolas de nieve y...");
			String resultMsg = "";
			if (bolesJ1 > bolesJ2) {
				// CAS 1: Guanya l'atacant
				System.out.println(this.getNickname() + " gana!");
				pingu.mourePosicio(-diferencia); 
				System.out.println("El rival retrocedera " + diferencia + " caselles...");
				resultMsg = this.getNickname() + " guanya! " + pingu.getNickname() + " retrocede.";
				this.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				pingu.getInventari().eliminarItemsPerTipus(BolaNeu.class);
			} else if (bolesJ1 < bolesJ2) {
				// CAS 2: Guanya el contrincant
				System.out.println(pingu.getNickname() + " gana!");
				this.mourePosicio(diferencia); 
				System.out.println("El atacante retrocedera " + Math.abs(diferencia) + " casillas...");
				resultMsg = pingu.getNickname() + " guanya! " + this.getNickname() + " retrocede.";
				this.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				pingu.getInventari().eliminarItemsPerTipus(BolaNeu.class);
			} else {
				// CAS 3: Empat
				System.out.println("Empat! Cap jugador retrocedeix, però perden les boles de neu.");
				this.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				pingu.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				resultMsg = "Empate! Ambos pierden todas las bolas de nieve.";
			}

			// Mostrar resultat en un Alert (UI)
			javafx.scene.control.Alert batallaAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
			vista.PantallaJuego.estilarAlerta(batallaAlert);
			batallaAlert.setTitle("Resultado de la Batalla");
			batallaAlert.setHeaderText("¡Combate de bolas de nieve!");
			batallaAlert.setContentText(resultMsg);
			batallaAlert.showAndWait();

			// Animación de retroceso si alguien ha movido
			if (this.getPosicio() != posJ1Abans) {
				vista.PantallaJuego.animarRetrocesoEstatico(this, posJ1Abans, this.getPosicio());
			}
			if (pingu.getPosicio() != posJ2Abans) {
				vista.PantallaJuego.animarRetrocesoEstatico(pingu, posJ2Abans, pingu.getPosicio());
			}

		} else {
			// Gestió d'errors unificada
			if (pingu == null) {
				System.out.println("ERROR: OPERACIÓN INVÀLIDA (JUGADOR VACIO)");
			} else {
				System.out.println("ERROR: OPERACIÓN INVÀLIDA CON LAS BOLAS DE NIEVE");
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

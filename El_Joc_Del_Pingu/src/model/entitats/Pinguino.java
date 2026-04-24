package model.entitats;

import model.items.Inventari;
import model.items.Item;
import model.items.BolaNeu;

public class Pinguino extends Jugador {

	public Inventari inventari;
	private String contrasenya; // Para el ranking/login
	private int victories; // Para el ranking



	public Pinguino(String nickname, String color, Inventari inventari) {
		super(nickname, color); // OBLIGATORIO
		this.inventari = inventari;
		this.contrasenya = "";
		this.victories = 0;
	}


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


	// MÉTODO PARA PELEAR CON UN RIVAL
	public void gestionarBatalla(Pinguino pingu) {
		int bolesJ1 = this.getInventari().getBoles();
		int bolesJ2 = -1;
		if (pingu != null) {
			bolesJ2 = pingu.getInventari().getBoles();
		}

		// Validamos: Rival no nulo y que las cantidades de bolas sean lógicas
		if (pingu != null && bolesJ1 >= 0 && bolesJ2 >= 0) {
			// Guardar posiciones anteriores para animaciones
			int posJ1Abans = this.getPosicio();
			int posJ2Abans = pingu.getPosicio();

			int diferencia = bolesJ1 - bolesJ2;
			System.out.println("¡Empieza la pelea! Los dos tiran bolas de nieve y...");
			if (bolesJ1 > bolesJ2) {
				// CASO 1: Gana el atacante
				System.out.println("¡" + this.getNickname() + " gana!");
				pingu.mourePosicio(-diferencia); 
				System.out.println("El rival retrocederá " + diferencia + " casillas...");
				this.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				pingu.getInventari().eliminarItemsPerTipus(BolaNeu.class);
			} else if (bolesJ1 < bolesJ2) {
				// CASO 2: Gana el contrincante
				System.out.println("¡" + pingu.getNickname() + " gana!");
				this.mourePosicio(diferencia); 
				System.out.println("El atacante retrocederá " + Math.abs(diferencia) + " casillas...");
				this.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				pingu.getInventari().eliminarItemsPerTipus(BolaNeu.class);
			} else {
				// CASO 3: Empate
				System.out.println("¡Empate! Nadie retrocede, pero se pierden las bolas de nieve.");
				this.getInventari().eliminarItemsPerTipus(BolaNeu.class);
				pingu.getInventari().eliminarItemsPerTipus(BolaNeu.class);
			}


		} else {
			// Gestión de errores unificada
			if (pingu == null) {
				System.out.println("ERROR: OPERACIÓN INVÁLIDA (JUGADOR VACÍO)");
			} else {
				System.out.println("ERROR: OPERACIÓN INVÁLIDA CON LAS BOLAS DE NIEVE");
			}
		}
	}

// Usa un ítem del inventario (consume 1 unidad)
	public void usarItem(model.items.Item i) {
		inventari.usarItem(i);
	}

// Añade un ítem al inventario respetando los límites máximos
	public void agregarItem(model.items.Item i) {
		inventari.afegirItem(i);
	}

// Quita (descarta) un ítem del inventario sin usarlo
	public void trureItem(model.items.Item i) {
		inventari.eliminarItem(i);
	}

}

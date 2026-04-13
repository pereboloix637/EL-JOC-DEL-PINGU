package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.BolaNeu;
import model.items.Dau;
import model.items.Peix;

import java.util.Random;

/**
 * Casella Event: el pingüí rep un ítem aleatori (peix, boles de neu o dau especial).
 */
public class Event extends Casella {

	// Noms dels events possibles (ús informatiu / futura extensió)
	private String[] esdeveniments;
	
	private Runnable callbackFinalizacion;

	// Constructor
	public Event(int posicio, String[] esdeveniments) {
		super(posicio);
		this.esdeveniments = esdeveniments;
	}

	// Getters i Setters
	public String[] getEsdeveniments() {
		return esdeveniments;
	}

	public void setEsdeveniments(String[] esdeveniments) {
		this.esdeveniments = esdeveniments;
	}

	public void setCallbackFinalizacion(Runnable callback) {
		this.callbackFinalizacion = callback;
	}

	@Override
	public void realitzarAccio(Partida partida, Jugador jugador) {
		if (!(jugador instanceof Pinguino)) return;
		Pinguino pingui = (Pinguino) jugador;

		int roll = new Random().nextInt(100);
		int index;
		if (roll < 25) {
			index = 0; // 25% Peix
		} else if (roll < 50) {
			index = 1; // 25% Boles
		} else if (roll < 90) {
			index = 3; // 40% Dau Lent (probabilitat alta)
		} else {
			index = 2; // 10% Dau Ràpid (probabilitat baixa)
		}
		
		// En lugar de aplicar directamente, pedimos a la vista que muestre la ruleta
		// y pase un callback para aplicar el premio al terminar.
		vista.PantallaJuego.mostrarRuletaEstatico(pingui, index, () -> {
			aplicarPremio(partida, pingui, index);
			if (callbackFinalizacion != null) {
				callbackFinalizacion.run();
				callbackFinalizacion = null; // Limpiar para evitar re-ejecución accidental
			}
		});
	}

	public void aplicarPremio(Partida partida, Pinguino pingui, int index) {
		Random random = new Random();
		switch (index) {
		case 0:
			if (pingui.getInventari().getPeixos() < 2) {
				pingui.getInventari().afegirItem(new Peix("Peix", 1));
				vista.PantallaJuego.mostrarPopupItem(pingui, "Pez.png");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha obtenido 1 pez en la ruleta!", "log-info");
			} else {
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ya tenía el máximo de peces.", "log-info");
			}
			break;
		case 1:
			int bolesNoves = random.nextInt(3) + 1;
			int bolesAfegir = Math.min(bolesNoves, 6 - pingui.getInventari().getBoles());
			if (bolesAfegir > 0) {
				pingui.getInventari().afegirItem(new BolaNeu("Bola de Neu", bolesAfegir));
				vista.PantallaJuego.mostrarPopupItem(pingui, "BolasNieve.png");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha obtenido " + bolesAfegir + " bolas de nieve!", "log-info");
			} else {
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ya tenía el máximo de bolas de nieve.", "log-info");
			}
			break;
		case 2:
			if (pingui.getInventari().getDausEspecials() < 3) {
				pingui.getInventari().afegirItem(new Dau("Dado rápido", 1, 5, 10));
				vista.PantallaJuego.mostrarPopupItem(pingui, "Dado_Rapido.png");
			} else {
				System.out.println(pingui.getNickname() + " ya tiene el máximo de dados especiales (3).");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caído en una casilla de evento, ¡pero ya tiene el máximo de dados especiales!", "log-info");
			}
			break;

		case 3:
			// EVENT 3: dau lent 1-3 caselles (màxim 3 daus especials)
			if (pingui.getInventari().getDausEspecials() < 3) {
				pingui.getInventari().afegirItem(new Dau("Dado lento", 1, new int[] { 1, 3 }));
				System.out.println(pingui.getNickname() + " ¡ha obtenido un dado lento! (1 o 3 casillas)");
				vista.PantallaJuego.mostrarPopupItem(pingui, "Dado_Lento.png");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ¡ha caído en una casilla de evento y ha obtenido un dado lento!", "log-info");
			} else {
				System.out.println(pingui.getNickname() + " ya tiene el máximo de dados especiales (3).");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caído en una casilla de evento, ¡pero ya tiene el máximo de dados especiales!", "log-info");
			}
			break;
		}
	}
}
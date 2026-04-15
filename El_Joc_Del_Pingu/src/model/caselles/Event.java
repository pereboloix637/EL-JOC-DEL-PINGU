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
		if (roll < 15) {
			index = 0; // 15% Peix
		} else if (roll < 30) {
			index = 1; // 15% Boles
		} else if (roll < 44) {
			index = 2; // 14% Dau Ràpid
		} else if (roll < 58) {
			index = 3; // 14% Dau Lent
		} else if (roll < 72) {
			index = 4; // 14% Moto de Nieve
		} else if (roll < 86) {
			index = 5; // 14% Perder Turno
		} else {
			index = 6; // 14% Perder Item
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
		case 4:
			// EVENT 4: Moto de Nieve - Lleva al trineo más cercano por delante
			int posActual = pingui.getPosicio();
			int seguentTrineu = -1;
			for (Casella casella : partida.getTaulell().getCaselles()) {
				if (casella instanceof Trineu && casella.getPosicio() > posActual) {
					if (seguentTrineu == -1 || casella.getPosicio() < seguentTrineu) {
						seguentTrineu = casella.getPosicio();
					}
				}
			}

			if (seguentTrineu != -1) {
				pingui.setPosicio(seguentTrineu);
				vista.PantallaJuego.registrarEventoEstatico("¡A " + pingui.getNickname() + " le ha tocado la moto de nieve y ha avanzado hasta el trineo más cercano!", "log-info");
			} else {
				vista.PantallaJuego.registrarEventoEstatico("¡A " + pingui.getNickname() + " le ha tocado la moto de nieve pero no hay más trineos adelante!", "log-info");
			}
			break;

		case 5:
			// EVENT 5: Perder Turno - El jugador pierde el siguiente turno
			pingui.setTornsBloquejat(pingui.getTornsBloquejat() + 1);
			vista.PantallaJuego.mostrarPopupItem(pingui, "perder_turno.png");
			vista.PantallaJuego.registrarEventoEstatico("¡" + pingui.getNickname() + " ha perdido el siguiente turno!", "log-warning");
			break;

		case 6:
			// EVENT 6: Perder Item - El jugador pierde un item aleatorio del inventario
			String itemPerdut = pingui.getInventari().retirarItemAleatorio();
			vista.PantallaJuego.mostrarPopupItem(pingui, "perder_item.png");
			if (itemPerdut != null) {
				vista.PantallaJuego.registrarEventoEstatico("¡" + pingui.getNickname() + " ha perdido un " + itemPerdut + " de su inventario!", "log-warning");
			} else {
				vista.PantallaJuego.registrarEventoEstatico("¡A " + pingui.getNickname() + " le ha tocado perder item, pero no tiene nada en el inventario!", "log-info");
			}
			break;
		}
	}
}
package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.BolaNeu;
import model.items.Dau;
import model.items.Peix;

import java.util.Random;

/**
 * Casella Event: el jugador (Pinguino o Foca) rep un ítem aleatori (peix, boles de neu o dau especial).
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
		if ((jugador instanceof Pinguino) || (jugador instanceof model.entitats.Foca)) {
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
			
			if (jugador instanceof Pinguino) {
				// En lugar de aplicar directamente, pedimos a la vista que muestre la ruleta
				// y pase un callback para aplicar el premio al terminar.
				vista.PantallaJuego.mostrarRuletaEstatico(jugador, index, () -> {
					aplicarPremio(partida, jugador, index);
					if (callbackFinalizacion != null) {
						callbackFinalizacion.run();
						callbackFinalizacion = null; // Limpiar para evitar re-ejecución accidental
					}
				});
			} else {
				// Foca: sin ruleta
				aplicarPremio(partida, jugador, index);
				if (callbackFinalizacion != null) {
					callbackFinalizacion.run();
					callbackFinalizacion = null; // Limpiar para evitar re-ejecución accidental
				}
			}
		}
	}

	public void aplicarPremio(Partida partida, Jugador jugador, int index) {
		Random random = new Random();
		model.items.Inventari inventari = null;
		
		if (jugador instanceof Pinguino) {
			inventari = ((Pinguino)jugador).getInventari();
		} else if (jugador instanceof model.entitats.Foca) {
			inventari = ((model.entitats.Foca)jugador).getInventari();
		}
		
		if (inventari != null) {
			switch (index) {
			case 0:
				if (inventari.getPeixos() < 2) {
					inventari.afegirItem(new Peix("Peix", 1));
					vista.PantallaJuego.mostrarPopupItem(jugador, "Pez.png");
					String msg = (jugador instanceof Pinguino) ? jugador.getNickname() + " ha obtenido 1 pez en la ruleta!" : jugador.getNickname() + " ha obtenido 1 pez!";
					vista.PantallaJuego.registrarEventoEstatico(msg, "log-info");
				} else {
					vista.PantallaJuego.registrarEventoEstatico(jugador.getNickname() + " ya tenía el máximo de peces.", "log-info");
				}
				break;
			case 1:
				int bolesNoves = random.nextInt(3) + 1;
				int bolesAfegir = Math.min(bolesNoves, 6 - inventari.getBoles());
				if (bolesAfegir > 0) {
					inventari.afegirItem(new BolaNeu("Bola de Neu", bolesAfegir));
					vista.PantallaJuego.mostrarPopupItem(jugador, "BolasNieve.png");
					String msg = (jugador instanceof Pinguino) ? jugador.getNickname() + " ha obtenido " + bolesAfegir + " bolas de nieve en la ruleta!" : jugador.getNickname() + " ha obtenido " + bolesAfegir + " bolas de nieve!";
					vista.PantallaJuego.registrarEventoEstatico(msg, "log-info");
				} else {
					vista.PantallaJuego.registrarEventoEstatico(jugador.getNickname() + " ya tenía el máximo de bolas de nieve.", "log-info");
				}
				break;
			case 2:
				if (inventari.getDausEspecials() < 3) {
					inventari.afegirItem(new Dau("Dado Rápido", 1, 5, 10));
					vista.PantallaJuego.mostrarPopupItem(jugador, "Dado_Rapido.png");
					String msg = (jugador instanceof Pinguino) ? jugador.getNickname() + " ha obtenido un Dado Rápido en la ruleta!" : jugador.getNickname() + " ha obtenido un Dado Rápido!";
					vista.PantallaJuego.registrarEventoEstatico(msg, "log-info");
				} else {
					System.out.println(jugador.getNickname() + " ya tiene el máximo de dados especiales (3).");
					vista.PantallaJuego.registrarEventoEstatico(jugador.getNickname() + " ha caído en una casilla de evento, ¡pero ya tiene el máximo de dados especiales!", "log-info");
				}
				break;
	
			case 3:
				if (inventari.getDausEspecials() < 3) {
					inventari.afegirItem(new Dau("Dado Lento", 1, new int[] { 1, 3 }));
					System.out.println(jugador.getNickname() + " ¡ha obtenido un dado lento! (1 o 3 casillas)");
					vista.PantallaJuego.mostrarPopupItem(jugador, "Dado_Lento.png");
					String msg = (jugador instanceof Pinguino) ? jugador.getNickname() + " ha obtenido un Dado Lento en la ruleta!" : jugador.getNickname() + " ha obtenido un Dado Lento!";
					vista.PantallaJuego.registrarEventoEstatico(msg, "log-info");
				} else {
					System.out.println(jugador.getNickname() + " ya tiene el máximo de dados especiales (3).");
					vista.PantallaJuego.registrarEventoEstatico(jugador.getNickname() + " ha caído en una casilla de evento, ¡pero ya tiene el máximo de dados especiales!", "log-info");
				}
				break;
			case 4:
				int posActual = jugador.getPosicio();
				int seguentTrineu = -1;
				for (Casella casella : partida.getTaulell().getCaselles()) {
					if (casella instanceof Trineu && casella.getPosicio() > posActual) {
						if (seguentTrineu == -1 || casella.getPosicio() < seguentTrineu) {
							seguentTrineu = casella.getPosicio();
						}
					}
				}
	
				if (seguentTrineu != -1) {
					jugador.setPosicio(seguentTrineu);
					vista.PantallaJuego.registrarEventoEstatico("¡A " + jugador.getNickname() + " le ha tocado la moto de nieve y ha avanzado hasta el trineo más cercano!", "log-info");
				} else {
					vista.PantallaJuego.registrarEventoEstatico("¡A " + jugador.getNickname() + " le ha tocado la moto de nieve pero no hay más trineos adelante!", "log-info");
				}
				break;
	
			case 5:
				jugador.setTornsBloquejat(jugador.getTornsBloquejat() + 1);
				vista.PantallaJuego.mostrarPopupItem(jugador, "perder_turno.png");
				vista.PantallaJuego.registrarEventoEstatico("¡" + jugador.getNickname() + " ha perdido el siguiente turno!", "log-warning");
				break;
	
			case 6:
				String itemPerdut = inventari.retirarItemAleatorio();
				vista.PantallaJuego.mostrarPopupItem(jugador, "perder_item.png");
				if (itemPerdut != null) {
					vista.PantallaJuego.registrarEventoEstatico("¡" + jugador.getNickname() + " ha perdido un " + itemPerdut + " de su inventario!", "log-warning");
				} else {
					vista.PantallaJuego.registrarEventoEstatico("¡A " + jugador.getNickname() + " le ha tocado perder item, pero no tiene nada en el inventario!", "log-info");
				}
				break;
			}
		}
	}
}
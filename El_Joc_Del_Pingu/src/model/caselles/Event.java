package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.BolaNeu;
import model.items.Dau;
import model.items.Peix;

import java.util.Random;

/**
 * Casella Event: el pingüí rep un ítem aleatori (peix, boles de neu o dau
 * especial).
 */
public class Event extends Casella {

	// Noms dels events possibles (ús informatiu / futura extensió)
	private String[] esdeveniments;

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

	// Escull un event aleatori entre 4 i l'aplica a l'inventari del pingüí
	@Override
	public void realitzarAccio(Partida partida, Jugador jugador) {
		// Només els pingüins poden activar events
		if (!(jugador instanceof Pinguino))
			return;
		Pinguino pingui = (Pinguino) jugador;

		Random random = new Random();
		int index = random.nextInt(4);

		switch (index) {
		case 0:
			// EVENT 0: 1 peix (màxim 2)
			if (pingui.getInventari().getPeixos() < 2) {
				pingui.getInventari().afegirItem(new Peix("Peix", 1));
				System.out.println(pingui.getNickname() + " ha obtingut 1 peix!");
				vista.PantallaJuego.mostrarPopupItem(pingui, "Pez.png");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caigut en una casella d'esdeveniment i ha obtingut 1 peix!", "log-info");
			} else {
				System.out.println(pingui.getNickname() + " ja té el màxim de peixos (2).");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caigut en una casella d'esdeveniment però ja té el màxim de peixos!", "log-info");
			}
			break;

		case 1:
			// EVENT 1: 1-3 boles de neu (màxim 6)
			int bolesNoves = random.nextInt(3) + 1;
			int bolesAfegir = Math.min(bolesNoves, 6 - pingui.getInventari().getBoles());
			if (bolesAfegir > 0) {
				pingui.getInventari().afegirItem(new BolaNeu("Bola de Neu", bolesAfegir));
				System.out.println(pingui.getNickname() + " ha obtingut " + bolesAfegir + " boles de neu!");
				vista.PantallaJuego.mostrarPopupItem(pingui, "BolasNieve.png");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caigut en una casella d'esdeveniment i ha obtingut " + bolesAfegir + " boles de neu!", "log-info");
			} else {
				System.out.println(pingui.getNickname() + " ja té el màxim de boles de neu (6).");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caigut en una casella d'esdeveniment però ja té el màxim de boles de neu!", "log-info");
			}
			break;

		case 2:
			// EVENT 2: dau ràpid 5-10 caselles (màxim 3 daus especials)
			if (pingui.getInventari().getDausEspecials() < 3) {
				pingui.getInventari().afegirItem(new Dau("Dau ràpid", 1, 5, 10));
				System.out.println(pingui.getNickname() + " ha obtingut un dau ràpid! (5-10 caselles)");
				vista.PantallaJuego.mostrarPopupItem(pingui, "Dado_Rapido.png");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caigut en una casella d'esdeveniment i ha obtingut un dau ràpid!", "log-info");
			} else {
				System.out.println(pingui.getNickname() + " ja té el màxim de daus especials (3).");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caigut en una casella d'esdeveniment però ja té el màxim de daus especials!", "log-info");
			}
			break;

		case 3:
			// EVENT 3: dau lent 1-3 caselles (màxim 3 daus especials)
			if (pingui.getInventari().getDausEspecials() < 3) {
				pingui.getInventari().afegirItem(new Dau("Dau lent", 1, new int[] { 1, 3 }));
				System.out.println(pingui.getNickname() + " ha obtingut un dau lent! (1 o 3 caselles)");
				vista.PantallaJuego.mostrarPopupItem(pingui, "Dado_Lento.png");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caigut en una casella d'esdeveniment i ha obtingut un dau lent!", "log-info");
			} else {
				System.out.println(pingui.getNickname() + " ja té el màxim de daus especials (3).");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha caigut en una casella d'esdeveniment però ja té el màxim de daus especials!", "log-info");
			}
			break;
		}
	}
}
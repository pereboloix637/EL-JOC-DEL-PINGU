package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.Item;

/**
 * Casella Trencadís: el terra es trenca.
 * L'efecte depèn del total d'objectes que porta el jugador:
 *   - Més de 5 objectes → cau i torna a l'inici.
 *   - 1 a 5 objectes   → perd un torn.
 *   - Sense objectes   → passa sense penalització.
 */
public class Trencadis extends Casella {

	// Constructor
	public Trencadis(int posicio) {
		super(posicio);
	}

	@Override
	public void realitzarAccio(Partida partida, Jugador jugador) {
		// Comptem el total d'unitats de tots els ítems de l'inventari
		int totalObjectes = 0;
		if (jugador instanceof Pinguino pingui) {
			for (Item item : pingui.getInventari().getLlista()) {
				totalObjectes += item.getQuantitat();
			}
		}

		if (totalObjectes == 0) {
			// Sense objectes: passa sense penalització
			System.out.println(jugador.getNickname()
					+ " ha trepitjat un sòl trencat, però no porta res → passa sense penalització.");
			vista.PantallaJuego.registrarEventoEstatico(jugador.getNickname() + " ha trepitjat un sòl trencat però passa sense penalització.", "log-info");
		} else if (totalObjectes <= 5) {
			// Fins a 5 objectes: perd un torn
			jugador.setTornsBloquejat(jugador.getTornsBloquejat() + 1);
			System.out.println(jugador.getNickname()
					+ " ha trepitjat un sòl trencat i porta " + totalObjectes
					+ " objectes → perd un torn!");
			vista.PantallaJuego.registrarEventoEstatico(jugador.getNickname() + " ha trepitjat un sòl trencat i perd un torn!", "log-warning");
		} else {
			// Més de 5 objectes: cau i torna a l'inici
			jugador.setPosicio(0);
			System.out.println(jugador.getNickname()
					+ " ha trepitjat un sòl trencat i porta " + totalObjectes
					+ " objectes → cau i torna a l'inici!");
			vista.PantallaJuego.registrarEventoEstatico(jugador.getNickname() + " ha trepitjat un sòl trencat i torna a l'inici!", "log-warning");
		}
	}
}
package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pingui;
import model.items.Peix;

/** Casella Os: l'ós ataca el jugador. El pingüí es pot defensar amb un peix. */
public class Os extends Casella {

	// Constructor
	public Os(int posicio) {
		super(posicio);
	}

	// Si el pingüí té un peix el gasta; sense peix torna a l'inici
	@Override
	public void realitzarAccio(Partida partida, Jugador jugador) {
		if (!(jugador instanceof Pingui))
			return;
		Pingui pingui = (Pingui) jugador;

		Peix peix = (Peix) pingui.getInventari().getLlista().stream()
				.filter(i -> i instanceof Peix && i.getQuantitat() > 0).findFirst().orElse(null);

		if (peix != null) {
			pingui.getInventari().usarItem(peix);
			System.out.println(pingui.getNickname() + " ha estat atacat per un ós! Però tenia un peix i s'ha salvat.");
		} else {
			pingui.setPosicio(0);
			System.out.println(pingui.getNickname() + " ha estat atacat per un ós! Torna a l'inici.");
		}
	}
}

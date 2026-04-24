package controlador;

import model.core.Taulell;
import model.entitats.Foca;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.Dau;
import model.items.Item;
import model.items.Peix;



// Gestiona les acciones del jugador en la partida
public class GestorJugador {
	
	// Usa l'ítem de l'inventari que coincideixi amb el nom
	public void jugadorUsaItem(Pinguino p, String nombreItem) {
		boolean encontrado = false;
		for (int i = 0; i < p.getInventari().getLlista().size() && !encontrado; i++) {
			Item item = p.getInventari().getLlista().get(i);
			if (item.getNom().equalsIgnoreCase(nombreItem)) {
				p.usarItem(item);
				encontrado = true;
			}
		}
		if (!encontrado) {
			System.out.println(p.getNickname() + " no tiene \"" + nombreItem + "\" en el inventario.");
		}
	}

	
	// Mou el jugador: usa dau especial si en té, sinó dau normal (1-6)
	public void jugadorSeMou(Jugador j, int pasos, Taulell t) {
		int resultat;

		if (pasos > 0) {
			resultat = pasos;
		} else if (j instanceof Pinguino) {
			Pinguino p = (Pinguino) j;
			Dau dauEspecial = (Dau) p.getInventari().obtenirPrimer(Dau.class);
			if (dauEspecial != null) {
				resultat = dauEspecial.tirarIUsar();
				if (resultat == -1)
					resultat = new Dau().tirar();
			} else {
				resultat = new Dau().tirar();
			}
		} else {
			resultat = new Dau().tirar();
		}

		int novaPos = Math.min(j.getPosicio() + resultat, t.getCaselles().size() - 1);
		j.setPosicio(novaPos);
		System.out.println(j.getNickname() + " se mueve " + resultat + " casillas → posición " + novaPos + ".");
	}

	
	// Finalitza el torn del jugador
	public void jugadorFinalitzaTorn(Jugador j) {
		System.out.println(j.getNickname() + " ha finalizado su turno.");
	}

	
	// El pingüí rep un event: guanya un peix si l'inventari no esta ple
	public void pinguinoEvento(Pinguino p) {
		Peix peixRecompensa = new Peix("Pez", 1);
		int afegits = p.getInventari().afegirItem(peixRecompensa);
		if (afegits > 0)
			System.out.println("¡Evento! " + p.getNickname() + " ha recibido un pez.");
		else
			System.out.println("¡Evento! " + p.getNickname() + " no puede coger más peces.");
	}

	
	// Inicia la batalla de boles de neu entre dos pingüins
	public void pinguinoGuerra(Pinguino p1, Pinguino p2) {
		System.out.println("¡Guerra entre " + p1.getNickname() + " y " + p2.getNickname() + "!");
		p1.gestionarBatalla(p2);
	}

	// El pingüí interactua amb la foca: usa un peix per salvar-se o torna a l'inici
	public void focaInteractua(Pinguino p, Foca f) {
		if (f.isSoborno()) {
			System.out.println("La foca deja pasar a " + p.getNickname() + " (sobornada).");
		} else {
			Peix peix = (Peix) p.getInventari().obtenirPrimer(Peix.class);
			if (peix != null && p.getInventari().usarItem(peix)) {
				System.out.println(p.getNickname() + " ¡usa un pez y se salva de la foca!");
			} else {
				System.out.println("¡La foca ataca a " + p.getNickname() + "! Vuelve a la posición 0.");
				p.setPosicio(0);
			}
		}
		
		
	}
	
	
}

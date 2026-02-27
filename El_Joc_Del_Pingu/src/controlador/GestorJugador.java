package controlador;

import java.util.Random;

import model.core.Taulell;
import model.entitats.Foca;
import model.entitats.Jugador;
import model.entitats.Pingui;
import model.items.Dau;
import model.items.Item;
import model.items.Peix;



// Gestiona les acciones del jugador en la partida
public class GestorJugador {

	private Random random;

	public GestorJugador() {
		this.random = new Random();
	}

	
	// Usa l'ítem de l'inventari que coincideixi amb el nom
	public void jugadorUsaItem(Pingui p, String nombreItem) {
		for (Item item : p.getInventari().getLlista()) {
			if (item.getNom().equalsIgnoreCase(nombreItem)) {
				p.usarItem(item);
				return;
			}
		}
		System.out.println(p.getNickname() + " no té \"" + nombreItem + "\" a l'inventari.");
	}

	
	// Mou el jugador: usa dau especial si en té, sinó dau normal (1-6)
	public void jugadorSeMou(Jugador j, int pasos, Taulell t) {
		int resultat;

		if (pasos > 0) {
			resultat = pasos;
		} else if (j instanceof Pingui) {
			Pingui p = (Pingui) j;
			Dau dauEspecial = (Dau) p.getInventari().obtenirPrimer(Dau.class);
			if (dauEspecial != null) {
				resultat = dauEspecial.tirarIUsar(random);
				if (resultat == -1)
					resultat = new Dau().tirar(random);
			} else {
				resultat = new Dau().tirar(random);
			}
		} else {
			resultat = new Dau().tirar(random);
		}

		int novaPos = Math.min(j.getPosicio() + resultat, t.getCaselles().size() - 1);
		j.setPosicio(novaPos);
		System.out.println(j.getNickname() + " es mou " + resultat + " caselles → posicio " + novaPos + ".");
	}

	
	// Finalitza el torn del jugador
	public void jugadorFinalitzaTorn(Jugador j) {
		System.out.println(j.getNickname() + " ha finalitzat el seu torn.");
	}

	
	// El pingüí rep un event: guanya un peix si l'inventari no esta ple
	public void pinguinoEvento(Pingui p) {
		Peix peixRecompensa = new Peix("Peix", 1);
		int afegits = p.getInventari().afegirItem(peixRecompensa);
		if (afegits > 0)
			System.out.println("Event! " + p.getNickname() + " ha rebut un peix.");
		else
			System.out.println("Event! " + p.getNickname() + " no pot agafar més peixos.");
	}

	
	// Inicia la batalla de boles de neu entre dos pingüins
	public void pinguinoGuerra(Pingui p1, Pingui p2) {
		System.out.println("Guerra entre " + p1.getNickname() + " i " + p2.getNickname() + "!");
		p1.gestionarBatalla(p2);
	}

	// El pingüí interactua amb la foca: usa un peix per salvar-se o torna a l'inici
	public void focaInteractua(Pingui p, Foca f) {
		if (f.isSoborno()) {
			System.out.println("La foca deixa passar a " + p.getNickname() + " (sobornada).");
		} else {
			Peix peix = (Peix) p.getInventari().obtenirPrimer(Peix.class);
			if (peix != null && p.getInventari().usarItem(peix)) {
				System.out.println(p.getNickname() + " usa un peix i es salva de la foca!");
			} else {
				System.out.println("La foca ataca a " + p.getNickname() + "! Torna a la posició 0.");
				p.setPosicio(0);
			}
		}
		
		
	}
	
	
}

package model.items;

import java.util.ArrayList;
import model.entitats.Jugador;

// Ítem Bola de Neu: fa retrocedir un rival N caselles. Màxim 6 per inventari
public class BolaNeu extends Item {

	private int retrocedir; // caselles que retrocedeix l'objectiu

	// Constructor amb retrocés personalitzat
	public BolaNeu(String nom, int quantitat, int retrocedir) {
		super(nom, quantitat);
		this.retrocedir = retrocedir;
	}

	// Constructor amb retrocés per defecte (3 caselles)
	public BolaNeu(String nom, int quantitat) {
		this(nom, quantitat, 3);
	}

	// Getters i Setters
	public int getRetrocedir() {
		return retrocedir;
	}

	public void setRetrocedir(int retrocedir) {
		this.retrocedir = retrocedir;
	}

	// Consumeix 1 bola de neu. Retorna true si ha tingut èxit
	@Override
	public boolean usar() {
		if (getQuantitat() > 0) {
			setQuantitat(getQuantitat() - 1);
			System.out.println("¡Has usado una bola de nieve! Te quedan " + getQuantitat() + " bolas de nieve.");
			return true;
		}
		System.out.println("¡No tienes bolas de nieve!");
		return false;
	}

	// Llença una bola contra un jugador concret i el fa retrocedir
	public boolean llençarBola(Jugador atacant, Jugador objectiu) {
		if (usar()) {
			int novaPos = Math.max(0, objectiu.getPosicio() - retrocedir);
			objectiu.setPosicio(novaPos);
			System.out.println(atacant.getNickname() + " ¡ha lanzado una bola de nieve a " + objectiu.getNickname()
					+ "! Retrocede " + retrocedir + " casillas hasta la posición " + novaPos + ".");
			return true;
		}
		return false;
	}

	// Llença una bola contra un rival aleatori de la partida
	public boolean llençarBolaAleatoria(Jugador atacant, ArrayList<Jugador> jugadors) {
		ArrayList<Jugador> rivals = new ArrayList<>();
		for (Jugador j : jugadors) {
			if (!j.equals(atacant))
				rivals.add(j);
		}
		if (rivals.isEmpty()) {
			System.out.println("¡No hay rivales a los que lanzar la bola de nieve!");
			return false;
		}
		int idx = (int) (Math.random() * rivals.size());
		return llençarBola(atacant, rivals.get(idx));
	}
}
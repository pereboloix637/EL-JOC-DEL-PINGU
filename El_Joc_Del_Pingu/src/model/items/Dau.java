package model.items;

import java.util.Random;

// Ítem Dau: normal (1-6) o especial (rang personalitzat). Màxim 3 especials per inventari
public class Dau extends Item {

	private int min;
	private int max;
	private boolean especial;

	// Constructor dau especial (s'emmagatzema a l'inventari)
	public Dau(String nom, int quantitat, int min, int max) {
		super(nom, quantitat);
		this.min = min;
		this.max = max;
		this.especial = true;
	}

	// Constructor dau normal (no ocupa inventari)
	public Dau() {
		super("Dau normal", 1);
		this.min = 1;
		this.max = 6;

	}

	// Getters i Setters
	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public void setMax(int max) {
		this.max = max;
	}

	// Tira el dau i retorna un valor aleatori entre min i max
	public int tirar() {
		Random r = new Random();
		int num = r.nextInt((max - min) + 1) + min;
		System.out.println("Tirada dau: " + num);
		return num;
	}

	// Consumeix 1 unitat del dau especial. Retorna true si ha tingut èxit
	@Override
	public boolean usar() {
		if (this.especial == true) { // Si es un dau especial, fer l'operacio
			if (getQuantitat() > 0) { // Si dins de aquesta condicio, la quantitat es 0.
				setQuantitat(getQuantitat() - 1);
				System.out.println("Has usat el " + getNom() + "! Et queden " + getQuantitat() + " daus especials.");
				return true;
			} // Si tens 0 d'aquest.
			System.out.println("No tens " + getNom() + "!");
			return false;
		} else { // Pero si el Dau NO ES especial (es a dir, un Dau Normal
			return true;
		}
	}

	// Tira el dau i consumeix 1 unitat. Retorna el resultat, o -1 si no en té
	public int tirarIUsar() {
		if (usar()) {
			int resultat = tirar();
			System.out.println("Tirada amb " + getNom() + ": " + resultat);
			return resultat;
		}
		return -1;
	}

	public boolean esEspecial() {
		return especial;
	}

}
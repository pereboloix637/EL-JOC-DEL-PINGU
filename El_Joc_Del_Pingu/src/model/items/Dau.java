package model.items;

import java.util.Random;

// Ítem Dau: normal (1-6) o especial (rang personalitzat). Màxim 3 especials per inventari
public class Dau extends Item {

	private int min;
	private int max;

	// Constructor dau especial (s'emmagatzema a l'inventari)
	public Dau(String nom, int quantitat, int min, int max) {
		super(nom, quantitat);
		this.min = min;
		this.max = max;
	}

	// Constructor dau normal (no ocupa inventari)
	public Dau() {
		super("Dau normal", 0);
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
	public int tirar(Random r) {
		return r.nextInt((max - min) + 1) + min;
	}

	// Consumeix 1 unitat del dau especial. Retorna true si ha tingut èxit
	@Override
	public boolean usar() {
		
		if (getQuantitat() > 0) {
			setQuantitat(getQuantitat() - 1);
			System.out.println("Has usat el " + getNom() + "! Et queden " + getQuantitat() + " daus especials.");
			return true;
		}
		System.out.println("No tens " + getNom() + "!");
		return false;
	}

	// Tira el dau i consumeix 1 unitat. Retorna el resultat, o -1 si no en té
	public int tirarIUsar(Random r) {
		if (usar()) {
			int resultat = tirar(r);
			System.out.println("Tirada amb " + getNom() + ": " + resultat);
			return resultat;
		}
		return -1;
	}
	
}
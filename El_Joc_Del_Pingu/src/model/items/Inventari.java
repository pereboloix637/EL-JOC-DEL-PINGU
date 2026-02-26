package model.items;

import java.util.ArrayList;

// Gestiona la llista d'ítems d'un pingüí respectant els límits de RF-03
public class Inventari {

	private ArrayList<Item> llista;

	// Constructor
	public Inventari() {
		llista = new ArrayList<>();
	}

	// Getters i Setters
	public ArrayList<Item> getLlista() {
		return llista;
	}

	public void setLlista(ArrayList<Item> llista) {
		this.llista = llista;
	}

	// ── Consultes ─────────────────────────────────────────────────────────────

	// Compta el total d'unitats d'un tipus d'ítem
	public int contarTipus(Class<? extends Item> tipus) {
		int total = 0;
		for (Item i : llista) {
			if (tipus.isInstance(i))
				total += i.getQuantitat();
		}
		return total;
	}

	public int getBoles() {
		return contarTipus(BolaNeu.class);
	}

	public int getPeixos() {
		return contarTipus(Peix.class);
	}

	public int getDausEspecials() {
		return contarTipus(Dau.class);
	}

	// Retorna el primer ítem disponible del tipus indicat, o null
	public Item obtenirPrimer(Class<? extends Item> tipus) {
		for (Item i : llista) {
			if (tipus.isInstance(i) && i.getQuantitat() > 0)
				return i;
		}
		return null;
	}

	// ── Gestió ────────────────────────────────────────────────────────────────

	// Afegeix un ítem respectant el límit màxim (hardcoded per tipus). Retorna les
	// unitats afegides realment
	public int afegirItem(Item item) {
		int max;
		if (item instanceof BolaNeu)
			max = 6;
		else if (item instanceof Peix)
			max = 2;
		else if (item instanceof Dau)
			max = 3;
		else {
			System.out.println("Aquest ítem no es pot guardar a l'inventari.");
			return 0;
		}
		int disponibles = max - contarTipus(item.getClass());
		if (disponibles <= 0) {
			System.out.println("Inventari ple per a " + item.getNom() + " (màxim " + max + ").");
			return 0;
		}
		int afegir = Math.min(item.getQuantitat(), disponibles);
		item.setQuantitat(afegir);
		llista.add(item);
		System.out.println("S'ha afegit: " + item.getNom() + " x" + afegir);
		return afegir;
	}

	// Elimina completament un ítem de la llista
	public void eliminarItem(Item item) {
		if (llista.remove(item))
			System.out.println("S'ha eliminat: " + item.getNom());
		else
			System.out.println("L'ítem " + item.getNom() + " no és a l'inventari.");
	}
	
	// Elimina el item per el seu tipus
	public void eliminarItemsPerTipus(Class<? extends Item> tipus) {

	    llista.removeIf(item -> tipus.isInstance(item));

	    System.out.println("S'han eliminat tots els ítems de tipus: " + tipus.getSimpleName());
	}	

	// Retorna l'ítem si existeix, null altrament
	public Item obtenirItem(Item item) {
		for (Item i : llista) {
			if (i.equals(item))
				return i;
		}
		return null;
	}

	// Llença un ítem sense usar-lo
	public void tirarItem(Item item) {
		if (llista.remove(item))
			System.out.println("Has llençat: " + item.getNom());
		else
			System.out.println("L'ítem " + item.getNom() + " no és a l'inventari.");
	}

	// Usa un ítem i l'elimina si arriba a 0 unitats. Retorna true si ha tingut èxit
	public boolean usarItem(Item item) {
		if (llista.contains(item)) {
			boolean usat = item.usar();
			if (usat && item.getQuantitat() <= 0)
				llista.remove(item);
			return usat;
		}
		System.out.println("L'ítem " + item.getNom() + " no és a l'inventari.");
		return false;
	}

	// Usa el primer ítem disponible del tipus indicat
	public boolean usarPrimer(Class<? extends Item> tipus) {
		Item i = obtenirPrimer(tipus);
		if (i != null)
			return usarItem(i);
		System.out.println("No tens cap " + tipus.getSimpleName() + " disponible.");
		return false;
	}

	@Override
	public String toString() {
		if (llista.isEmpty())
			return "Inventari buit.";
		StringBuilder sb = new StringBuilder("Inventari:\n");
		for (Item i : llista)
			sb.append("  - ").append(i).append("\n");
		return sb.toString();
	}
}
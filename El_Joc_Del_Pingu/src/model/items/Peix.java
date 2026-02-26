package model.items;

// Ítem Peix: evita tornar a l'inici quan l'ós ataca. Màxim 2 per inventari
public class Peix extends Item {

	// Constructor
	public Peix(String nom, int quantitat) {
		super(nom, quantitat);
	}

	// Usa un peix (consumeix 1 unitat). Retorna true si ha tingut èxit
	@Override
	public boolean usar() {
		if (getQuantitat() > 0) {
			setQuantitat(getQuantitat() - 1);
			System.out.println("Has usat un peix! Et queden " + getQuantitat() + " peixos.");
			return true;
		}
		System.out.println("No tens peixos!");
		return false;
	}

	// Alias de usar() per llegibilitat
	public boolean usarPeix() {
		return usar();
	}
}
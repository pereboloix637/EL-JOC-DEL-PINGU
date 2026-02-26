package model.items;

public abstract class Item {

    private String nom;
    private int quantitat;

    // Constructor
    public Item(String nom, int quantitat) {
        this.nom = nom;
        this.quantitat = quantitat;
    }

    // Getters
    public String getNom() { return nom; }
    public int getQuantitat() { return quantitat; }

    // Setters
    public void setNom(String nom) { this.nom = nom; }
    public void setQuantitat(int quantitat) { this.quantitat = quantitat; }

    // Usa l'ítem (consumeix 1 unitat). Retorna true si ha tingut èxit
    public abstract boolean usar();

    // Comprova si queda almenys 1 unitat
    public boolean estaDisponible() { return quantitat > 0; }

    @Override
    public String toString() { return nom + " (x" + quantitat + ")"; }
}
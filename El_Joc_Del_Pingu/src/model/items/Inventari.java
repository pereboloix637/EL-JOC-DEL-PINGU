package model.items;

import java.util.ArrayList;

/** Conté la llista d'ítems d'un pingüí i ofereix mètodes per gestionar-los. */
public class Inventari {

    // Atributs
    private ArrayList<Item> llista;

    // Constructor
    public Inventari() {
        llista = new ArrayList<>();
    }

    // Getters i Setters
    public ArrayList<Item> getLlista() { return llista; }
    public void setLlista(ArrayList<Item> llista) { this.llista = llista; }

    // Afegeix un ítem a l'inventari
    public void afegirItem(Item item) {
        llista.add(item);
        System.out.println("S'ha afegit: " + item.getNom() + " x" + item.getQuantitat());
    }

    // Elimina completament un ítem de l'inventari
    public void eliminarItem(Item item) {
        if (llista.remove(item)) {
            System.out.println("S'ha eliminat: " + item.getNom());
        } else {
            System.out.println("L'ítem " + item.getNom() + " no és a l'inventari.");
        }
    }

    // Retorna l'ítem si existeix a l'inventari, null altrament
    public Item obtenirItem(Item item) {
        for (Item i : llista) {
            if (i.equals(item)) return i;
        }
        return null;
    }

    // Llença (descarta) un ítem sense usar-lo
    public void tirarItem(Item item) {
        if (llista.remove(item)) {
            System.out.println("Has llençat: " + item.getNom());
        } else {
            System.out.println("L'ítem " + item.getNom() + " no és a l'inventari.");
        }
    }

    // Usa un ítem: redueix quantitat en 1 i l'elimina si arriba a 0
    public void usarItem(Item item) {
        if (llista.contains(item)) {
            item.setQuantitat(item.getQuantitat() - 1);
            if (item.getQuantitat() <= 0) {
                llista.remove(item);
            }
            System.out.println("Has usat: " + item.getNom());
        } else {
            System.out.println("L'ítem " + item.getNom() + " no és a l'inventari.");
        }
    }
}
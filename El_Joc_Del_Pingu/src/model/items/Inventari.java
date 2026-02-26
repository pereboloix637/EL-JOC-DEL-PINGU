package model.items;

import java.util.ArrayList;

public class Inventari {

    private ArrayList<Item> llista;

    // Constructor
    public Inventari() {
        llista = new ArrayList<>();
    }

    // Getter
    public ArrayList<Item> getLlista() {
        return llista;
    }

    // Setter
    public void setLlista(ArrayList<Item> llista) {
        this.llista = llista;
    }
    
    public void usarItem(Item item) {
        if (llista.contains(item)) {
            item.setQuantitat(item.getQuantitat() - 1);
            if (item.getQuantitat() <= 0) {
                llista.remove(item);
            }
            System.out.println("Has usat: " + item.getNom());
        } else {
            System.out.println("L'item " + item.getNom() + " no és a l'inventari.");
        }
    }
}
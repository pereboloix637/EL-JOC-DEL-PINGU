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
}
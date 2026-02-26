package model.core;

import java.util.ArrayList;
import model.caselles.Casella;

public class Taulell {

	private ArrayList<Casella> taulell;

    // Constructor
    public Taulell() {
        taulell = new ArrayList<>();
    }

    // Getter
    public ArrayList<taulell> getLlista() {
        return taulell;
    }

    // Setter
    public void setLlista(ArrayList<taulell> llista) {
        this.taulell = llista;
}

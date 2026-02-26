package model.core;

import java.util.ArrayList;

import model.caselles.Casella;

public class Taulell {

    // Atributs
    private ArrayList<Casella> caselles;

    // Constructor
    public Taulell(ArrayList<Casella> caselles) {
        this.caselles = caselles;
    }

    // Getters
    public ArrayList<Casella> getCaselles() {
        return caselles;
    }

    // Setters
    public void setCaselles(ArrayList<Casella> caselles) {
        this.caselles = caselles;
    }

    // Mètode actualitzarTaulell
    public void actualitzarTaulell() {
        // Lògica per actualitzar el taulell
    }
}

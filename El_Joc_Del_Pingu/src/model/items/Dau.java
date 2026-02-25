package model.items;

import java.util.Random;

public class Dau extends Item {

    private int max;
    private int min;

    // Constructor
    public Dau(String nom, int quantitat, int min, int max) {
        super(nom, quantitat);
        this.min = min;
        this.max = max;
    }

    // Getters
    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    // Setters
    public void setMax(int max) {
        this.max = max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    // Mètode tirar
    public int tirar(Random r) {
        return r.nextInt((max - min) + 1) + min;
    }
}
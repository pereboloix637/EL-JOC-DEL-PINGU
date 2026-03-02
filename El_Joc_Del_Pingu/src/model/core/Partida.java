package model.core;

import java.util.ArrayList;

import model.entitats.Jugador;

public class Partida {

    // Atributs
    private int id;
    private String seed;
    private Taulell taulell;
    private ArrayList<Jugador> jugadors;
    private int torns;
    private int jugadorActual;
    private boolean finalitzada;
    private Jugador guanyador;

    // Constructor
    public Partida(String seed, Taulell taulell, ArrayList<Jugador> jugadors) {
        this.id = 0;
        this.seed = seed;
        this.taulell = taulell;
        this.jugadors = jugadors;
        this.torns = 0;
        this.jugadorActual = 0;
        this.finalitzada = false;
        this.guanyador = null;
    }

    // Getters
    public Taulell getTaulell() {
        return taulell;
    }

    public ArrayList<Jugador> getJugadors() {
        return jugadors;
    }

    public int getTorns() {
        return torns;
    }

    public int getIndexJugadorActual() {
        return jugadorActual;
    }

    public boolean isFinalitzada() {
        return finalitzada;
    }

    public Jugador getGuanyador() {
        return guanyador;
    }

    public int getId() {
        return id;
    }

    public String getSeed() {
        return seed;
    }

    // Setters
    public void setTaulell(Taulell taulell) {
        this.taulell = taulell;
    }

    public void setJugadors(ArrayList<Jugador> jugadors) {
        this.jugadors = jugadors;
    }

    public void setTorns(int torns) {
        this.torns = torns;
    }

    public void setIndexJugadorActual(int jugadorActual) {
        this.jugadorActual = jugadorActual;
    }

    public void setFinalitzada(boolean finalitzada) {
        this.finalitzada = finalitzada;
    }

    public void setGuanyador(Jugador guanyador) {
        this.guanyador = guanyador;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    // Mètode getJugadorActual
    public Jugador getJugadorActual() {
        if (jugadors == null || jugadors.isEmpty()) return null;
        return jugadors.get(jugadorActual);
    }
}

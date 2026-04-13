package model.core;

import java.util.ArrayList;

import model.entitats.Jugador;

public class Partida {

    // Atributs
    private int id;
    private Taulell taulell;
    private ArrayList<Jugador> jugadors;
    private int torns;
    private int jugadorActual;
    private boolean finalitzada;
    private Jugador guanyador;

    // Constructor
    public Partida(Taulell taulell, ArrayList<Jugador> jugadors) {
        this.id = 0;
        this.taulell = taulell;
        this.jugadors = jugadors;
        this.torns = 0;
        this.jugadorActual = 0;
        this.finalitzada = false;
        this.guanyador = null;

        // Asignar IDs lógicos para usar en arrays
        if (jugadors != null) {
            for (int i = 0; i < jugadors.size(); i++) {
                jugadors.get(i).setIdPartida(i);
            }
        }
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

    // Mètode getJugadorActual
    public Jugador getJugadorActual() {
        if (jugadors == null || jugadors.isEmpty()) return null;
        return jugadors.get(jugadorActual);
    }
}

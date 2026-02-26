package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pingui;
import model.items.BolaNeu;
import model.items.Dau;
import model.items.Item;
import model.items.Peix;

import java.util.ArrayList;
import java.util.Random;

import java.util.Random;

import java.util.Random;

import java.util.ArrayList;
import java.util.Random;

import java.util.ArrayList;
import java.util.Random;

public class Event extends Casella {

    private String[] esdeveniments;

    public Event(int posicio, String[] esdeveniments) {
        super(posicio);
        this.esdeveniments = esdeveniments;
    }

    public String[] getEsdeveniments() { return esdeveniments; }
    public void setEsdeveniments(String[] esdeveniments) { this.esdeveniments = esdeveniments; }

    @Override
    public void realitzarAccio(Partida partida, Jugador jugador) {
        // Només els pingüins poden activar events
        if (!(jugador instanceof Pingui)) return;
        Pingui pingui = (Pingui) jugador;

        // Escollim un event aleatori entre els 4 possibles
        Random random = new Random();
        int index = random.nextInt(4);

        // Obtenim la llista d'items de l'inventari del pingüí
        ArrayList<Item> llista = pingui.getInventari().getLlista();

        switch (index) {
            case 0:
                // EVENT: Obtenir 1 peix (màxim 2 peixos a l'inventari)
                long totalPeixos = llista.stream()
                    .filter(i -> i instanceof Peix)
                    .mapToLong(i -> i.getQuantitat()).sum();
                if (totalPeixos < 2) {
                    llista.add(new Peix("Peix", 1));
                    pingui.getInventari().setLlista(llista);
                    System.out.println(pingui.getNickname() + " ha obtingut 1 peix!");
                } else {
                    System.out.println(pingui.getNickname() + " ja té el màxim de peixos (2).");
                }
                break;

            case 1:
                // EVENT: Obtenir entre 1 i 3 boles de neu (màxim 6 a l'inventari)
                int bolesNoves = random.nextInt(3) + 1;
                long bolesActuals = llista.stream()
                    .filter(i -> i instanceof BolaNeu)
                    .mapToLong(i -> i.getQuantitat()).sum();
                // Calculem quantes boles podem afegir sense superar el màxim
                int bolesAfegir = (int) Math.min(bolesNoves, 6 - bolesActuals);
                if (bolesAfegir > 0) {
                    llista.add(new BolaNeu("Bola de Neu", bolesAfegir));
                    pingui.getInventari().setLlista(llista);
                    System.out.println(pingui.getNickname() + " ha obtingut " + bolesAfegir + " boles de neu!");
                } else {
                    System.out.println(pingui.getNickname() + " ja té el màxim de boles de neu (6).");
                }
                break;

            case 2:
                // EVENT: Obtenir un dau ràpid que avança entre 5 i 10 caselles (màxim 3 daus)
                long dausActuals = llista.stream()
                    .filter(i -> i instanceof Dau)
                    .mapToLong(i -> i.getQuantitat()).sum();
                if (dausActuals < 3) {
                    llista.add(new Dau("Dau ràpid", 5, 10, 1));
                    pingui.getInventari().setLlista(llista);
                    System.out.println(pingui.getNickname() + " ha obtingut un dau ràpid! (5-10 caselles)");
                } else {
                    System.out.println(pingui.getNickname() + " ja té el màxim de daus especials (3).");
                }
                break;

            case 3:
                // EVENT: Obtenir un dau lent que avança entre 1 i 3 caselles (màxim 3 daus)
                long dausActuals2 = llista.stream()
                    .filter(i -> i instanceof Dau)
                    .mapToLong(i -> i.getQuantitat()).sum();
                if (dausActuals2 < 3) {
                    llista.add(new Dau("Dau lent", 1, 3, 1));
                    pingui.getInventari().setLlista(llista);
                    System.out.println(pingui.getNickname() + " ha obtingut un dau lent! (1-3 caselles)");
                } else {
                    System.out.println(pingui.getNickname() + " ja té el màxim de daus especials (3).");
                }
                break;
        }
    }
}
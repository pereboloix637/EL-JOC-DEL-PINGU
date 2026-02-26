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

/** Casella Event: el pingüí rep un ítem aleatori (peix, boles de neu o dau especial). */
public class Event extends Casella {

    // Noms dels events possibles (ús informatiu / futura extensió)
    private String[] esdeveniments;

    // Constructor
    public Event(int posicio, String[] esdeveniments) {
        super(posicio);
        this.esdeveniments = esdeveniments;
    }

    // Getters i Setters
    public String[] getEsdeveniments() { return esdeveniments; }
    public void setEsdeveniments(String[] esdeveniments) { this.esdeveniments = esdeveniments; }

    // Escull un event aleatori entre 4 i l'aplica a l'inventari del pingüí
    @Override
    public void realitzarAccio(Partida partida, Jugador jugador) {
        // Només els pingüins poden activar events
        if (!(jugador instanceof Pingui)) return;
        Pingui pingui = (Pingui) jugador;

        Random random = new Random();
        int index = random.nextInt(4);

        ArrayList<Item> llista = pingui.getInventari().getLlista();

        switch (index) {
            case 0:
                // EVENT 0: 1 peix (màxim 2)
                long totalPeixos = llista.stream()
                        .filter(i -> i instanceof Peix)
                        .mapToLong(Item::getQuantitat).sum();
                if (totalPeixos < 2) {
                    pingui.getInventari().afegirItem(new Peix("Peix", 1));
                    System.out.println(pingui.getNickname() + " ha obtingut 1 peix!");
                } else {
                    System.out.println(pingui.getNickname() + " ja té el màxim de peixos (2).");
                }
                break;

            case 1:
                // EVENT 1: 1-3 boles de neu (màxim 6)
                int bolesNoves = random.nextInt(3) + 1;
                long bolesActuals = llista.stream()
                        .filter(i -> i instanceof BolaNeu)
                        .mapToLong(Item::getQuantitat).sum();
                int bolesAfegir = (int) Math.min(bolesNoves, 6 - bolesActuals);
                if (bolesAfegir > 0) {
                    pingui.getInventari().afegirItem(new BolaNeu("Bola de Neu", bolesAfegir));
                    System.out.println(pingui.getNickname() + " ha obtingut " + bolesAfegir + " boles de neu!");
                } else {
                    System.out.println(pingui.getNickname() + " ja té el màxim de boles de neu (6).");
                }
                break;

            case 2:
                // EVENT 2: dau ràpid 5-10 caselles (màxim 3 daus especials)
                long dausActuals = llista.stream()
                        .filter(i -> i instanceof Dau)
                        .mapToLong(Item::getQuantitat).sum();
                if (dausActuals < 3) {
                    pingui.getInventari().afegirItem(new Dau("Dau ràpid", 5, 10, 1));
                    System.out.println(pingui.getNickname() + " ha obtingut un dau ràpid! (5-10 caselles)");
                } else {
                    System.out.println(pingui.getNickname() + " ja té el màxim de daus especials (3).");
                }
                break;

            case 3:
                // EVENT 3: dau lent 1-3 caselles (màxim 3 daus especials)
                long dausActuals2 = llista.stream()
                        .filter(i -> i instanceof Dau)
                        .mapToLong(Item::getQuantitat).sum();
                if (dausActuals2 < 3) {
                    pingui.getInventari().afegirItem(new Dau("Dau lent", 1, 3, 1));
                    System.out.println(pingui.getNickname() + " ha obtingut un dau lent! (1-3 caselles)");
                } else {
                    System.out.println(pingui.getNickname() + " ja té el màxim de daus especials (3).");
                }
                break;
        }
    }
}
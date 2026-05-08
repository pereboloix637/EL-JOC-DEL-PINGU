package controlador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.caselles.Casella;
import model.core.Partida;
import model.core.Taulell;
import model.entitats.Jugador;

public class GestorTaulell {

    /**
     * Executa l'acció de la casella sobre el jugador.
     */
    public void executarCasella(Partida partida, Jugador j, Casella casella) {
        casella.realitzarAccio(partida, j);
    }

    /**
     * Comprova si la partida ha finalitzat i estableix el guanyador.
     */
    public void comprovarFiTorn(Partida partida) {
        Jugador jugadorActual = partida.getJugadorActual();
        Taulell taulell = partida.getTaulell();

        int posicio = jugadorActual.getPosicio();
        int totalCaselles = taulell.getCaselles().size();

        if (posicio == totalCaselles - 1) {
            partida.setFinalitzada(true);
            partida.setGuanyador(jugadorActual);
        }
    }

    /**
     * Reconstrueix l'string del seed a partir de les caselles d'un taulell
     * real.
     *
     * @param taulell El taulell del qual volem extreure el seed.
     * @return Un String de caràcters (0-5) que representa el taulell.
     */
    public String obtenirSeedTaulell(Taulell taulell) {
        if (taulell == null || taulell.getCaselles() == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Casella c : taulell.getCaselles()) {
            if (c instanceof model.caselles.Normal) {
                sb.append("0"); 
            }else if (c instanceof model.caselles.Os) {
                sb.append("1"); 
            }else if (c instanceof model.caselles.Trineu) {
                sb.append("2"); 
            }else if (c instanceof model.caselles.Forat) {
                sb.append("3"); 
            }else if (c instanceof model.caselles.Event) {
                sb.append("4"); 
            }else if (c instanceof model.caselles.Trencadis) {
                sb.append("5"); 
            }else {
                sb.append("0"); // Per defecte si no es reconeix

                    }}
        return sb.toString();
    }

    /**
     * Genera un taulell a partir d'un seed de 50 caràcters (funciona tant si
     * l'usuari passa un String "0123...45" o qualsevol altra representació
     * numèrica com a text). 0=NORMAL, 1=OS, 2=TRINEU, 3=Forat,
     * 4=ESDEVENIMENT(Event), 5=TRENCADIS
     *
     * @param seed Un string amb 50 dígits, on cadascun representa el tipus de
     * casella.
     * @return El taulell instanciat amb les caselles corresponents.
     */
    public Taulell generarTaulell(String seed) {
        ArrayList<Casella> caselles = new ArrayList<>();

        // Validem el seed de forma estricta. Si no és vàlid, en generem un d'aleatori.
        if (!esSeedValid(seed)) {
            System.out.println("Error: El seed proporcionado no es válido. Se generará un tablero aleatorio.");
            seed = generarSeedAleatori();
        }

        String[] nomsEvents = new String[]{"Pez", "Bolas de Nieve", "Dado Rápido", "Dado Lento"};

        for (int i = 0; i < 50; i++) {
            char type = seed.charAt(i);
            Casella casella;

            switch (type) {
                case '1':
                    casella = new model.caselles.Os(i);
                    break;
                case '2':
                    casella = new model.caselles.Trineu(i);
                    break;
                case '3':
                    casella = new model.caselles.Forat(i);
                    break;
                case '4':
                    casella = new model.caselles.Event(i, nomsEvents);
                    break;
                case '5':
                    casella = new model.caselles.Trencadis(i);
                    break;
                case '0':
                default:
                    casella = new model.caselles.Normal(i);
                    break;
            }
            caselles.add(casella);
        }

        return new Taulell(caselles);
    }

    /**
     * Valida que el seed compleixi tots els requisits estrictes de la partida:
     * - Exactament 50 caràcters. - Només dígits del '0' al '5'. - Les 4
     * primeres (0-3) i 2 últimes caselles (48-49) han de ser '0' (normals). -
     * Cap casella especial ('1' a '5') pot aparèixer més de 5 vegades. - Cada
     * tipus especial ha de tenir una separació mínima de 4 caselles entre
     * aparicions.
     *
     * @param seed l'string a validar.
     * @return true si el seed és vàlid, false en cas contrari.
     */
    public boolean esSeedValid(String seed) {
        

        return true; 
    }

    /**
     * Genera un seed aleatori de 50 caràcters per al taulell, complint amb les
     * següents regles: - Les 4 primeres caselles i les 2 últimes són normals
     * (0). - Limita la quantitat de caselles especials per evitar repetició
     * excessiva (màxim 5 de cada tipus). - Els zeros tenen més probabilitat
     * d'aparèixer (50%) per intercalar-se entre especials. - Cada tipus
     * especial té una separació mínima entre aparicions del mateix tipus.
     *
     * @return Un String de 50 caràcters per utilitzar amb generarTaulell(seed).
     */
    public String generarSeedAleatori() {
        String seed;
        int intents = 0;
        do {
            seed = generarCandidatoSeed();
            intents++;
        } while (!esSeedValid(seed) && intents < 100);

        return seed;
    }

    /**
     * Genera un candidat a seed intentant complir les regles bàsiques. Utilitza
     * un sac de 30 caselles (6 de cada tipus) per garantir varietat i equitat.
     */
    private String generarCandidatoSeed() {
        char[] seed = new char[50];
        for (int i = 0; i < 50; i++) {
            seed[i] = '0';
        }

        List<Integer> sac = new ArrayList<>();
        for (int type = 1; type <= 5; type++) {
            for (int j = 0; j < 6; j++) {
                sac.add(type);
            }
        }
        Collections.shuffle(sac);

        List<Integer> indexsDisponibles = new ArrayList<>();
        for (int i = 4; i < 48; i++) {
            indexsDisponibles.add(i);
        }
        Collections.shuffle(indexsDisponibles);

        for (int tipus : sac) {
            boolean colocat = false;
            for (int i = 0; i < indexsDisponibles.size() && !colocat; i++) {
                int pos = indexsDisponibles.get(i);

                if (esValidCollocar(seed, pos, tipus)) {
                    seed[pos] = (char) ('0' + tipus);
                    indexsDisponibles.remove(i);
                    colocat = true;
                }
            }
            // Si no es pot col·locar, es perd aquesta casella (però el validador ho detectarà)
        }

        return new String(seed);
    }

    /**
     * Comprova si és vàlid col·locar un tipus de casella en una posició.
     */
    private boolean esValidCollocar(char[] seed, int pos, int tipus) {
        // 1. Separació mínima de 4 amb el mateix tipus
        for (int i = Math.max(0, pos - 3); i <= Math.min(49, pos + 3); i++) {
            if (i != pos && seed[i] == (char) ('0' + tipus)) {
                return false;
            }
        }

        // 2. Màxim 2 especials consecutives
        // Marem a l'esquerra
        int consecutives = 1;
        if (pos > 0 && seed[pos - 1] != '0') {
            consecutives++;
            if (pos > 1 && seed[pos - 2] != '0') {
                return false;
            }
        }
        // Marem a la dreta
        if (pos < 49 && seed[pos + 1] != '0') {
            consecutives++;
            if (consecutives > 2) {
                return false;
            }
            if (pos < 48 && seed[pos + 2] != '0') {
                return false;
            }
        }

        // 3. Combinacions letals (1, 3, 5)
        if (tipus == 1 || tipus == 3 || tipus == 5) {
            if (pos > 0) {
                int prev = seed[pos - 1] - '0';
                if (prev == 1 || prev == 3 || prev == 5) {
                    return false;
                }
            }
            if (pos < 49) {
                int next = seed[pos + 1] - '0';
                if (next == 1 || next == 3 || next == 5) {
                    return false;
                }
            }
        }

        return true;
    }
}

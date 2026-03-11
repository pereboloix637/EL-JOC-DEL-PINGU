package controlador;

import java.util.ArrayList;
import java.util.Random;

import model.caselles.Casella;
import model.core.Partida;
import model.core.Taulell;
import model.entitats.Jugador;
import model.entitats.Pinguino;

public class GestorTaulell {

	/**
	 * Executa l'acció de la casella sobre el pingüí.
	 */
	public void executarCasella(Partida partida, Pinguino pingui, Casella casella) {
		casella.realitzarAccio(partida, pingui);
	}

	/**
	 * Comprova si la partida ha finalitzat i estableix el guanyador.
	 */
	public void comprovarFiTorn(Partida partida) {
		Jugador jugadorActual = partida.getJugadorActual();
		Taulell taulell = partida.getTaulell();

		int posicio = jugadorActual.getPosicio();
		int totalCaselles = taulell.getCaselles().size();

		if (posicio >= totalCaselles - 1) {
			partida.setFinalitzada(true);
			partida.setGuanyador(jugadorActual);
		}
	}

    /**
     * Reconstrueix l'string del seed a partir de les caselles d'un taulell real.
     * @param taulell El taulell del qual volem extreure el seed.
     * @return Un String de caràcters (0-5) que representa el taulell.
     */
    public String obtenirSeedTaulell(Taulell taulell) {
        if (taulell == null || taulell.getCaselles() == null) return "";
        
        StringBuilder sb = new StringBuilder();
        for (Casella c : taulell.getCaselles()) {
            if (c instanceof model.caselles.Normal) sb.append("0");
            else if (c instanceof model.caselles.Os) sb.append("1");
            else if (c instanceof model.caselles.Trineu) sb.append("2");
            else if (c instanceof model.caselles.Forat) sb.append("3");
            else if (c instanceof model.caselles.Event) sb.append("4");
            else if (c instanceof model.caselles.Trencadis) sb.append("5");
            else sb.append("0"); // Per defecte si no es reconeix
        }
        return sb.toString();
    }

	/**
	 * Genera un taulell a partir d'un seed de 50 caràcters (funciona tant si l'usuari passa un String
	 * "0123...45" o qualsevol altra representació numèrica com a text).
	 * 0=NORMAL, 1=OS, 2=TRINEU, 3=Forat, 4=ESDEVENIMENT(Event), 5=TRENCADIS
	 *
	 * @param seed Un string amb 50 dígits, on cadascun representa el tipus de casella.
	 * @return El taulell instanciat amb les caselles corresponents.
	 */
	public Taulell generarTaulell(String seed) {
		ArrayList<Casella> caselles = new ArrayList<>();

		// Validem el seed de forma estricta. Si no és vàlid, en generem un d'aleatori.
		if (!esSeedValid(seed)) {
			System.out.println("Error: El seed proporcionat no és vàlid. Es generarà un taulell aleatori.");
			seed = generarSeedAleatori();
		}

		String[] nomsEvents = new String[]{"Peix", "Boles de Neu", "Dau Ràpid", "Dau Lent"};

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
	 * - Exactament 50 caràcters.
	 * - Només dígits del '0' al '5'.
	 * - Les 4 primeres (0-3) i 2 últimes caselles (48-49) han de ser '0' (normals).
	 * - Cap casella especial ('1' a '5') pot aparèixer més de 5 vegades.
	 * - Cada tipus especial ha de tenir una separació mínima de 4 caselles entre aparicions.
	 *
	 * @param seed l'string a validar.
	 * @return true si el seed és vàlid, false en cas contrari.
	 */
	public boolean esSeedValid(String seed) {
	    if (seed == null || seed.length() != 50) {
	        return false;
	    }

	    // Validació posicions inicials i finals
	    for (int i = 0; i < 4; i++) {
	        if (seed.charAt(i) != '0') return false;
	    }
	    for (int i = 48; i < 50; i++) {
	        if (seed.charAt(i) != '0') return false;
	    }

	    // Comptatge de caselles especials i registre de l'última posició de cada tipus
	    int[] comptadors = new int[6];
	    int[] ultimaPosicio = {-10, -10, -10, -10, -10, -10};
	    int separacioMinima = 4;

	    for (int i = 0; i < 50; i++) {
	        char c = seed.charAt(i);

	        // Si no és un caràcter entre '0' i '5' no és vàlid
	        if (c < '0' || c > '5') {
	            return false;
	        }

	        int type = Character.getNumericValue(c);
	        comptadors[type]++;

	        // Validem separació mínima entre especials del mateix tipus
	        if (type != 0) {
	            if ((i - ultimaPosicio[type]) < separacioMinima) {
	                return false; // Dos especials del mateix tipus massa propers
	            }
	            ultimaPosicio[type] = i;
	        }
	    }

	    // Validem que cap comptador especial passi del límit (5)
	    for (int i = 1; i <= 5; i++) {
	        if (comptadors[i] > 5) {
	            return false; // Massa caselles repetides d'aquest tipus
	        }
	    }

	    return true; // Ha passat totes les validacions
	}

	/**
	 * Genera un seed aleatori de 50 caràcters per al taulell, complint amb les següents regles:
	 * - Les 4 primeres caselles i les 2 últimes són normals (0).
	 * - Limita la quantitat de caselles especials per evitar repetició excessiva (màxim 5 de cada tipus).
	 * - Els zeros tenen més probabilitat d'aparèixer (50%) per intercalar-se entre especials.
	 * - Cada tipus especial té una separació mínima entre aparicions del mateix tipus.
	 *
	 * @return Un String de 50 caràcters per utilitzar amb generarTaulell(seed).
	 */
	public String generarSeedAleatori() {
	    StringBuilder seed = new StringBuilder(50);
	    Random random = new Random();

	    // Comptadors màxims per evitar que hi hagi moltes d'un mateix tipus repetides
	    int maxOs = 5;
	    int maxTrineu = 5;
	    int maxForat = 5;
	    int maxEvent = 5;
	    int maxTrencadis = 5;

	    // Registre de l'última posició on ha aparegut cada tipus especial.
	    // Inicialitzat a -10 per permetre que qualsevol tipus pugui aparèixer des del principi.
	    // Índex: 0=normal, 1=os, 2=trineu, 3=forat, 4=event, 5=trencadís
	    int[] ultimaPosicio = {-10, -10, -10, -10, -10, -10};

	    // Nombre mínim de caselles que han de passar entre dos especials del mateix tipus
	    int separacioMinima = 4;

	    for (int i = 0; i < 50; i++) {

	        // Les primeres 4 i les 2 últimes posicions sempre són caselles normals
	        if (i < 4 || i >= 48) {
	            seed.append('0');
	            continue;
	        }

	        boolean afegit = false;
	        while (!afegit) {

	            // Es genera un número entre 0 i 9.
	            // Els valors 0-4 (5 opcions) corresponen al tipus normal → 50% de probabilitat.
	            // Els valors 5-9 (1 opció cada un) corresponen als 5 tipus especials → 10% cadascun.
	            int roll = random.nextInt(10);
	            int type = (roll < 5) ? 0 : (roll - 4);

	            switch (type) {
	                case 0:
	                    // Casella normal: sempre s'afegeix sense restriccions
	                    seed.append('0');
	                    afegit = true;
	                    break;

	                case 1:
	                    // Casella os: comprova que no s'hagi superat el màxim
	                    // i que hagin passat prou caselles des de l'última aparició
	                    if (maxOs > 0 && (i - ultimaPosicio[1]) >= separacioMinima) {
	                        seed.append('1');
	                        maxOs--;
	                        ultimaPosicio[1] = i;
	                        afegit = true;
	                    }
	                    break;

	                case 2:
	                    // Casella trineu: mateixa lògica que l'os
	                    if (maxTrineu > 0 && (i - ultimaPosicio[2]) >= separacioMinima) {
	                        seed.append('2');
	                        maxTrineu--;
	                        ultimaPosicio[2] = i;
	                        afegit = true;
	                    }
	                    break;

	                case 3:
	                    // Casella forat: mateixa lògica que l'os
	                    if (maxForat > 0 && (i - ultimaPosicio[3]) >= separacioMinima) {
	                        seed.append('3');
	                        maxForat--;
	                        ultimaPosicio[3] = i;
	                        afegit = true;
	                    }
	                    break;

	                case 4:
	                    // Casella event: mateixa lògica que l'os
	                    if (maxEvent > 0 && (i - ultimaPosicio[4]) >= separacioMinima) {
	                        seed.append('4');
	                        maxEvent--;
	                        ultimaPosicio[4] = i;
	                        afegit = true;
	                    }
	                    break;

	                case 5:
	                    // Casella trencadís: mateixa lògica que l'os
	                    if (maxTrencadis > 0 && (i - ultimaPosicio[5]) >= separacioMinima) {
	                        seed.append('5');
	                        maxTrencadis--;
	                        ultimaPosicio[5] = i;
	                        afegit = true;
	                    }
	                    break;
	            }
	            // Si la casella especial no compleix les condicions (màxim superat o massa propera),
	            // el bucle torna a intentar-ho amb un nou tipus aleatori
	        }
	    }

	    return seed.toString();
	}
}

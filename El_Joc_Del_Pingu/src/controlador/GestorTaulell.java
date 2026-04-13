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
	    int casellesEspecialsConsecutives = 0;

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
	            
	            casellesEspecialsConsecutives++;
	            if (casellesEspecialsConsecutives > 2) {
	                return false; // Mes de 2 caselles especials consecutives
	            }
	            
	            // Validem combinacions letals: no es poden posar 1(Os), 3(Forat), 5(Trencadis) junts
	            if (i > 0) {
	                int prevType = Character.getNumericValue(seed.charAt(i - 1));
	                if ((type == 1 || type == 3 || type == 5) && (prevType == 1 || prevType == 3 || prevType == 5)) {
	                    return false;
	                }
	            }
	        } else {
	            casellesEspecialsConsecutives = 0;
	        }
	    }

	    // Validem que cap comptador especial passi del límit (5)
	    // I que es compleixin els mínims demanats: 1:Os(2), 2:Trineu(2), 3:Forat(2), 4:Event(4), 5:Trencadis(2)
	    if (comptadors[1] < 2) return false;
	    if (comptadors[2] < 2) return false;
	    if (comptadors[3] < 2) return false;
	    if (comptadors[4] < 4) return false;
	    if (comptadors[5] < 2) return false;

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
	    String seed;
	    int intents = 0;
	    do {
	        seed = generarCandidatoSeed();
	        intents++;
	    } while (!esSeedValid(seed) && intents < 100);
	    
	    return seed;
	}

	/**
	 * Genera un candidat a seed intentant complir les regles bàsiques.
	 */
	private String generarCandidatoSeed() {
	    StringBuilder seed = new StringBuilder(50);
	    Random random = new Random();

	    int[] comptadorsEspecial = new int[6]; // 1-5
	    int[] ultimaPosicio = {-10, -10, -10, -10, -10, -10};
	    int separacioMinima = 4;
	    int casellesEspecialsConsecutives = 0;

	    for (int i = 0; i < 50; i++) {
	        if (i < 4 || i >= 48) {
	            seed.append('0');
	            casellesEspecialsConsecutives = 0;
	        } else {
	            boolean afegit = false;
	            int intentsCasella = 0;
	            while (!afegit && intentsCasella < 30) {
	                intentsCasella++;
	                int roll = random.nextInt(100);
	                int type = 0;

	                // Zones de dificultat
	                if (i < 16) {
	                    // Zona 1: Mes fàcil, més trineus i events
	                    if (roll < 45) type = 0;
	                    else if (roll < 50) type = 1; // 5%
	                    else if (roll < 65) type = 2; // 15%
	                    else if (roll < 70) type = 3; // 5%
	                    else if (roll < 85) type = 4; // 15%
	                    else type = 5; // 15%
	                } else if (i < 36) {
	                    // Zona 2: Probabilitats estàndard
	                    if (roll < 40) type = 0;
	                    else if (roll < 52) type = 1;
	                    else if (roll < 64) type = 2;
	                    else if (roll < 76) type = 3;
	                    else if (roll < 88) type = 4;
	                    else type = 5;
	                } else {
	                    // Zona 3: Final difícil, més ossos i forats
	                    if (roll < 35) type = 0;
	                    else if (roll < 50) type = 1; // 15%
	                    else if (roll < 60) type = 2; // 10%
	                    else if (roll < 75) type = 3; // 15%
	                    else if (roll < 85) type = 4; // 10%
	                    else type = 5; // 15%
	                }

	                // Limitem caselles consecutives per forçar una normal
	                if (casellesEspecialsConsecutives >= 2) {
	                    type = 0;
	                } else if (type == 1 || type == 3 || type == 5) {
	                    // Evitar combinacions letals (1, 3, 5 juntos)
	                    if (i > 0) {
	                        int prevType = Character.getNumericValue(seed.charAt(i - 1));
	                        if (prevType == 1 || prevType == 3 || prevType == 5) {
	                            continue; // Tornem a fer roll
	                        }
	                    }
	                }

	                if (type == 0) {
	                    seed.append('0');
	                    casellesEspecialsConsecutives = 0;
	                    afegit = true;
	                } else {
	                    // Comprovar límits (màxim 5) i separació
	                    if (comptadorsEspecial[type] < 5 && (i - ultimaPosicio[type]) >= separacioMinima) {
	                        seed.append(type);
	                        comptadorsEspecial[type]++;
	                        ultimaPosicio[type] = i;
	                        casellesEspecialsConsecutives++;
	                        afegit = true;
	                    }
	                }
	            }
	            // Si no s'ha pogut afegir res després de molts intents, posem una normal
	            if (!afegit) {
	                seed.append('0');
	                casellesEspecialsConsecutives = 0;
	            }
	        }
	    }
	    return seed.toString();
	}
}

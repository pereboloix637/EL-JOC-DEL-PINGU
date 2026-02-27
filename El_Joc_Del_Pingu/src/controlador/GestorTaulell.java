package controlador;

import java.util.ArrayList;
import java.util.Random;

import model.caselles.Casella;
import model.core.Partida;
import model.core.Taulell;
import model.entitats.Jugador;
import model.entitats.Pingui;

public class GestorTaulell {

	/**
	 * Executa l'acció de la casella sobre el pingüí.
	 */
	public void executarCasella(Partida partida, Pingui pingui, Casella casella) {
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
	 * Genera un taulell a partir d'un seed de 50 caràcters (funciona tant si l'usuari passa un String
	 * "0123...45" o qualsevol altra representació numèrica com a text).
	 * 0=NORMAL, 1=OS, 2=TRINEU, 3=AIGUERO(Forat), 4=ESDEVENIMENT(Event), 5=TRENCADIS
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

		// Comptatge de caselles especials
		int[] comptadors = new int[6]; // Índexos 0 a 5 per a cada tipus

		for (int i = 0; i < 50; i++) {
			char c = seed.charAt(i);
			
			// Si no és un caràcter entre '0' i '5' no és vàlid
			if (c < '0' || c > '5') {
				return false;
			}
			
			int type = Character.getNumericValue(c);
			comptadors[type]++;
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

		for (int i = 0; i < 50; i++) {
			if (i < 4 || i >= 48) {
				// Les primeres 4 i les 2 últimes obligatòriament són normals
				seed.append('0');
			} else {
				boolean afegit = false;
				while (!afegit) {
					int type = random.nextInt(6); // de 0 a 5

					switch (type) {
						case 0:
							seed.append('0');
							afegit = true;
							break;
						case 1:
							if (maxOs > 0) {
								seed.append('1');
								maxOs--;
								afegit = true;
							}
							break;
						case 2:
							if (maxTrineu > 0) {
								seed.append('2');
								maxTrineu--;
								afegit = true;
							}
							break;
						case 3:
							if (maxForat > 0) {
								seed.append('3');
								maxForat--;
								afegit = true;
							}
							break;
						case 4:
							if (maxEvent > 0) {
								seed.append('4');
								maxEvent--;
								afegit = true;
							}
							break;
						case 5:
							if (maxTrencadis > 0) {
								seed.append('5');
								maxTrencadis--;
								afegit = true;
							}
							break;
					}
				}
			}
		}

		return seed.toString();
	}
}

package model.entitats;

import model.items.Peix;
import model.items.BolaNeu;
import model.items.Dau;
import model.caselles.Casella;
import model.caselles.Forat;
import model.core.Partida;


public class Foca extends Jugador {
/// ATRIBUTS
	private boolean soborno;
	private int bloqueix;

/// CONSTRUCTOR
	public Foca(String nickname, String color, boolean soborno, int bloqueix) {
		super(nickname, color); // LLAMADA OBLIGATORIA
		this.soborno = soborno;
		this.bloqueix = bloqueix;
	}

/// GETTERS I SETTERS
	public boolean isSoborno() {
		return soborno;
	}

	public void setSoborno(boolean soborno) {
		this.soborno = soborno;
	}

	public int getBloqueix() {
		return bloqueix;
	}

	public void setBloqueix(int bloqueix) {
		this.bloqueix = bloqueix;
	}

/// METODES
	public void aplastarPingu(Pingui p) { // La Foca aplasta al Pingui elegit

		int bolesB = p.getInventari().getBoles();
		int bolesP = p.getInventari().getPeixos();
		int bolesD = p.getInventari().getDausEspecials();

		// Eliminem totes les boles del aplastat (pingu)
		for (int i = 0; i < bolesB; i++) {
			p.getInventari().eliminarItemsPerTipus(BolaNeu.class);
		}

		// Eliminem tots els peixos del aplastat (pingu)
		for (int i = 0; i < bolesP; i++) {
			p.getInventari().eliminarItemsPerTipus(Peix.class);
			;
		}

		// Eliminem tots els daus especials del aplastat (pingu)
		for (int i = 0; i < bolesD; i++) {
			p.getInventari().eliminarItemsPerTipus(Dau.class);
		}
	}

	public void pegarPingu(Pingui jugador, Partida partida) { // La Foca atacara al Pingui elegit
	    if (this.soborno == false) {
	        System.out.println("Accion denegada, la foca no ha siguit soboronada");
	    } else if (jugador.getInventari().getPeixos() >= 1) {
	    	
	        // Si el jugador te un peix, la pot alimentar per que quedi bloquejada (2 torns)
	        this.bloqueix = 2;
	        System.out.println("La foca ha sido alimentada y queda bloqueada " + this.bloqueix + " turnos.");
	        
	        // I se li treu un "Peix" al Jugador/Pingui
	        jugador.getInventari().eliminarItemsPerTipus(Peix.class);
	        
	    } else {
	        // Si no te peixos, la Foca el pega, portant-lo a un forat anterior.
	        int posActual = jugador.getPosicio();
	        int foratAnterior = -1;
	        Casella casellaDestino = null;

	        for (Casella casella : partida.getTaulell().getCaselles()) {
	            if (casella instanceof Forat && casella.getPosicio() < posActual) {
	                if (casella.getPosicio() > foratAnterior) {
	                    foratAnterior = casella.getPosicio();
	                    casellaDestino = casella;
	                }
	            }
	        }

	        if (casellaDestino != null) {
	            int desplazamiento = casellaDestino.getPosicio() - posActual; // Calculem la diferencia
	            jugador.setPosicio(desplazamiento); // Llavors movem al jugador on tingui que estar
	            System.out.println("El jugador no tenía peixos, ha sido enviado al agujero anterior.");
	        } else {
	            System.out.println("El jugador no tenía peixos, pero no hay agujero anterior.");
	        }
	    }
	}
	
	public void sobornarFoca(Pingui p) { // Permet sobornar a la Foca
// Si el jugador te suficients peixos, es sobornada, sino no fara res i mostrara un missatje d'error.
		if (p.getInventari().getPeixos() >= 1) {
			soborno = true;
			p.getInventari().eliminarItemsPerTipus(Peix.class); // Se li resta un peix.
		} else {
			System.out.println("SOBORNO DENEGAT: Peixos insuficients");
		}
	}

}

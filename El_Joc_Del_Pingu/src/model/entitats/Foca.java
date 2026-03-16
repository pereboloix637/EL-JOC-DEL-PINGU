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
	// CONSTRUCTOR AMB TOTS ELS PARAMETRES
	public Foca(String nickname, String color, boolean soborno, int bloqueix) {
		super(nickname, color); // LLAMADA OBLIGATORIA
		this.soborno = soborno;
		this.bloqueix = bloqueix;
	}
	
	// CONSTRUCTOR QUE COLOCA LA FOCA SENSE SOBORNAR I BLOQUEJAR
	public Foca(String nickname, String color) {
		super(nickname, color); // LLAMADA OBLIGATORIA
		this.soborno = false;
		this.bloqueix = 0;
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
	public void aplastarPingu(Pinguino p) { // La Foca aplasta al Pingui elegit

		
		int itemB = p.getInventari().getBoles();
		int itemP = p.getInventari().getPeixos();
		int itemD = p.getInventari().getDausEspecials();
		System.out.println(p.getNickname() + " serà aixafat/aixafada i perdrà els següents ítems:");
		
		// Eliminem totes les boles del aplastat (pingu)
		for (int i = 0; i < itemB; i++) {
			p.getInventari().eliminarItemsPerTipus(BolaNeu.class);
		}

		// Eliminem tots els peixos del aplastat (pingu)
		for (int i = 0; i < itemP; i++) {
			p.getInventari().eliminarItemsPerTipus(Peix.class);
		}

		// Eliminem tots els daus especials del aplastat (pingu)
		for (int i = 0; i < itemD; i++) {
			p.getInventari().eliminarItemsPerTipus(Dau.class);
		}
		
        System.out.println("=====================================================");

	}

	public void pegarPingu(Pinguino jugador, Partida partida) { // La Foca atacara al Pingui elegit
	    if (this.soborno == false) {
	        System.out.println("Acció denegada, la foca no ha estat subornada");
	    } else if (jugador.getInventari().getPeixos() >= 1) {
	    	
	    	// Ask the user if they want to use the Fish using a JavaFX Alert
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
			alert.setTitle("Trobada amb la Foca!");
			alert.setHeaderText("La foca vol atacar-te...");
			alert.setContentText("Tens un peix a l'inventari. Vols usar-lo per alimentar la foca i bloquejar-la?");

			javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Sí, alimentar Foca");
			javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No, fugir");
			alert.getButtonTypes().setAll(btnYes, btnNo);

			java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
			
			if (result.isPresent() && result.get() == btnYes) {
		        // Si el jugador decide usar el pez, la puede alimentar per que quedi bloquejada (2 torns)
		        this.bloqueix = 2;
		        
		        System.out.println("La foca ha estat alimentada i queda bloquejada per " + this.bloqueix + " torns.");
		        System.out.println("L'usuari " + jugador.getNickname() + " alimenta la foca i perd 1 peix.");

		        // I se li treu un "Peix" al Jugador/Pingui
		        jugador.getInventari().eliminarItemsPerTipus(Peix.class);
			} else {
				// Si no vol usar el peix, la foca ataca
				aplicarPegarPingu(jugador, partida);
			}
	        
	    } else {
	        // Si no te peixos, la Foca el pega, portant-lo a un forat anterior.
	    	aplicarPegarPingu(jugador, partida);
	    }
	}
	
	private void aplicarPegarPingu(Pinguino jugador, Partida partida) {
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
	            System.out.println("El jugador no tenia peixos, ha estat enviat al forat anterior.");
	        } else {
	            System.out.println("El jugador no tenia peixos, però no hi ha cap forat anterior.");
	        }
	}
	
	public void sobornarFoca(Pinguino p) { // Permet sobornar a la Foca
// Si el jugador te suficients peixos, es sobornada, sino no fara res i mostrara un missatje d'error.
		if (p.getInventari().getPeixos() >= 1) {
			System.out.println(p.getNickname() + " ha sobornat a la foca " + this.getNickname());
			soborno = true;
			p.getInventari().eliminarItemsPerTipus(Peix.class); // Se li resta un peix.
		} else {
			System.out.println("SOBORNO DENEGAT: Peixos insuficients");
		}
	}

}

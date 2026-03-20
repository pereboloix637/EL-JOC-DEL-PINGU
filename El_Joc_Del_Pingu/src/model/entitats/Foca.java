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
		// Si NO ha sido sobornada y NO está bloqueada, ataca
		if (!this.soborno && this.bloqueix == 0) {
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
	}

	public void pegarPingu(Pinguino jugador, Partida partida) { // La Foca atacara al Pingui elegit
		// Si ha sido sobornada o está bloqueada, no ataca
		if (this.soborno || this.bloqueix > 0) {
			System.out.println("La foca " + this.getNickname() + " está tranquila o bloqueada.");
			return;
		}

		// Si no está tranquila, ataca directamente
		aplicarPegarPingu(jugador, partida);
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
	            jugador.setPosicio(casellaDestino.getPosicio()); // Llavors movem al jugador on tingui que estar (posició absoluta)
	            System.out.println("El jugador no tenia peixos, ha estat enviat al forat anterior.");
	        } else {
	            System.out.println("El jugador no tenia peixos, però no hi ha cap forat anterior.");
	        }
	}
	
	public void sobornarFoca(Pinguino p) { // Permet sobornar a la Foca
		// Si ha sido alimentada recientemente, no volvemos a preguntar
		if (this.bloqueix > 0 || this.soborno) return;

		if (p.getInventari().getPeixos() >= 1) {
			// Preguntamos al usuario si quiere usar el pez para sobornar/alimentar
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
			vista.PantallaJuego.estilarAlerta(alert); // Aplicamos el estilo polar
			alert.setTitle("Trobada amb la Foca!");
			alert.setHeaderText("¡Una foca obstaculiza el camino!");
			alert.setContentText("Tens un peix a l'inventari. Vols usar-lo para alimentar la foca?");

			javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Sí, alimentar (2 torns)");
			javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No, arriscar-se");
			alert.getButtonTypes().setAll(btnYes, btnNo);

			java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
			
			if (result.isPresent() && result.get() == btnYes) {
				System.out.println(p.getNickname() + " ha alimentado a la foca " + this.getNickname());
				this.soborno = true;
				this.bloqueix = 2; // Queda bloqueada por 2 turnos
				p.getInventari().eliminarItemsPerTipus(Peix.class); // Se li resta un peix
			}
		} else {
			System.out.println("No tens peixos per alimentar la foca.");
		}
	}

	/**
	 * Decideix aleatòriament si la foca colpeja (pegar) o aixafa (aplastar) el pingüí.
	 * Només triarà aixafar si el pingüí té ítems a l'inventari.
	 */
	public void decidirAccion(Pinguino p, Partida partida) {
		if (this.soborno || this.bloqueix > 0) return;

		boolean tieneItems = p.getInventari().getBoles() > 0 || 
		                     p.getInventari().getPeixos() > 0 || 
		                     p.getInventari().getDausEspecials() > 0;

		java.util.Random rand = new java.util.Random();
		
		// Si té ítems, tria 50/50. Si no, només pot pegar.
		if (tieneItems && rand.nextBoolean()) {
			aplastarPingu(p);
		} else {
			pegarPingu(p, partida);
		}
	}

}

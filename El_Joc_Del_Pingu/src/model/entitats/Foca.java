package model.entitats;

import model.items.Peix;
import model.items.BolaNeu;
import model.items.Dau;
import model.items.Item;
import model.caselles.Casella;
import model.caselles.Forat;
import model.core.Partida;

public class Foca extends Jugador {

	private boolean soborno;
	private int[] sobornosJugadores = new int[10]; // Turnos de seguridad para cada ID del 0 al 9
	private model.items.Inventari inventari;

	public Foca(String nickname, String color, boolean soborno, int bloqueix) {
		super(nickname, color); 
		this.soborno = soborno;
		this.setTornsBloquejat(bloqueix);
		this.inventari = new model.items.Inventari();
	}
	
	public Foca(String nickname, String color) {
		super(nickname, color);
		this.soborno = false;
		this.setTornsBloquejat(0);
		this.inventari = new model.items.Inventari();
	}

	public boolean isSoborno() {
		return soborno;
	}
	public void setSoborno(boolean soborno) {
		this.soborno = soborno;
	}
	public int getBloqueix() {
		return this.getTornsBloquejat();
	}
	public void setBloqueix(int bloqueix) {
		this.setTornsBloquejat(bloqueix);
	}
	public model.items.Inventari getInventari() {
		return inventari;
	}

	// Gestión temporal de objetos análoga a la del Pingüino
	public void usarItem(model.items.Item i) {
		inventari.usarItem(i);
	}
	public void agregarItem(model.items.Item i) {
		inventari.afegirItem(i);
	}
	public void retirarItem(model.items.Item i) {
		inventari.eliminarItem(i);
	}

	public void aplastarPingu(Pinguino p) {
		if (p.getIdPartida() < sobornosJugadores.length && sobornosJugadores[p.getIdPartida()] <= 0 && getBloqueix() == 0) {
			int deleteBoles = p.getInventari().getBoles() / 2;
			int deletePeixos = p.getInventari().getPeixos() / 2;
			int deleteDaus = p.getInventari().getDausEspecials() / 2;
			
			System.out.println("¡A " + p.getNickname() + " le van a quitar la mitad del inventario!");
			if (deleteBoles > 0) {
				p.getInventari().retirarQuantitat(BolaNeu.class, deleteBoles);
			}
			if (deletePeixos > 0) {
				p.getInventari().retirarQuantitat(Peix.class, deletePeixos);
			}
			if (deleteDaus > 0) {
				p.getInventari().retirarQuantitat(model.items.Dau.class, deleteDaus);
			}

			vista.PantallaJuego.registrarEventoEstatico("¡Foca aplasta a " + p.getNickname() + " y le roba la mitad de los ítems!", "log-warning");
		}
	}

	public void pegarPingu(Pinguino jugador, Partida partida) {
		if ((jugador.getIdPartida() < sobornosJugadores.length && sobornosJugadores[jugador.getIdPartida()] > 0) || getBloqueix() > 0) {
			System.out.println("La foca " + this.getNickname() + " está tranquila para " + jugador.getNickname());
		} else {
			aplicarPegarPingu(jugador, partida);
		}
	}

	private void aplicarPegarPingu(Pinguino jugador, Partida partida) {
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
			jugador.setPosicio(casellaDestino.getPosicio()); 
			vista.PantallaJuego.registrarEventoEstatico("¡" + jugador.getNickname() + " ha sido golpeado por Foca y enviado al agujero anterior!", "log-warning");
		} else {
			vista.PantallaJuego.registrarEventoEstatico("¡La foca golpeó a " + jugador.getNickname() + " pero no hay agujero donde caer!", "log-info");
		}
	}

	public void sobornarFoca(Pinguino p) {
		if (!((Object)p instanceof Foca) && this.getBloqueix() <= 0) {
			if (p.getInventari().getPeixos() >= 1) {
				javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
				vista.PantallaJuego.estilarAlerta(alert); 
				alert.setTitle("¡Encuentro con la foca!");
				alert.setHeaderText("¡Una foca te corta el paso!");
				alert.setContentText("Tienes un pez. ¿Quieres usarlo para alimentar a la foca?");
	
				javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Sí, alimentar (2 turnos)");
				javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No, arriesgarse");
				alert.getButtonTypes().setAll(btnYes, btnNo);
	
				java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
	
				if (result.isPresent() && result.get() == btnYes) {
					if (p.getIdPartida() < sobornosJugadores.length) {
						sobornosJugadores[p.getIdPartida()] = 2; 
					}
					this.soborno = true;
					p.getInventari().eliminarItemsPerTipus(model.items.Peix.class);
					vista.PantallaJuego.registrarEventoEstatico(p.getNickname() + " ha alimentado a la foca. ¡A salvo 2 turnos!", "log-info");
				}
			}
		}
	}

	public void actualizarSobornos() {
		boolean algunSobornoActivo = false;
		for (int i = 0; i < sobornosJugadores.length; i++) {
			if (sobornosJugadores[i] > 0) {
				sobornosJugadores[i]--;
				if (sobornosJugadores[i] > 0) {
					algunSobornoActivo = true;
				}
			}
		}
		this.soborno = algunSobornoActivo;
	}

	public void reaccionarAOs(Partida partida) {
		java.util.Random rand = new java.util.Random();
		if (rand.nextInt(100) < 50) {
			vista.PantallaJuego.registrarEventoEstatico("¡La foca " + this.getNickname() + " esquiva al oso!", "log-info");
		} else {
			this.setPosicio(0);
			vista.PantallaJuego.registrarEventoEstatico("¡La foca " + this.getNickname() + " ha sido cazada por un oso y vuelve al inicio!", "log-warning");
		}
	}

	// =========================================================
	// INTELIGENCIA ARTIFICIAL DE LA FOCA DURANTE SU TURNO
	// =========================================================
	/**
	 * Evalúa el estado del tablero al iniciar el turno de la Foca y decide si debe usar un objeto 
	 * táctico de su inventario (Dado rápido para avanzar, Dado lento o Bola de nieve para atacar).
	 * Su prioridad número 1 es ganar, así que intentará avanzar rápidamente si se queda atrás.
	 * Su prioridad secundaria es molestar a los jugadores que estén cerca o desprotegidos.
	 * 
	 * @param partida La partida actual para obtener la distancia y posición de los rivales.
	 * @return Un objeto Dau (Dado Especial) si la IA decide usarlo, o null si usará el dado normal.
	 */
	public Dau jugarTurnoTactico(Partida partida) {
		actualizarSobornos(); // Refrescar los turnos de protección antes de pensar
		
		if (this.getTornsBloquejat() > 0) {
			return null; // Si ella misma está bloqueada, no hace nada
		}
		
		int maxDistanciaLider = 0;
		Pinguino elMasCercano = null;
		int minDistanciaCercano = 999;
		
		// 1. Escaneo del entorno táctico
		for (Jugador j : partida.getJugadors()) {
			if (j instanceof Pinguino) {
				Pinguino ping = (Pinguino) j;

				// Descartar a jugadores protegidos por el arreglo de sobornos
				if (!(ping.getIdPartida() < sobornosJugadores.length && sobornosJugadores[ping.getIdPartida()] > 0)) {
					if (ping.getPosicio() > maxDistanciaLider) {
						maxDistanciaLider = ping.getPosicio();
					}

					// Calcula la distancia hacia adelante (sólo objetivos frente a la foca)
					int dist = ping.getPosicio() - this.getPosicio();
					if (dist > 0 && dist < minDistanciaCercano) {
						minDistanciaCercano = dist;
						elMasCercano = ping;
					}
				}
			}
		}
		
		// 2. Modo: Acelerar (Prioridad Ganar)
		// Si el líder está sacando demasiada ventaja (>10 casillas), gastamos inventario para atrapar
		if (this.getPosicio() < maxDistanciaLider - 10 && inventari.getDausEspecials() > 0) {
			for(Item item : inventari.getLlista()) {
				if(item instanceof Dau && ((Dau)item).getNom().equals("Dado Rápido")) {
					Dau d = (Dau) item;
					// [BUGFIX] No usamos el ítem aquí (inventari.usarItem), ya que PantallaJuego 
					// y GestorPartida lo tirarán y consumirán después. Si lo hacemos aquí,
					// se resta 2 veces y la foca acaba tirando un dado normal al quedarse a 0.
					vista.PantallaJuego.registrarEventoEstatico("La Foca ha activado un Dado Rápido para ganar ventaja.", "log-warning");
					return d;
				}
			}
		}
		
		// 3. Modo: Embestida (Prioridad Atacar de cerca)
		// Si un objetivo clave está muy cerca (a tiro de piedra, max 3 casillas), forzamos aterrizaje con dado lento
		if (minDistanciaCercano > 0 && minDistanciaCercano <= 3 && inventari.getDausEspecials() > 0) {
			for(Item item : inventari.getLlista()) {
				if(item instanceof Dau && ((Dau)item).getNom().equals("Dado Lento")) {
					Dau d = (Dau) item;
					// [BUGFIX] Idem que arriba: el consumo se delega al GestorPartida.tirarDau
					vista.PantallaJuego.registrarEventoEstatico("¡La Foca activa un Dado Lento para acechar a " + elMasCercano.getNickname() + "!", "log-warning");
					return d;
				}
			}
		}
		
		// 4. Modo: Ataque a distancia (Prioridad Fastidiar)
		// Lanza hielo al jugador más cercano en su rango sin piedad
		if (minDistanciaCercano > 0 && minDistanciaCercano <= 4 && inventari.getBoles() > 0 && elMasCercano != null) {
			inventari.retirarQuantitat(BolaNeu.class, 1);
			elMasCercano.mourePosicio(-2); // Efecto básico bola nieve en retroceso
			vista.PantallaJuego.registrarEventoEstatico("¡La foca lanza Bola de Nieve a " + elMasCercano.getNickname() + "!", "log-warning");
		}
		
		// Si no valía la pena nada, tirará el dado estándar.
		return null; 
	}
}

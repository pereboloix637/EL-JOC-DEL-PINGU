package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.Peix;

/** Casella Os: l'ós ataca el jugador. El pingüí es pot defensar amb un peix. */
public class Os extends Casella {

	// Constructor
	public Os(int posicio) {
		super(posicio);
	}

	// Si el pingüí té un peix el gasta; sense peix torna a l'inici
	@Override
	public void realitzarAccio(Partida partida, Jugador jugador) {
		if (!(jugador instanceof Pinguino))
			return;
		Pinguino pingui = (Pinguino) jugador;

		vista.PantallaJuego.mostrarAtaqueOso(this.getPosicio());

		Peix peix = (Peix) pingui.getInventari().getLlista().stream()
				.filter(i -> i instanceof Peix && i.getQuantitat() > 0).findFirst().orElse(null);

		if (peix != null) {
			// Ask the user if they want to use the Fish using a JavaFX Alert
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
			alert.setTitle("¡Ataque del Oso!");
			alert.setHeaderText(pingui.getNickname() + " ha sido atacado por un oso.");
			alert.setContentText("Tienes un pez en el inventario. ¿Quieres usarlo para distraer al oso y salvarte?");

			javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Sí, usar Pez");
			javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No, guardar Pez");
			alert.getButtonTypes().setAll(btnYes, btnNo);

			// Estilem l'alerta
			vista.PantallaJuego.estilarAlerta(alert);

			java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
			
			if (result.isPresent() && result.get() == btnYes) {
				pingui.getInventari().usarItem(peix);
				System.out.println(pingui.getNickname() + " ¡ha sido atacado por un oso! Pero tenía un pez y se ha salvado.");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ¡ha sido atacado por un oso, pero se ha salvado gracias a un pez!", "log-warning");
			} else {
				pingui.setPosicio(0);
				System.out.println(pingui.getNickname() + " ¡ha sido atacado por un oso! Vuelve al inicio.");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ¡ha sido atacado por un oso y vuelve al inicio!", "log-warning");
			}
		} else {
			pingui.setPosicio(0);
			System.out.println(pingui.getNickname() + " ¡ha sido atacado por un oso! Vuelve al inicio.");
			vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ¡ha sido atacado por un oso y vuelve al inicio!", "log-warning");
		}
	}
}

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
			alert.setTitle("Atac de l'Os!");
			alert.setHeaderText(pingui.getNickname() + " ha estat atacat per un ós.");
			alert.setContentText("Tens un peix a l'inventari. Vols usar-lo per distraure l'ós i salvar-te?");

			javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Sí, usar Peix");
			javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No, guardar Peix");
			alert.getButtonTypes().setAll(btnYes, btnNo);

			// Estilem l'alerta
			vista.PantallaJuego.estilarAlerta(alert);

			java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
			
			if (result.isPresent() && result.get() == btnYes) {
				pingui.getInventari().usarItem(peix);
				System.out.println(pingui.getNickname() + " ha estat atacat per un ós! Però tenia un peix i s'ha salvat.");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha estat atacat per un ós, però s'ha salvat gràcies a un peix!", "log-warning");
			} else {
				pingui.setPosicio(0);
				System.out.println(pingui.getNickname() + " ha estat atacat per un ós! Torna a l'inici.");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha estat atacat per un ós i torna a l'inici!", "log-warning");
			}
		} else {
			pingui.setPosicio(0);
			System.out.println(pingui.getNickname() + " ha estat atacat per un ós! Torna a l'inici.");
			vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha estat atacat per un ós i torna a l'inici!", "log-warning");
		}
	}
}

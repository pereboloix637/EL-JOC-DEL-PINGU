package model.caselles;

import model.core.Partida;
import model.entitats.Foca;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.Peix;
import java.util.Random;

/** Casella Os: l'ós ataca el jugador. El pingüí es pot defensar amb un peix. */
public class Os extends Casella {

	// Constructor
	public Os(int posicio) {
		super(posicio);
	}

	// Si el pingüí té un peix el gasta; sense peix torna a l'inici
	@Override
	public void realitzarAccio(Partida partida, Jugador jugador) {
		// Mostrar ataque de l'ós (animació visual)
		vista.PantallaJuego.mostrarAtaqueOso(this.getPosicio());

		if (jugador instanceof Pinguino) {
			Pinguino pingui = (Pinguino) jugador;
			Peix peix = (Peix) pingui.getInventari().getLlista().stream()
					.filter(i -> i instanceof Peix && i.getQuantitat() > 0).findFirst().orElse(null);

			if (peix != null) {
				// Diàleg per al jugador humà
				javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
				alert.setTitle("Atac de l'Os!");
				alert.setHeaderText(pingui.getNickname() + " ha estat atacat per un ós.");
				alert.setContentText("Tens un peix a l'inventari. Vols usar-lo per distraure l'ós i salvar-te?");

				javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Sí, usar Peix");
				javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("No, guardar Peix");
				alert.getButtonTypes().setAll(btnYes, btnNo);
				vista.PantallaJuego.estilarAlerta(alert);

				java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
				
				if (result.isPresent() && result.get() == btnYes) {
					pingui.getInventari().usarItem(peix);
					System.out.println(pingui.getNickname() + " ha usat un peix i s'ha salvat de l'ós.");
					vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " s'ha salvat gràcies a un peix!", "log-info");
				} else {
					pingui.setPosicio(0);
					System.out.println(pingui.getNickname() + " torna a l'inici per l'atac de l'ós.");
					vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha estat atacat per un ós i torna a l'inici!", "log-warning");
				}
			} else {
				pingui.setPosicio(0);
				System.out.println(pingui.getNickname() + " torna a l'inici per l'atac de l'ós (sense peix).");
				vista.PantallaJuego.registrarEventoEstatico(pingui.getNickname() + " ha estat atacat per un ós i torna a l'inici!", "log-warning");
			}
		} else if (jugador instanceof Foca) {
			// Nerfeig per a les foces: El "por" a l'os les fa tenir el moviment limitat el següent torn (1-3)
			jugador.setNerfOs(true);
			
			System.out.println("La foca " + jugador.getNickname() + " ha estat espantada per l'os i tindrà el moviment limitat el proper torn.");
			vista.PantallaJuego.registrarEventoEstatico("La foca " + jugador.getNickname() + " ha estat espantada per l'os i tindrà el moviment limitat el proper torn!", "log-warning");
		}
	}
}

package vista;

import java.util.ArrayList;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;

import model.caselles.Casella;
import model.core.Taulell;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.Inventari;
import model.items.Dau;
import controlador.GestorPartida;
import controlador.GestorTaulell;

public class PantallaJuego {

	// Menu items
	@FXML
	private MenuItem newGame;
	@FXML
	private MenuItem saveGame;
	@FXML
	private MenuItem loadGame;
	@FXML
	private MenuItem quitGame;
	
	
	
	
	// Buttons
	@FXML
	private Button dado;
	@FXML
	private Button rapido;
	@FXML
	private Button lento;
	@FXML
	private Button peces;
	@FXML
	private Button nieve;

	// Texts
	@FXML
	private Text dadoResultText;
	@FXML
	private Text rapido_t;
	@FXML
	private Text lento_t;
	@FXML
	private Text peces_t;
	@FXML
	private Text nieve_t;
	@FXML
	private Text eventos;

	// Game board and player pieces
	@FXML
	private GridPane tablero;
	@FXML
	private Circle P1;
	@FXML
	private Circle P2;
	@FXML
	private Circle P3;
	@FXML
	private Circle P4;

	private GestorPartida gestorPartida;
	// ONLY FOR TESTING!!!
	private int p1Position = 0; // Tracks current position (from 0 to 49 in a 5x10 grid)
	private static final int COLUMNS = 5;

	private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";


	@FXML
	private void initialize() {
		eventos.setText("¡El juego ha comenzado!");

		gestorPartida = new GestorPartida();
		GestorTaulell gestorTaulell = new GestorTaulell();
		
		// Generar taulell aleatori seguint les regles del GestorTaulell
		String seed = gestorTaulell.generarSeedAleatori();
		Taulell taulell = gestorTaulell.generarTaulell(seed);
		
		ArrayList<Jugador> jugadors = new ArrayList<Jugador>();
		Inventari inventari = new Inventari();
		Dau dau = new Dau("Dau normal", 1, 1, 6);
		inventari.afegirItem(dau);
		
		Pinguino pingu = new Pinguino("Jugador1", "Blau", inventari);
		jugadors.add(pingu);

		gestorPartida.novaPartida(jugadors, taulell);

		// Show board info
		mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTaulell());
	}

	private void mostrarTiposDeCasillasEnTablero(Taulell t) {
		// Clear only the labels we generated in previous calls
		tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

		for (int i = 0; i < t.getCaselles().size(); i++) {
			Casella casilla = t.getCaselles().get(i);

			// Skip position 0 and 49 if you want them to be special (start/end)
			if (i > 0 && i < 49) {
			String tipo = casilla.getClass().getSimpleName();

			Text texto = new Text(tipo);
			texto.setUserData(TAG_CASILLA_TEXT);
			texto.getStyleClass().add("cell-type");

			int row = i / COLUMNS;
			int col = i % COLUMNS;

			GridPane.setRowIndex(texto, row);
			GridPane.setColumnIndex(texto, col);

			tablero.getChildren().add(texto);
			}
		}
	}

	// Menu actions
	@FXML
	private void handleNewGame() {
		System.out.println("New game.");
		// TODO
	}

	@FXML
	private void handleSaveGame() {
		System.out.println("Saved game.");
		// Implementación del guardado llamando al gestor
		gestorPartida.guardarPartida(null); 
	}


	@FXML
	private void handleLoadGame() {
		System.out.println("Loaded game.");
		// TODO
	}

	@FXML
	private void handleQuitGame() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Salir del Juego");
		alert.setHeaderText("¿Deseas guardar la partida antes de salir?");
		alert.setContentText("Elige una opción:");

		ButtonType buttonTypeYes = new ButtonType("Sí, guardar y salir");
		ButtonType buttonTypeNo = new ButtonType("No, salir sin guardar");
		ButtonType buttonTypeCancel = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());

		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

		alert.showAndWait().ifPresent(response -> {
			if (response == buttonTypeYes) {
				handleSaveGame();
				System.exit(0);
			} else if (response == buttonTypeNo) {
				System.exit(0);
			}
		});
	}


	// Button actions
	@FXML
	private void handleDado(ActionEvent event) {
		Pinguino pingu = (Pinguino) gestorPartida.getPartida().getJugadors().get(0);
		Dau d = (Dau) pingu.getInventari().obtenirPrimer(Dau.class);
		
		if (d == null) {
			d = new Dau(); // Default dice if none in inventory
		}
		
		System.out.println("Pos pingu previa:" + pingu.getPosicio());
		
		int resultado = gestorPartida.tirarDau(pingu, d);
		
		// Actualitzar el model (GestorPartida.tirarDau no mou el jugador automàticament si només crida tirar)
		// Processar torn complet o moure el jugador manualment per reflectir el canvi
		pingu.mourePosicio(resultado);
		
		System.out.println("Pos pingu actual:" + pingu.getPosicio());

		// Update the Text
		dadoResultText.setText("Ha salido: " + resultado);

		// Update the position in UI
		moveP1(resultado);
	}

	
/*	Old simple version
 * private void moveP1(int steps) {
		p1Position += steps;

		// Bound player
		if (p1Position >= 50) {
			p1Position = 49; // 5 columns * 10 rows = 50 cells (index 0 to 49)
		}
		
		if (p1Position < 0) {
			p1Position = 0;
		}

		// Check row and column
		int row = p1Position / COLUMNS;
		int col = p1Position % COLUMNS;

		// Change P1 property to match row and column
		GridPane.setRowIndex(P1, row);
		GridPane.setColumnIndex(P1, col);
	}*/
	
	private void moveP1(int steps) {

	    // Evita spam del botón
	    dado.setDisable(true);

	    int oldPosition = p1Position;

	    p1Position += steps;

	    // Bound player
	    if (p1Position >= 50) {
	        p1Position = 49;
	    }

	    if (p1Position < 0) {
	        p1Position = 0;
	    }

	    // OLD position
	    int oldRow = oldPosition / COLUMNS;
	    int oldCol = oldPosition % COLUMNS;

	    // NEW position
	    int newRow = p1Position / COLUMNS;
	    int newCol = p1Position % COLUMNS;

	    // Cell size (aproximado)
	    double cellWidth = tablero.getWidth() / COLUMNS;
	    double cellHeight = tablero.getHeight() / 10;

	    double dx = (newCol - oldCol) * cellWidth;
	    double dy = (newRow - oldRow) * cellHeight;

	    TranslateTransition slide = new TranslateTransition(Duration.millis(350), P1);

	    slide.setByX(dx);
	    slide.setByY(dy);

	    slide.setOnFinished(e -> {

	        // reset translation
	        P1.setTranslateX(0);
	        P1.setTranslateY(0);

	        // set real position in grid
	        GridPane.setRowIndex(P1, newRow);
	        GridPane.setColumnIndex(P1, newCol);

	        // volver a activar el botón
	        dado.setDisable(false);
	    });

	    slide.play();
	}

	@FXML
	private void handleRapido() {
		Pinguino pingu = (Pinguino) gestorPartida.getPartida().getJugadors().get(0);
		// Cercar un Dau que es digui "Dau Ràpid" o similar, o que tingui rang superior
		// Per simplificar, busquem el primer Dau especial
		Dau d = (Dau) pingu.getInventari().obtenirPrimer(Dau.class);
		if (d != null && d.getMax() > 6) {
			handleDado(null); // Reuse dice logic if it's the right one
		} else {
			System.out.println("No tens Dau Ràpid!");
		}
	}

	@FXML
	private void handleLento() {
		Pinguino pingu = (Pinguino) gestorPartida.getPartida().getJugadors().get(0);
		Dau d = (Dau) pingu.getInventari().obtenirPrimer(Dau.class);
		if (d != null && d.getMax() <= 3) {
			handleDado(null);
		} else {
			System.out.println("No tens Dau Lent!");
		}
	}

	@FXML
	private void handlePeces() {
		Pinguino pingu = (Pinguino) gestorPartida.getPartida().getJugadors().get(0);
		if (pingu.getInventari().getPeixos() > 0) {
			System.out.println("Has usat un peix!");
			// TODO: Aplicar efecte peix (p.ex. moure posició o inventari)
		} else {
			System.out.println("No tens peixos!");
		}
	}

	@FXML
	private void handleNieve() {
		Pinguino pingu = (Pinguino) gestorPartida.getPartida().getJugadors().get(0);
		if (pingu.getInventari().getBoles() > 0) {
			System.out.println("Has usat una bola de neu!");
			// TODO: Aplicar efecte bola de neu
		} else {
			System.out.println("No tens boles de neu!");
		}
	}

	
}

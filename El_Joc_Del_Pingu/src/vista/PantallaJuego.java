package vista;

import java.util.ArrayList;

import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Scanner;
import java.sql.Connection;
import javafx.util.Duration;
import javafx.scene.control.ChoiceDialog;

import model.caselles.Casella;
import model.core.Partida;
import model.core.Taulell;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.Inventari;
import model.items.Dau;
import model.items.Peix;
import model.items.BolaNeu;
import javafx.application.Platform;
import controlador.GestorPartida;
import controlador.GestorTaulell;
import controlador.GestorBBDD;

public class PantallaJuego {

	// Menu items
	@FXML
	private MenuItem saveGame;
	@FXML
	private MenuItem menuItem;
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
	@FXML
	private VBox logEventos;
	@FXML
	private ScrollPane scrollEventos;

	// Game board and player pieces
	@FXML
	private StackPane boardContainer;
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
	private static Partida partidaInicial;
	
	private static PantallaJuego instanciaActual;

	// Dado especial seleccionado para el próximo turno (null = dado estándar)
	private Dau dauSeleccionat = null;

	private static final int COLUMNS = 10;
	private static final int ROWS = 5;

	@FXML
	private VBox sidebarPlayers;

	private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";

	public static void setPartidaInicial(Partida p) {
		partidaInicial = p;
	}

	@FXML
	private void initialize() {
		instanciaActual = this;
		
		tablero.setPrefSize(2688, 1472);
		tablero.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		tablero.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

		javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(1, 1, 0, 0);
		tablero.getTransforms().add(scaleTransform);

		NumberBinding scaleFactor = Bindings.min(
		    // 20px de Insets de padding (10 left + 10 right = 20)
		    boardContainer.widthProperty().subtract(20).divide(2688.0),
		    boardContainer.heightProperty().subtract(20).divide(1472.0)
		);

		scaleTransform.xProperty().bind(scaleFactor);
		scaleTransform.yProperty().bind(scaleFactor);

		javafx.scene.Group scaleGroup = new javafx.scene.Group(tablero);
		boardContainer.getChildren().clear();
		boardContainer.getChildren().add(scaleGroup);
		
		registrarEvento("¡El juego ha comenzado!", "log-info");

		gestorPartida = new GestorPartida();
		
		if (partidaInicial != null) {
			gestorPartida.setPartida(partidaInicial);
			partidaInicial = null; // Limpiar para la próxima vez
		} else {
			// Fallback: Nueva partida por defecto
			GestorTaulell gestorTaulell = new GestorTaulell();
			String seed = gestorTaulell.generarSeedAleatori();
			Taulell taulell = gestorTaulell.generarTaulell(seed);
			ArrayList<Jugador> jugadors = new ArrayList<Jugador>();
			jugadors.add(new Pinguino("Jugador1", "Azul", new Inventari()));
			gestorPartida.novaPartida(jugadors, taulell);
		}

		// Mostrar info del tablero
		mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTaulell());
		actualizarUI();
		
		// Verificar si el primer turno es de la CPU
		checkTurnoCPU();
	}

	/**
	 * Actualitza tota la interfície per reflectir l'estat actual de la partida.
	 * Sincronitza posicions de fitxes, visibilitat i indicadors de torn.
	 */
	public void actualizarUI() {
		ArrayList<Jugador> js = gestorPartida.getPartida().getJugadors();
		
		// Gestió de la visibilitat de les peces segons el nombre de jugadors
		P2.setVisible(js.size() > 1);
		P3.setVisible(js.size() > 2);
		P4.setVisible(js.size() > 3);
		
		// Actualitzar la posició física de cada peça al GridPane
		for (int i = 0; i < js.size(); i++) {
			Jugador j = js.get(i);
			Circle pieza = getPiezaParaJugador(j);
			if (pieza != null) {
				int pos = j.getPosicio();
				// Snake/Zigzag mapping (10x5 grid built bottom-up)
				int logicalRow = pos / COLUMNS;
				int logicalCol = pos % COLUMNS;
				
				// Si la fila es impar (1, 3, etc.), la dirección es derecha-a-izquierda
				if (logicalRow % 2 != 0) {
					logicalCol = (COLUMNS - 1) - logicalCol;
				}

				int row = ((ROWS - 1) - logicalRow) + 1;
				int col = logicalCol + 1;

				GridPane.setRowIndex(pieza, row);
				GridPane.setColumnIndex(pieza, col);
			}
		}
		
		// Actualitzar barra lateral de jugadors
		actualizarSidebarJugadores();

		// Actualitzar comptadors d'objectes i estat dels botons
		actualizarContadoresObjetos();
	}

	/**
	 * Registra un nou esdeveniment al log persistent.
	 */
	private void registrarEvento(String mensaje, String styleClass) {
		if (logEventos == null) return;

		String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		Label entry = new Label("[" + timestamp + "] " + mensaje);
		entry.getStyleClass().add(styleClass);
		entry.setWrapText(true);
		entry.setMaxWidth(280);

		logEventos.getChildren().add(entry);

		// Auto-scroll al final
		Platform.runLater(() -> scrollEventos.setVvalue(1.0));
		
		// Compatibility fallback if anyone else uses the old label
		if (eventos != null) eventos.setText(mensaje);
	}

	/**
	 * Habilita o deshabilita todos los controles de interacción del jugador.
	 */
	private void bloquearControles(boolean bloquear) {
	    dado.setDisable(bloquear);
	    rapido.setDisable(bloquear);
	    lento.setDisable(bloquear);
	    peces.setDisable(bloquear);
	    nieve.setDisable(bloquear);
	}

	/**
	 * Actualitza els comptadors d'objectes a la UI i habilita/deshabilita
	 * els botons segons l'inventari del jugador humà actiu.
	 */
	private void actualizarContadoresObjetos() {
		Jugador actual = gestorPartida.getPartida().getJugadorActual();

		// Si no és el torn d'un Pingüí (humano), deshabilitar todos los controles
		if (!(actual instanceof Pinguino pingu)) {
			bloquearControles(true);
			return;
		}

		// Si es humano, primero desbloqueamos la base y luego afinamos según inventario
		bloquearControles(false);
		Inventari inv = pingu.getInventari();

		// Cercar daus especials: ràpid (max > 6) i lent (max <= 3)
		Dau dRapid = null, dLent = null;
		for (model.items.Item obj : inv.getLlista()) {
			if (obj instanceof Dau d) {
				if (d.getMax() > 6  && dRapid == null) dRapid = d;
				if (d.getMax() <= 3 && dLent  == null) dLent  = d;
			}
		}

		// Actualitzar textos amb quantitats reals de l'inventari
		rapido_t.setText("Dado rápido: " + (dRapid != null ? dRapid.getQuantitat() : 0));
		lento_t.setText( "Dado lento: "  + (dLent  != null ? dLent.getQuantitat()  : 0));
		peces_t.setText( "Peces: "        + inv.getPeixos());
		nieve_t.setText( "Bolas de nieve: " + inv.getBoles());

		// Habilitar/deshabilitar botons
		rapido.setDisable(dRapid == null || dRapid.getQuantitat() <= 0);
		lento.setDisable( dLent  == null || dLent.getQuantitat()  <= 0);
		peces.setDisable( inv.getPeixos() <= 0);
		nieve.setDisable( inv.getBoles()  <= 0);
	}

	private void actualizarSidebarJugadores() {
		sidebarPlayers.getChildren().clear();
		
		Label title = new Label("Estado Jugadores");
		title.getStyleClass().add("sidebar-title");
		sidebarPlayers.getChildren().add(title);

		Partida pActual = gestorPartida.getPartida();
		for (Jugador j : pActual.getJugadors()) {
			VBox card = new VBox(5);
			card.getStyleClass().add("player-status-card");
			
			// Highlight del jugador amb torn actiu
			if (j == pActual.getJugadorActual()) {
				card.getStyleClass().add("is-current-turn");
			}

			HBox header = new HBox(10);
			Circle colorIndicator = new Circle(8);
			String colorHex = getColorForPlayerIndex(pActual.getJugadors().indexOf(j));
			colorIndicator.setStyle("-fx-fill: " + colorHex + ";");
			
			Label name = new Label(j.getNickname());
			name.getStyleClass().add("player-name");
			
			header.getChildren().addAll(colorIndicator, name);
			card.getChildren().add(header);

			if (j instanceof Pinguino p) {
				VBox inv = new VBox(2);
				inv.getStyleClass().add("player-inv-mini");
				
				Label peces = new Label("Peces: " + p.getInventari().getPeixos());
				Label boles = new Label("Boles: " + p.getInventari().getBoles());
				Label daus = new Label("Daus: " + p.getInventari().getDausEspecials());
				
				inv.getChildren().addAll(peces, boles, daus);
				card.getChildren().add(inv);
			} else {
				Label cpuLabel = new Label("(CPU - Foca)");
				cpuLabel.getStyleClass().add("cpu-label");
				card.getChildren().add(cpuLabel);
			}

			sidebarPlayers.getChildren().add(card);
		}
	}

	/**
	 * Retorna l'element visual (Cercle) associat a un jugador segons el seu índex.
	 */
	private Circle getPiezaParaJugador(Jugador j) {
		int idx = gestorPartida.getPartida().getJugadors().indexOf(j);
		switch (idx) {
			case 0: return P1;
			case 1: return P2;
			case 2: return P3;
			case 3: return P4;
			default: return null;
		}
	}

	/**
	 * Retorna el color HEX que correspon a la fitxa del jugador segons el seu índex.
	 * Sincronitzat amb els colors definits a PantallaJuego.css
	 */
	private String getColorForPlayerIndex(int index) {
		switch (index) {
			case 0: return "#C0392B"; // P1 - Rojo
			case 1: return "#3498DB"; // P2 - Azul
			case 2: return "#27AE60"; // P3 - Verde
			case 3: return "#F1C40F"; // P4 - Amarillo
			default: return "#FFFFFF";
		}
	}

	private void mostrarTiposDeCasillasEnTablero(Taulell t) {
		// Clear only the tiles we generated in previous calls
		tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

		int total = t.getCaselles().size();
		for (int i = 0; i < total; i++) {
			Casella casilla = t.getCaselles().get(i);

			// Wrap in a StackPane to represent the cell (no text, only CSS class)
			StackPane iceBlock = new StackPane();
			iceBlock.setUserData(TAG_CASILLA_TEXT);
			iceBlock.getStyleClass().add("board-cell");

			// Add specific type class for coloring/assets
			if (i == 0) {
				iceBlock.getStyleClass().add("start-cell");
			} else if (i == total - 1) {
				iceBlock.getStyleClass().add("finish-cell");
			} else {
				iceBlock.getStyleClass().add("cell-" + casilla.getClass().getSimpleName());
			}

			// Snake mapping to inner 10x5 area of 12x7 grid
			int logicalRow = i / COLUMNS;
			int logicalCol = i % COLUMNS;
			
			// Fila impar = derecha a izquierda
			if (logicalRow % 2 != 0) {
				logicalCol = (COLUMNS - 1) - logicalCol;
			}

			int row = ((ROWS - 1) - logicalRow) + 1;
			int col = logicalCol + 1;

			GridPane.setRowIndex(iceBlock, row);
			GridPane.setColumnIndex(iceBlock, col);

			tablero.getChildren().add(0, iceBlock); // Add to back so players stay on top
		}
	}

	public static void mostrarPopupItem(Jugador j, String imagenNombre) {
		if (instanciaActual == null) return;
		Platform.runLater(() -> instanciaActual.mostrarPopupUI(j, imagenNombre));
	}

	private void mostrarPopupUI(Jugador j, String imagenNombre) {
		Circle pieza = getPiezaParaJugador(j);
		if (pieza == null) return;

		int pos = j.getPosicio();
		int logicalRow = pos / COLUMNS;
		int logicalCol = pos % COLUMNS;
		if (logicalRow % 2 != 0) {
			logicalCol = (COLUMNS - 1) - logicalCol;
		}

		int row = ((ROWS - 1) - logicalRow) + 1;
		int col = logicalCol + 1;

		try {
			javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/assets/" + imagenNombre));
			javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView(img);
			// Mida de l'icono
			icon.setFitWidth(80);
			icon.setFitHeight(80);
			icon.setPreserveRatio(true);

			GridPane.setRowIndex(icon, row);
			GridPane.setColumnIndex(icon, col);
			GridPane.setHalignment(icon, javafx.geometry.HPos.CENTER);
			GridPane.setValignment(icon, javafx.geometry.VPos.CENTER);

			// Començar 30 píxels amunt per no tapar totalment el jugador
			icon.setTranslateY(-30);

			tablero.getChildren().add(icon);

			// Animació de pujada i esvaïment
			javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(1500), icon);
			tt.setByY(-60);

			javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(1500), icon);
			ft.setFromValue(1.0);
			ft.setToValue(0.0);

			javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(icon, tt, ft);
			pt.setOnFinished(e -> tablero.getChildren().remove(icon));
			pt.play();
			
		} catch (Exception e) {
			System.err.println("Error carregant la imatge del popup: " + imagenNombre);
		}
	}

	// Menu actions

	private Connection getBDConnection() {
		// Patrón basado en PantallaMenu para entorno de desarrollo
		return GestorBBDD.conectarBaseDatos();
	}

	@FXML
	private void handleSaveGame() {
		System.out.println("Saved game.");
		try (Connection con = getBDConnection()) {
			if (con != null) {
				gestorPartida.guardarPartida(con);
				registrarEvento("Partida guardada correctamente.", "log-info");
			} else {
				registrarEvento("No se pudo conectar a la base de datos para guardar.", "log-warning");
			}
		} catch (Exception e) {
			e.printStackTrace();
			registrarEvento("Error al guardar la partida.", "log-warning");
		}
	}


	@FXML
	private void handleGoToMenu() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		estilar(alert);
		alert.setTitle("Volver al Menú");
		alert.setHeaderText("¿Deseas guardar la partida antes de volver al menú?");
		alert.setContentText("Elige una opción:");

		ButtonType buttonTypeYes = new ButtonType("Sí, guardar y salir");
		ButtonType buttonTypeNo = new ButtonType("No, salir sin guardar");
		ButtonType buttonTypeCancel = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());

		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

		alert.showAndWait().ifPresent(response -> {
			if (response == buttonTypeYes) {
				handleSaveGame();
				goToMenu();
			} else if (response == buttonTypeNo) {
				goToMenu();
			}
		});
	}

	private void goToMenu() {
		try {
			// Usar el método centralizado para mantener resolución y estado
			controlador.Main.cambiarEscena("/resources/PantallaMenu.fxml");
		} catch (Exception e) {
			e.printStackTrace();
			registrarEvento("Error al volver al menú.", "log-warning");
		}
	}

	@FXML
	private void handleQuitGame() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		estilar(alert);
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
	    // Solo permitimos el clic manual si es el turno del jugador humano
	    if (gestorPartida.getPartida().getJugadorActual() instanceof Pinguino) {
	        executartorn();
	    }
	}

	/**
	 * Executa la lògica d'un torn complet: tirada de dau i moviment.
	 */
	private void executartorn() {
		Partida p = gestorPartida.getPartida();
		Jugador actual = p.getJugadorActual();
		
		if (p.isFinalitzada()) {
			registrarEvento("¡Partida acabada! Guanyador: " + p.getGuanyador().getNickname(), "log-warning");
			return;
		}

		// Bloqueamos controles inmediatamente para evitar doble clic o interferencias
		bloquearControles(true);

		registrarEvento("Torn de: " + actual.getNickname(), "log-turn");

		// Obtenir dau: prioritzar el seleccionat manualment per l'usuari
		Dau d;
		if (dauSeleccionat != null) {
			d = dauSeleccionat;
			dauSeleccionat = null; // Consumir la selecció un cop usada
		} else {
			// Els jugadors humans ARA SEMPRE usen un dau estàndard (1-6) per defecte
			// encara que tinguin daus especials a l'inventari, a menys que hagin pitjat el botó.
			d = new Dau();
		}

		int resultado = gestorPartida.tirarDau(actual, d);
		dadoResultText.setText("Dau: " + resultado);
		
		// Color del text del dau: Taronja (Ràpid), Verd (Lent), Blanc (Estàndard)
		if (d.esEspecial()) {
		    if (d.getMax() > 6) {
		        dadoResultText.setStyle("-fx-fill: #E67E22;"); // Naranja
		    } else if (d.getMax() <= 3) {
		        dadoResultText.setStyle("-fx-fill: #27AE60;"); // Verde
		    }
		} else {
		    dadoResultText.setStyle("-fx-fill: white;");
		}
		
		// Animació a la UI abans de canviar el torn al model
		moverPieza(actual, resultado);
	}

	/**
	 * Mou una peça amb una animació de transició i actualitza el model en acabar.
	 */
	private void moverPieza(Jugador j, int steps) {
	    bloquearControles(true);
	    Circle pieza = getPiezaParaJugador(j);
	    if (pieza == null) return;

	    int oldPos = j.getPosicio();
	    int newPos = Math.min(oldPos + steps, gestorPartida.getPartida().getTaulell().getCaselles().size() - 1);

	    int oldLogicalRow = oldPos / COLUMNS;
	    int oldLogicalCol = oldPos % COLUMNS;
	    if (oldLogicalRow % 2 != 0) {
	    	oldLogicalCol = (COLUMNS - 1) - oldLogicalCol;
	    }
	    
	    int newLogicalRow = newPos / COLUMNS;
	    int newLogicalCol = newPos % COLUMNS;
	    if (newLogicalRow % 2 != 0) {
	    	newLogicalCol = (COLUMNS - 1) - newLogicalCol;
	    }

	    int oldRow = ((ROWS - 1) - oldLogicalRow) + 1;
	    int oldCol = oldLogicalCol + 1;
	    int newRow = ((ROWS - 1) - newLogicalRow) + 1;
	    int newCol = newLogicalCol + 1;

	    // the grid geometry gives us fixed constraints based on 10x5 physical structure 
	    // in a scene where width is exactly mapped to % constraints
	    // For translations, simply use bound constraints of the cells inside the Grid
	    double cellWidth = tablero.getPrefWidth() * 0.0918;   // 9.18% de ancho x columna
	    double cellHeight = tablero.getPrefHeight() * 0.1711; // 17.11% de alto x fila


	    double dx = (newCol - oldCol) * cellWidth;
	    double dy = (newRow - oldRow) * cellHeight;

	    TranslateTransition slide = new TranslateTransition(Duration.millis(500), pieza);
	    slide.setByX(dx);
	    slide.setByY(dy);

	    slide.setOnFinished(e -> {
	        pieza.setTranslateX(0);
	        pieza.setTranslateY(0);
	        
	        // Actualitzar model
	        j.setPosicio(newPos);
	        
	        // --- LÒGICA DE BATALLA ---
	        // Si el jugador acabat de moure és un pingüí, busquem col·lisions
	        if (j instanceof Pinguino pActual) {
	            for (Jugador rival : gestorPartida.getPartida().getJugadors()) {
	                // Si hi ha un altre pingüí a la mateixa casella (i no sóc jo mateix)
	                if (rival != pActual && rival.getPosicio() == newPos && rival instanceof Pinguino pRival) {
	                    registrarEvento("Col·lisió! Batalla entre " + pActual.getNickname() + " i " + pRival.getNickname(), "log-warning");
	                    pActual.gestionarBatalla(pRival);
	                    break; // Una única trobada per torn
	                }
	            }
	        }
	        
	        // Executar lògica de la casella on arribat
	        GestorTaulell gt = new GestorTaulell();
	        gt.executarCasella(gestorPartida.getPartida(), j, gestorPartida.getPartida().getTaulell().getCaselles().get(j.getPosicio()));
	        
	        // Verificar victoria tras movimiento y efectos
	        boolean wasFinished = gestorPartida.getPartida().isFinalitzada();
	        gt.comprovarFiTorn(gestorPartida.getPartida());
	        
	        if (gestorPartida.getPartida().isFinalitzada()) {
	            actualizarUI();
	            Jugador guanyador = gestorPartida.getPartida().getGuanyador();

	            if (!wasFinished && guanyador != null && !(guanyador instanceof model.entitats.Foca)) {
	                // Registrem la victoria al ranking de forma automática
	                try (Connection con = getBDConnection()) {
	                    if (con != null) {
	                        new GestorBBDD().registrarVictoria(guanyador.getId(), con);
	                    }
	                } catch (Exception e1) {
	                    System.err.println("Error registrant victoria: " + e1.getMessage());
	                }
	            }

	            mostrarAlertaGanador(guanyador);
	            return;
	        }

	        // Passar el torn
	        gestorPartida.seguentTorn();
	        
	        actualizarUI();
	        
	        // Comprovar si el següent és CPU
	        checkTurnoCPU();
	    });

	    slide.play();
	}

	/**
	 * Muestra un diálogo de victoria y ofrece opciones al usuario.
	 */
	private void mostrarAlertaGanador(Jugador g) {
	    Platform.runLater(() -> {
	        Alert alert = new Alert(AlertType.INFORMATION);
	        estilar(alert);
	        alert.setTitle("¡Fin de la partida!");
	        alert.setHeaderText("¡Tenemos un ganador!");
	        alert.setContentText("Enhorabuena " + g.getNickname() + ", ¡has llegado a la meta!");

	        ButtonType btnGuardar = new ButtonType("Guardar i Sortir");
	        ButtonType btnSalir = new ButtonType("Sortir sense Guardar");
	        alert.getButtonTypes().setAll(btnGuardar, btnSalir);

	        alert.showAndWait().ifPresent(result -> {
	            if (result == btnGuardar) {
	                handleSaveGame();
	                goToMenu();
	            } else if (result == btnSalir) {
	                goToMenu();
	            }
	        });
	    });
	}

	

	/**
	 * Comprova si el següent torn l'ha de fer la CPU i l'executa automàticament.
	 */
	private void checkTurnoCPU() {
		if (gestorPartida.getPartida().isFinalitzada()) return;

		Jugador proximo = gestorPartida.getPartida().getJugadorActual();
		// En aquest model, les Foques sempre són CPU
		if (proximo instanceof model.entitats.Foca) {
		    bloquearControles(true);
			new Thread(() -> {
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
				javafx.application.Platform.runLater(this::executartorn);
			}).start();
		}
	}

	@FXML
	private void handleRapido() {
		Jugador actual = gestorPartida.getPartida().getJugadorActual();
		if (!(actual instanceof Pinguino pingu)) return;

		// Buscar dau ràpid (max > 6) a la llista real de l'inventari
		Dau dRapid = null;
		for (model.items.Item obj : pingu.getInventari().getLlista()) {
			if (obj instanceof Dau d && d.getMax() > 6) { dRapid = d; break; }
		}

		if (dRapid == null || dRapid.getQuantitat() <= 0) {
			registrarEvento("No tienes dado rápido.", "log-warning");
			return;
		}

		// usarItem decrementa quantitat i l'elimina si arriba a 0
		pingu.getInventari().usarItem(dRapid);
		dauSeleccionat = dRapid;
		registrarEvento(pingu.getNickname() + " usa dado rápido (1-" + dRapid.getMax() + ")", "log-info");
		executartorn();
	}

	@FXML
	private void handleLento() {
		Jugador actual = gestorPartida.getPartida().getJugadorActual();
		if (!(actual instanceof Pinguino pingu)) return;

		// Buscar dau lent (max <= 3)
		Dau dLent = null;
		for (model.items.Item obj : pingu.getInventari().getLlista()) {
			if (obj instanceof Dau d && d.getMax() <= 3) { dLent = d; break; }
		}

		if (dLent == null || dLent.getQuantitat() <= 0) {
			registrarEvento("No tienes dado lento.", "log-warning");
			return;
		}

		pingu.getInventari().usarItem(dLent);
		dauSeleccionat = dLent;
		registrarEvento(pingu.getNickname() + " usa dado lento (1-" + dLent.getMax() + ")", "log-info");
		executartorn();
	}

	@FXML
	private void handlePeces() {
		Jugador actual = gestorPartida.getPartida().getJugadorActual();
		if (!(actual instanceof Pinguino pingu)) return;

		// usarPrimer busca el primer Peix disponible, el usa y lo elimina si llega a 0
		boolean usat = pingu.getInventari().usarPrimer(Peix.class);
		if (!usat) {
			registrarEvento("No tienes peces.", "log-warning");
			return;
		}

		registrarEvento(pingu.getNickname() + " usa un pez: avanza 2 casillas extra.", "log-info");
		int maxPos = gestorPartida.getPartida().getTaulell().getCaselles().size() - 1;
		pingu.setPosicio(Math.min(pingu.getPosicio() + 2, maxPos));
		actualizarUI();
	}

	@FXML
	private void handleNieve() {
		Jugador actual = gestorPartida.getPartida().getJugadorActual();
		if (!(actual instanceof Pinguino pingu)) return;

		boolean usat = pingu.getInventari().usarPrimer(BolaNeu.class);
		if (!usat) {
			registrarEvento("No tienes bolas de nieve.", "log-warning");
			return;
		}

		registrarEvento(pingu.getNickname() + " lanza una bola de nieve: el siguiente jugador retrocede 1 casilla.", "log-info");

		// El jugador següent retrocedeix 1 casella
		ArrayList<Jugador> js = gestorPartida.getPartida().getJugadors();
		int idxSeguent = (js.indexOf(pingu) + 1) % js.size();
		Jugador seguent = js.get(idxSeguent);
		seguent.setPosicio(Math.max(0, seguent.getPosicio() - 1));
		actualizarUI();
	}


	/**
	 * Aplica el stylesheet polar del menú a cualquier Alert o Dialog.
	 */
	private void estilar(javafx.scene.control.Dialog<?> d) {
		try {
			// Establecer el owner para intentar que no se salga de pantalla completa
			if (boardContainer != null && boardContainer.getScene() != null) {
				d.initOwner(boardContainer.getScene().getWindow());
			}

			javafx.scene.control.DialogPane pane = d.getDialogPane();
			String css = getClass().getResource("/resources/PantallaMenu.css").toExternalForm();
			pane.getStylesheets().add(css);
		} catch (Exception e) {
			System.err.println("No se pudo aplicar el CSS al diálogo: " + e.getMessage());
		}
	}
}
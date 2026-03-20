package vista;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import model.entitats.Foca;
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
	private StackPane boardRoot;
	@FXML
	private AnchorPane boardContainer;
	@FXML
	private GridPane tablero;
	@FXML
	private ImageView P1;
	@FXML
	private ImageView P2;
	@FXML
	private ImageView P3;
	@FXML
	private ImageView P4;

	private GestorPartida gestorPartida;
	private static Partida partidaInicial;
	
	private static PantallaJuego instanciaActual;

	// Caché de imágenes para evitar recargas constantes
	private static final Map<String, Image> imageCache = new HashMap<>();

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
		// Cargar imágenes de los pingüinos
		try {
			// Cargar imágenes de forma dinámica según el tipo de jugador si ya hay partida
			// o por defecto si es el inicio absoluto.
			actualizarImagenesIniciales();

			// Ensure centering in GridPane
			GridPane.setHalignment(P1, HPos.CENTER);
			GridPane.setValignment(P1, VPos.CENTER);
			GridPane.setHalignment(P2, HPos.CENTER);
			GridPane.setValignment(P2, VPos.CENTER);
			GridPane.setHalignment(P3, HPos.CENTER);
			GridPane.setValignment(P3, VPos.CENTER);
			GridPane.setHalignment(P4, HPos.CENTER);
			GridPane.setValignment(P4, VPos.CENTER);
		} catch (Exception e) {
			System.err.println("Error cargando imágenes de pingüinos: " + e.getMessage());
		}
		// Usamos el tamaño que venga definido del FXML (ajustado en Scene Builder)
		javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(1, 1, 0, 0);

		// El factor de escala ahora depende del contenedor RAÍZ (el que se estira con la ventana)
		// Ajustamos a la nueva resolución base 1920x1080
		NumberBinding scaleFactor = Bindings.min(
		    boardRoot.widthProperty().divide(1920.0), 
		    boardRoot.heightProperty().divide(1080.0)
		);
		
		scaleTransform.xProperty().bind(scaleFactor);
		scaleTransform.yProperty().bind(scaleFactor);

		// Metemos el boardContainer (Fondo + Grid + UI) en un Group para escalarlo todo junto
		javafx.scene.Group scaleGroup = new javafx.scene.Group(boardContainer);
		scaleGroup.getTransforms().add(scaleTransform);
		
		// El boardRoot (StackPane) centrará el Group automáticamente
		boardRoot.getChildren().clear();
		boardRoot.getChildren().add(scaleGroup);
		
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
		Map<Integer, Integer> recuento = new HashMap<>();
		for (Jugador j : js) {
			recuento.put(j.getPosicio(), recuento.getOrDefault(j.getPosicio(), 0) + 1);
		}

		Map<Integer, Integer> indiceActual = new HashMap<>();
		for (int i = 0; i < js.size(); i++) {
			Jugador j = js.get(i);
			ImageView pieza = getPiezaParaJugador(j);
			if (pieza != null) {
				// Actualizar imagen y tamaño según si es CPU (Foca) o Humano (Pinguino)
				pieza.setImage(obtenerImagenJugador(j));
				actualizarTamanyPeca(pieza, j);

				int pos = j.getPosicio();
				int numEnCasilla = indiceActual.getOrDefault(pos, 0);
				indiceActual.put(pos, numEnCasilla + 1);

				// Snake/Zigzag mapping (10x5 grid built bottom-up)
				int logicalRow = pos / COLUMNS;
				int logicalCol = pos % COLUMNS;
				
				// Si la fila es impar (1, 3, etc.), la dirección es derecha-a-izquierda
				if (logicalRow % 2 != 0) {
					logicalCol = (COLUMNS - 1) - logicalCol;
				}

				int row = (ROWS - 1) - logicalRow;
				int col = logicalCol;

				GridPane.setRowIndex(pieza, row);
				GridPane.setColumnIndex(pieza, col);
				
				// Aplicar offset solo si hay más de uno
				aplicarOffsetDeSeparacion(pieza, numEnCasilla, recuento.get(pos));
			}
		}
		
		// Actualitzar barra lateral de jugadors
		actualizarSidebarJugadores();

		// Actualitzar comptadors d'objectes i estat dels botons
		actualizarContadoresObjetos();
	}
	
	/**
	 * Wrapper estático para registrar eventos desde fuera de la vista.
	 */
	public static void registrarEventoEstatico(String mensaje, String styleClass) {
		if (instanciaActual != null) {
			instanciaActual.registrarEvento(mensaje, styleClass);
		}
	}

	public static void estilarAlerta(javafx.scene.control.Dialog<?> d) {
		if (instanciaActual != null) instanciaActual.estilar(d);
	}

	/**
	 * Registra un esdeveniment al log visual.
	 */
	public void registrarEvento(String mensaje, String styleClass) {
		Platform.runLater(() -> {
			Label lbl = new Label("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + mensaje);
			lbl.getStyleClass().add(styleClass);
			logEventos.getChildren().add(lbl);
			// Scroll automàtic al final
			scrollEventos.setVvalue(1.0);
		});
	}

	public static void mostrarOverlayBatallaEstatico() {
		if (instanciaActual != null) instanciaActual.mostrarOverlayBatalla();
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
			String colorHex = getColorForPlayer(j);
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
	 * Retorna l'element visual (ImageView) associat a un jugador segons el seu índex.
	 */
	private ImageView getPiezaParaJugador(Jugador j) {
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
	 * Retorna la imatge correcta per al jugador (Pinguino o Foca) segons el seu color.
	 */
	private Image obtenerImagenJugador(Jugador j) {
		String tipo = (j instanceof Foca) ? "FOCA" : "PINGUINO";
		String colorStr = (j.getColor() != null) ? j.getColor() : "Rojo";
		String key = tipo + "_" + colorStr;

		if (imageCache.containsKey(key)) {
			return imageCache.get(key);
		}

		String filename = "";
		if (j instanceof Foca) {
			// Ajustamos el nombre del color para el género femenino de las focas
			String fColor = colorStr;
			if (colorStr.equalsIgnoreCase("Rojo")) fColor = "Roja";
			if (colorStr.equalsIgnoreCase("Amarillo")) fColor = "Amarilla";
			
			filename = "Foca" + fColor + ".png";
		} else {
			// Para el pinguino usamos el formato PINGUINO_COLOR.png en mayúsculas
			filename = "PINGUINO_" + colorStr.toUpperCase() + ".png";
		}

		try {
			Image img = new Image(getClass().getResourceAsStream("/assets/" + filename));
			imageCache.put(key, img);
			return img;
		} catch (Exception e) {
			System.err.println("Error cargando imagen: " + filename);
			return null;
		}
	}

	/**
	 * Configura les imatges inicials de les peces.
	 */
	private void actualizarImagenesIniciales() {
		Partida p = (partidaInicial != null) ? partidaInicial : (gestorPartida != null ? gestorPartida.getPartida() : null);
		
		if (p != null) {
			ArrayList<Jugador> js = p.getJugadors();
			for (int i = 0; i < js.size(); i++) {
				ImageView pieza = getPiezaPorIndice(i);
				if (pieza != null) {
					Jugador j = js.get(i);
					pieza.setImage(obtenerImagenJugador(j));
					actualizarTamanyPeca(pieza, j);
				}
			}
		} else {
			// Fallback por defecto (Pingüinos)
			P1.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_ROJO.png")));
			P2.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_AZUL.png")));
			P3.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_VERDE.png")));
			P4.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_AMARILLO.png")));
		}
	}

	/**
	 * Ajusta el tamaño de la pieza (ImageView) según el tipo de jugador.
	 * Las focas se ven un poco más grandes que los pingüinos.
	 */
	private void actualizarTamanyPeca(ImageView pieza, Jugador j) {
		if (j instanceof Foca) {
			pieza.setFitWidth(55.0);
			pieza.setFitHeight(55.0);
		} else {
			// Tamaño original definido en FXML
			pieza.setFitWidth(42.5);
			pieza.setFitHeight(42.5);
		}
	}

	private ImageView getPiezaPorIndice(int index) {
		switch (index) {
			case 0: return P1;
			case 1: return P2;
			case 2: return P3;
			case 3: return P4;
			default: return null;
		}
	}

	/**
	 * Retorna el color HEX que correspon a la fitxa del jugador segons el seu valor 'color'.
	 */
	private String getColorForPlayer(Jugador j) {
		String color = (j.getColor() != null) ? j.getColor().toLowerCase() : "rojo";
		switch (color) {
			case "rojo":     return "#C0392B";
			case "azul":     return "#3498DB";
			case "verde":    return "#27AE60";
			case "amarillo": return "#F1C40F";
			default:         return "#FFFFFF";
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
			iceBlock.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			GridPane.setFillWidth(iceBlock, true);
			GridPane.setFillHeight(iceBlock, true);

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

			int row = (ROWS - 1) - logicalRow;
			int col = logicalCol;

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
		ImageView pieza = getPiezaParaJugador(j);
		if (pieza == null) return;

		int pos = j.getPosicio();
		int logicalRow = pos / COLUMNS;
		int logicalCol = pos % COLUMNS;
		if (logicalRow % 2 != 0) {
			logicalCol = (COLUMNS - 1) - logicalCol;
		}

		int row = (ROWS - 1) - logicalRow;
		int col = logicalCol;

		try {
			javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/assets/" + imagenNombre));
			javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView(img);
			// Mida de l'icono
			icon.setFitWidth(40);
			icon.setFitHeight(40);
			icon.setPreserveRatio(true);

			GridPane.setRowIndex(icon, row);
			GridPane.setColumnIndex(icon, col);
			GridPane.setHalignment(icon, javafx.geometry.HPos.CENTER);
			GridPane.setValignment(icon, javafx.geometry.VPos.CENTER);

			// Començar 30 píxels amunt per no tapar totalment el jugador
			icon.setTranslateY(-15);

			tablero.getChildren().add(icon);

			// Animació de pujada i esvaïment
			javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(1500), icon);
			tt.setByY(-30);

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
	    ImageView pieza = getPiezaParaJugador(j);
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

	    int oldRow = (ROWS - 1) - oldLogicalRow;
	    int oldCol = oldLogicalCol;
	    int newRow = (ROWS - 1) - newLogicalRow;
	    int newCol = newLogicalCol;

	    // the grid geometry gives us fixed constraints based on 10x5 physical structure 
	    // in a scene where width is exactly mapped to % constraints
	    // For translations, simply use bound constraints of the cells inside the Grid
	    double cellWidth = tablero.getPrefWidth() * 0.10;   // 10% de ancho x columna
	    double cellHeight = tablero.getPrefHeight() * 0.20; // 20% de alto x fila

	    // Calcular offset actual y objetivo para que la animación sea fluida
	    double currentTX = pieza.getTranslateX();
	    double currentTY = pieza.getTranslateY();
	    
	    // Pre-calcular cuántos jugadores habrá en la nueva posición antes que yo (según orden en la lista)
	    int numEnNuevaCasilla = 0;
	    ArrayList<Jugador> js = gestorPartida.getPartida().getJugadors();
	    for (int i = 0; i < js.indexOf(j); i++) {
	    	if (js.get(i).getPosicio() == newPos) {
	    		numEnNuevaCasilla++;
	    	}
	    }
	    
	    // Offset objetivo
	    double calcTX = 0, calcTY = 0;
	    double offsetSeparacion = 15.0;
	    
	    // Contar cuántos hay ya en el destino
	    int totalEnDestino = 0;
	    for (Jugador other : js) {
	    	if (other.getPosicio() == newPos) totalEnDestino++;
	    }
	    // Yo seré el totalEnDestino + 1 (aunque aquí sumo 1 para simular mi llegada)
	    totalEnDestino++; 

	    if (totalEnDestino > 1) {
		    switch (numEnNuevaCasilla) {
		    	case 0: calcTX = -offsetSeparacion; calcTY = -offsetSeparacion; break;
		    	case 1: calcTX =  offsetSeparacion; calcTY = -offsetSeparacion; break;
		    	case 2: calcTX = -offsetSeparacion; calcTY =  offsetSeparacion; break;
		    	case 3: calcTX =  offsetSeparacion; calcTY =  offsetSeparacion; break;
		    }
	    }

	    final double targetTX = calcTX;
	    final double targetTY = calcTY;

	    double dx = (newCol - oldCol) * cellWidth + (targetTX - currentTX);
	    double dy = (newRow - oldRow) * cellHeight + (targetTY - currentTY);

	    TranslateTransition slide = new TranslateTransition(Duration.millis(500), pieza);
	    slide.setByX(dx);
	    slide.setByY(dy);

	    slide.setOnFinished(e -> {
	        pieza.setTranslateX(targetTX);
	        pieza.setTranslateY(targetTY);
	        
	        // Actualitzar model
	        j.setPosicio(newPos);
	        
	        // --- LÒGICA DE COL·LISIONS I BATALLA ---
	        for (Jugador rival : gestorPartida.getPartida().getJugadors()) {
	            if (rival != j && rival.getPosicio() == newPos) {
	                manejarEncuentro(j, rival);
	                break;
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
	 * Gestiona la col·lisió i possible batalla quan un jugador cau en una casella ocupada.
	 */
	private void manejarEncuentro(Jugador j, Jugador rival) {
		if (j instanceof Pinguino pAtacante && rival instanceof Pinguino pRival) {
			registrarEvento("Col·lisió! Batalla entre " + pAtacante.getNickname() + " i " + pRival.getNickname(), "log-warning");
			pAtacante.gestionarBatalla(pRival);
		} else if (j instanceof Pinguino pJugador && rival instanceof Foca fRival) {
			// Intentar sobornar/alimentar a la foca primero (ella preguntará si tiene peces)
			fRival.sobornarFoca(pJugador);

			// Només interactua si la foca NO està sobornada ni bloquejada (vuelve a ser hostil)
			if (!fRival.isSoborno() && fRival.getBloqueix() == 0) {
				registrarEvento(pJugador.getNickname() + " ha topat amb " + fRival.getNickname(), "log-warning");
				int posAbans = pJugador.getPosicio();
				fRival.pegarPingu(pJugador, gestorPartida.getPartida());
				
				if (pJugador.getPosicio() != posAbans) {
					animarRetroceso(pJugador, posAbans, pJugador.getPosicio());
				}
			}
		} else if (j instanceof Foca fAtacante && rival instanceof Pinguino pRival) {
			// Foca CPU topa amb un jugador humà
			// Només interactua si la foca NO està sobornada ni bloquejada
			if (!fAtacante.isSoborno() && fAtacante.getBloqueix() == 0) {
				registrarEvento(fAtacante.getNickname() + " (CPU) ha topat amb " + pRival.getNickname(), "log-warning");
				int posAbans = pRival.getPosicio();
				fAtacante.pegarPingu(pRival, gestorPartida.getPartida());
				
				if (pRival.getPosicio() != posAbans) {
					animarRetroceso(pRival, posAbans, pRival.getPosicio());
				}
			}
		}
	}

	/**
	 * Muestra un overlay de batalla en el centro de la pantalla.
	 */
	public void mostrarOverlayBatalla() {
		try {
			Image img = new Image(getClass().getResourceAsStream("/assets/GestionarBatallaTEXTO.png"));
			ImageView overlay = new ImageView(img);
			overlay.setPreserveRatio(true);
			overlay.setFitWidth(800);
			overlay.setMouseTransparent(true); // No interferir con clics

			Platform.runLater(() -> {
				boardContainer.getChildren().add(overlay);
				javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(1500), overlay);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.setAutoReverse(true);
				ft.setCycleCount(2);
				ft.setOnFinished(e -> boardContainer.getChildren().remove(overlay));
				ft.play();
			});
		} catch (Exception e) {
			System.err.println("Error cargando GestionarBatallaTEXTO: " + e.getMessage());
		}
	}

	/**
	 * Anima el retroceso de un jugador a una nueva posición.
	 */
	public void animarRetroceso(Jugador j, int oldPos, int newPos) {
	    ImageView pieza = getPiezaParaJugador(j);
	    if (pieza == null) return;

	    // Calculamos desplazamientos físicos en el GridPane
	    int oldLogicalRow = oldPos / COLUMNS;
	    int oldLogicalCol = oldPos % COLUMNS;
	    if (oldLogicalRow % 2 != 0) oldLogicalCol = (COLUMNS - 1) - oldLogicalCol;
	    
	    int newLogicalRow = newPos / COLUMNS;
	    int newLogicalCol = newPos % COLUMNS;
	    if (newLogicalRow % 2 != 0) newLogicalCol = (COLUMNS - 1) - newLogicalCol;

	    int oldRow = (ROWS - 1) - oldLogicalRow;
	    int oldCol = oldLogicalCol;
	    int newRow = (ROWS - 1) - newLogicalRow;
	    int newCol = newLogicalCol;

	    double cellWidth = tablero.getPrefWidth() * 0.0918;
	    double cellHeight = tablero.getPrefHeight() * 0.1711;

	    double dx = (newCol - oldCol) * cellWidth;
	    double dy = (newRow - oldRow) * cellHeight;

	    TranslateTransition retreat = new TranslateTransition(Duration.millis(800), pieza);
	    retreat.setByX(dx);
	    retreat.setByY(dy);
	    retreat.setOnFinished(e -> {
	        // Reset translations and let actualizarUI position them correctly with offsets
	        pieza.setTranslateX(0);
	        pieza.setTranslateY(0);
	        actualizarUI();
	    });
	    retreat.play();
	}

	public static void animarRetrocesoEstatico(Jugador j, int oldPos, int newPos) {
		if (instanciaActual != null) {
			// Usamos Platform.runLater por si se llama desde un hilo no-UI, 
			// aunque en este proyecto suele llamarse desde el hilo de aplicaciones
			Platform.runLater(() -> instanciaActual.animarRetroceso(j, oldPos, newPos));
		}
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

		// El consum real es farà a gestorPartida.tirarDau -> dau.tirarIUsar()
		dauSeleccionat = dRapid;
		registrarEvento(pingu.getNickname() + " usa dado rápido (" + dRapid.getMin() + "-" + dRapid.getMax() + ")", "log-info");
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

		dauSeleccionat = dLent;
		String range = (dLent.getMin() == dLent.getMax()) ? String.valueOf(dLent.getMin()) : dLent.getMin() + "-" + dLent.getMax();
		// Si és el dau lent amb valors 1 i 3, el log pot ser més precís
		if (dLent.getNom().equals("Dau lent")) range = "1 o 3";
		
		registrarEvento(pingu.getNickname() + " usa dado lento (" + range + ")", "log-info");
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
	 * Aplica un offset visual para evitar que los pingüinos se solapen completamente
	 * cuando están en la misma casilla.
	 */
	private void aplicarOffsetDeSeparacion(ImageView pieza, int index, int total) {
		if (total <= 1) {
			pieza.setTranslateX(0);
			pieza.setTranslateY(0);
			return;
		}
		
		double offset = 15.0; // Píxeles de separación
		switch (index) {
			case 0: // Arriba-Izquierda
				pieza.setTranslateX(-offset);
				pieza.setTranslateY(-offset);
				break;
			case 1: // Arriba-Derecha
				pieza.setTranslateX(offset);
				pieza.setTranslateY(-offset);
				break;
			case 2: // Abajo-Izquierda
				pieza.setTranslateX(-offset);
				pieza.setTranslateY(offset);
				break;
			case 3: // Abajo-Derecha
				pieza.setTranslateX(offset);
				pieza.setTranslateY(offset);
				break;
			default:
				pieza.setTranslateX(0);
				pieza.setTranslateY(0);
				break;
		}
	}

	/**
	 * Aplica el stylesheet polar del menú a cualquier Alert o Dialog.
	 */
	public void estilar(javafx.scene.control.Dialog<?> d) {
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
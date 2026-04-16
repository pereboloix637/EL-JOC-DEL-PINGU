package vista;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.Interpolator;

import javafx.animation.TranslateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.TextInputDialog;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.sql.Connection;
import javafx.util.Duration;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.layout.Pane;
import javafx.animation.RotateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.FillTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.Group;

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
import controlador.AudioManager;

public class PantallaJuego {

	// Menu items
	@FXML
	private MenuItem saveGame;
	@FXML
	private MenuItem menuItem;
	@FXML
	private MenuItem copySeed;
	@FXML
	private MenuItem menuMute;
	@FXML
	private MenuItem menuMuteSfx;
	@FXML
	private MenuItem menuFullScreen;
	@FXML
	private MenuItem quitGame;
	@FXML
	private MenuItem menuAutoPlay;

	// Flag de modo auto-play (el jugador humano juega solo automáticamente)
	private boolean autoPlayActivo = false;

	

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
	@FXML
	private Button btnPausa;
	@FXML
	private Label lblAutoPlaying;

	// Item count labels
	@FXML
	private Label lblNieve;
	@FXML
	private Label lblRapido;
	@FXML
	private Label lblLento;
	@FXML
	private Label lblPeces;

	// Texts
	@FXML
	private Text dadoResultText;
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
	@FXML
	private ImageView P5;
	@FXML
	private ImageView P6;
	
	private ImageView[] sombras;

	private GestorPartida gestorPartida;
	private static Partida partidaInicial;
	
	private static PantallaJuego instanciaActual;
	private Map<Integer, StackPane> glassTiles = new HashMap<>();

	// Caché de imágenes para evitar recargas constantes
	private static final Map<String, Image> imageCache = new HashMap<>();

	// Dado especial seleccionado para el próximo turno (null = dado estándar)
	private Dau dauSeleccionat = null;

	private static final int COLUMNS = 10;
	private static final int ROWS = 5;

	@FXML
	private GridPane gridInventarios;

	private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";

	public static void setPartidaInicial(Partida p) {
		partidaInicial = p;
	}

	@FXML
	private void initialize() {
		instanciaActual = this;
		updateMuteUI();
		// Cargar imágenes de los pingüinos
		try {
			// Cargar imágenes de forma dinámica según el tipo de jugador si ya hay partida
			// o por defecto si es el inicio absoluto.
			actualizarImagenesIniciales();

			// Ensure centering in GridPane
			ImageView[] piezas = {P1, P2, P3, P4, P5, P6};
			for (ImageView p : piezas) {
				if (p != null) {
					GridPane.setHalignment(p, HPos.CENTER);
					GridPane.setValignment(p, VPos.CENTER);
				}
			}
		} catch (Exception e) {
			System.err.println("Error cargando imágenes de pingüinos: " + e.getMessage());
		}
		
		// --- Inicializar Sombras ---
		try {
			sombras = new ImageView[6];
			Image imgSombra = new Image(getClass().getResourceAsStream("/assets/sombra.png"));
			for (int i = 0; i < 6; i++) {
				ImageView s = new ImageView(imgSombra);
				s.setFitWidth(50);
				s.setFitHeight(20);
				s.setOpacity(0.6);
				s.setMouseTransparent(true);
				s.setVisible(false);
				sombras[i] = s;
				// Añadir al tablero al principio para que queden detrás (índice 0)
				tablero.getChildren().add(0, s);
				GridPane.setHalignment(s, HPos.CENTER);
				GridPane.setValignment(s, VPos.CENTER);
			}
		} catch (Exception e) {
			System.err.println("Error cargando sombras: " + e.getMessage());
		}
		// --------------------------
		// ── Escalado dinámico PERFECTO para Laptops sin romper la config base ──
		boardRoot.getChildren().clear();
		boardRoot.setMinSize(0, 0);

		javafx.scene.layout.Pane wrapper = new javafx.scene.layout.Pane();
		boardRoot.getChildren().add(wrapper);

		wrapper.getChildren().add(boardContainer);

		javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(1, 1, 0, 0);
		boardContainer.getTransforms().clear();
		boardContainer.getTransforms().add(scaleTransform);

		javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
		    double w = wrapper.getWidth();
		    double h = wrapper.getHeight();
		    if (w == 0 || h == 0) return;

		    double scaleFactor = Math.min(w / 1920.0, h / 1080.0);
		    scaleTransform.setX(scaleFactor);
		    scaleTransform.setY(scaleFactor);

		    double scaledWidth = 1920.0 * scaleFactor;
		    double scaledHeight = 1080.0 * scaleFactor;
		    boardContainer.setLayoutX((w - scaledWidth) / 2.0);
		    boardContainer.setLayoutY((h - scaledHeight) / 2.0);
		};

		wrapper.widthProperty().addListener(resizeListener);
		wrapper.heightProperty().addListener(resizeListener);
		
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

		// Mostrar info del tablero con un pequeño retraso para no bloquear la animación inicial
		PauseTransition boardDelay = new PauseTransition(Duration.millis(500));
		boardDelay.setOnFinished(e -> {
			mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTaulell());
			actualizarUI();
		});
		boardDelay.play();
		
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
		P5.setVisible(js.size() > 4);
		P6.setVisible(js.size() > 5);
		
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
				// Subir la pieza usando margen (no translateY, para no interferir con animaciones)
				GridPane.setMargin(pieza, new javafx.geometry.Insets(0, 0, 40, 0));

				// Sincronizar sombra
				ImageView sombra = getSombraParaJugador(j);
				if (sombra != null) {
					sombra.setVisible(pieza.isVisible());
					GridPane.setRowIndex(sombra, row);
					GridPane.setColumnIndex(sombra, col);
					aplicarOffsetDeSeparacion(sombra, numEnCasilla, recuento.get(pos));
					// La sombra queda en el suelo (un poco debajo del personaje)
					GridPane.setMargin(sombra, new javafx.geometry.Insets(0, 0, 10, 0));
				}
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
		if (instanciaActual != null) {
			instanciaActual.estilar(d);
		}
	}

	/**
	 * Registra un esdeveniment al log visual.
	 */
	public void registrarEvento(String mensaje, String styleClass) {
		Platform.runLater(() -> {
			Label lbl = new Label("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + mensaje);
			lbl.getStyleClass().add(styleClass);
			lbl.setWrapText(true);
			lbl.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
			logEventos.getChildren().add(lbl);
			// Scroll automàtic al final
			scrollEventos.setVvalue(1.0);
		});
	}

	public static void mostrarOverlayBatallaEstatico() {
		if (instanciaActual != null) {
			instanciaActual.mostrarOverlayBatalla(null);
		}
	}

	public static void mostrarRuletaEstatico(Jugador j, int itemIndex, Runnable onFinished) {
		if (instanciaActual != null) {
			Platform.runLater(() -> instanciaActual.mostrarRuletaItem(j, itemIndex, onFinished));
		}
	}

	public static void mostrarRuletaMalvadaEstatico(Jugador j, int actionIndex, Runnable onFinished) {
		if (instanciaActual != null) {
			Platform.runLater(() -> instanciaActual.mostrarRuletaMalvada(j, actionIndex, onFinished));
		}
	}

	public static void actualizarUIEstatica() {
		if (instanciaActual != null) {
			Platform.runLater(() -> instanciaActual.actualizarUI());
		}
	}

	/**
	 * Habilita o deshabilita todos los controles de interacción del jugador.
	 */
	private void bloquearControles(boolean bloquear) {
		if (dado != null) {
			dado.setDisable(bloquear);
		}
		if (rapido != null) {
			rapido.setDisable(bloquear);
		}
		if (lento != null) {
			lento.setDisable(bloquear);
		}
		if (peces != null) {
			peces.setDisable(bloquear);
		}
		if (nieve != null) {
			nieve.setDisable(bloquear);
		}
		if (btnPausa != null) {
			btnPausa.setDisable(bloquear);
		}
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

		// Calcular totals de daus especials: ràpid (max > 6) i lent (max <= 3)
		int totalRapido = 0;
		int totalLento = 0;

		for (model.items.Item obj : inv.getLlista()) {
			if (obj instanceof Dau d) {
				if (d.getMax() > 6) {
					totalRapido += d.getQuantitat();
				}
				if (d.getMax() <= 3) {
					totalLento += d.getQuantitat();
				}
			}
		}

		// Habilitar/deshabilitar botons (null-safe)
		if (rapido != null) {
			rapido.setDisable(totalRapido <= 0);
		}
		if (lento != null) {
			lento.setDisable(totalLento <= 0);
		}
		if (peces != null) {
			peces.setDisable(inv.getPeixos() <= 0);
		}
		if (nieve != null) {
			nieve.setDisable(inv.getBoles() <= 0);
		}

		// Actualitzar comptadors sobre els botons
		if (lblRapido != null) {
			lblRapido.setText(String.valueOf(totalRapido));
		}
		if (lblLento != null) {
			lblLento.setText(String.valueOf(totalLento));
		}
		if (lblPeces != null) {
			lblPeces.setText(String.valueOf(inv.getPeixos()));
		}
		if (lblNieve != null) {
			lblNieve.setText(String.valueOf(inv.getBoles()));
		}
	}

	private void actualizarSidebarJugadores() {
		gridInventarios.getChildren().clear();
		
		Partida pActual = gestorPartida.getPartida();
		ArrayList<Jugador> js = pActual.getJugadors();
		
		for (int i = 0; i < js.size(); i++) {
			Jugador j = js.get(i);
			VBox card = new VBox(5);
			card.getStyleClass().add("player-status-card");
			
			// Highlight del jugador amb torn actiu
			if (j == pActual.getJugadorActual()) {
				card.getStyleClass().add("is-current-turn");
			}

			HBox header = new HBox(10);
			header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

			// Imagen del personaje en lugar del círculo de color
			ImageView charIcon = new ImageView(obtenerImagenJugador(j));
			charIcon.setFitWidth(32);
			charIcon.setFitHeight(32);
			charIcon.setPreserveRatio(true);
			charIcon.setSmooth(false);
			
			Label name = new Label(j.getNickname());
			name.getStyleClass().add("player-name");
			
			header.getChildren().addAll(charIcon, name);
			card.getChildren().add(header);

			if (j instanceof Pinguino p) {
				VBox inv = new VBox(2);
				inv.getStyleClass().add("player-inv-mini");
				
				Label peces = new Label("Peces: " + p.getInventari().getPeixos());
				Label boles = new Label("Bolas: " + p.getInventari().getBoles());
				Label daus = new Label("Dados: " + p.getInventari().getDausEspecials());
				
				inv.getChildren().addAll(peces, boles, daus);
				card.getChildren().add(inv);
			} else if (j instanceof Foca f) {
				// Mostrar nombre como Foca 1, Foca 2, etc.
				int focaNum = 0;
				for (int k = 0; k <= i; k++) {
					if (js.get(k) instanceof Foca) focaNum++;
				}
				name.setText("Foca " + focaNum);
				
				// AHORA TAMBIÉN MOSTRAR EL INVENTARIO DE LA FOCA
				VBox inv = new VBox(2);
				inv.getStyleClass().add("player-inv-mini");
				
				Label peces = new Label("Peces: " + f.getInventari().getPeixos());
				Label boles = new Label("Bolas: " + f.getInventari().getBoles());
				Label daus = new Label("Dados: " + f.getInventari().getDausEspecials());
				
				Label cpuLabel = new Label("(CPU)");
				cpuLabel.getStyleClass().add("cpu-label");
				
				inv.getChildren().addAll(peces, boles, daus, cpuLabel);
				card.getChildren().add(inv);
			}

			// Posicionar en la cuadrícula 2x3
			int col = i % 2;
			int row = i / 2;
			gridInventarios.add(card, col, row);
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
			case 4: return P5;
			case 5: return P6;
			default: return null;
		}
	}

	/**
	 * Retorna l'element visual (ImageView) de la sombra associat a un jugador.
	 */
	private ImageView getSombraParaJugador(Jugador j) {
		int idx = gestorPartida.getPartida().getJugadors().indexOf(j);
		if (idx >= 0 && idx < 6 && sombras != null) {
			return sombras[idx];
		}
		return null;
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
			if (colorStr.equalsIgnoreCase("Morado")) fColor = "LIila";
			
			filename = "Foca" + fColor + ".png";
		} else {
			// Morado y Naranja usan espacio en el nombre del archivo
			if (colorStr.equalsIgnoreCase("Morado") || colorStr.equalsIgnoreCase("Naranja")) {
				filename = "PINGUINO " + colorStr.toUpperCase() + ".png";
			} else {
				filename = "PINGUINO_" + colorStr.toUpperCase() + ".png";
			}
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
			P1.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_ROJO.png"))); // P1.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_ROJO.png"))); | P1.setImage(new Image(getClass().getResourceAsStream("/assets/PenguinPyce_2D.png")));
			P2.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_AZUL.png")));
			P3.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_VERDE.png")));
			P4.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO_AMARILLO.png")));
			P5.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO MORADO.png")));
			P6.setImage(new Image(getClass().getResourceAsStream("/assets/PINGUINO NARANJA.png")));
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
			case 4: return P5;
			case 5: return P6;
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
			case "morado":   return "#8E44AD";
			case "naranja":  return "#E67E22";
			default:         return "#FFFFFF";
		}
	}

    private void mostrarTiposDeCasillasEnTablero(Taulell t) {
        // Limpiar celdas previas
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));
        glassTiles.clear();

        int total = t.getCaselles().size();
        int batchSize = 10;

        new Thread(() -> {
            for (int i = 0; i < total; i += batchSize) {
                final int start = i;
                final int end = Math.min(i + batchSize, total);

                Platform.runLater(() -> {
                    for (int j = start; j < end; j++) {
                        Casella casilla = t.getCaselles().get(j);
                        StackPane iceBlock = createCellNode(j, casilla, total);
                        tablero.getChildren().add(0, iceBlock);
                        glassTiles.put(j, iceBlock);
                    }
                });

                try {
                    Thread.sleep(16); // Aproximadamente un frame para dejar respirar a la UI
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();
    }

    private StackPane createCellNode(int i, Casella casilla, int total) {
        StackPane iceBlock = new StackPane();
        iceBlock.setUserData(TAG_CASILLA_TEXT);
        iceBlock.getStyleClass().add("board-cell");
        iceBlock.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setFillWidth(iceBlock, true);
        GridPane.setFillHeight(iceBlock, true);

        if (i == 0) {
            iceBlock.getStyleClass().add("start-cell");
        } else if (i == total - 1) {
            iceBlock.getStyleClass().add("finish-cell");
        } else {
            iceBlock.getStyleClass().add("cell-" + casilla.getClass().getSimpleName());
        }

        int logicalRow = i / COLUMNS;
        int logicalCol = i % COLUMNS;
        if (logicalRow % 2 != 0) {
            logicalCol = (COLUMNS - 1) - logicalCol;
        }
        int row = (ROWS - 1) - logicalRow;
        int col = logicalCol;

        GridPane.setRowIndex(iceBlock, row);
        GridPane.setColumnIndex(iceBlock, col);
        
        return iceBlock;
    }


	public static void mostrarPopupItem(Jugador j, String imagenNombre) {
		if (instanciaActual == null) {
			return;
		}
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

	/**
	 * Mostra l'animació d'atac de l'ós canviant la imatge de la casella.
	 */
	public static void mostrarAtaqueOso(int pos) {
		if (instanciaActual == null) return;
		Platform.runLater(() -> instanciaActual.ejecutarAtaqueOso(pos));
	}

	private void ejecutarAtaqueOso(int pos) {
		StackPane tile = glassTiles.get(pos);
		if (tile != null) {
			tile.getStyleClass().add("cell-Os-attacking");
			PauseTransition pause = new PauseTransition(Duration.seconds(2.0));
			pause.setOnFinished(e -> tile.getStyleClass().remove("cell-Os-attacking"));
			pause.play();
		}
	}

	/**
	 * Mostra una ruleta estil pixel-art per determinar quin item rep el jugador.
	 */
	public void mostrarRuletaItem(Jugador j, int itemIndex, Runnable onFinished) {
		StackPane rootOverlay = boardRoot;
		
		// Fondo oscuro
		Pane dim = new Pane();
		dim.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
		dim.setOpacity(0);
		
		VBox rouletteContainer = new VBox(25);
		rouletteContainer.setAlignment(javafx.geometry.Pos.CENTER);
		rouletteContainer.setMaxSize(750, 750);
		
		Label title = new Label("¡RULETA!");
		title.getStyleClass().add("big-text-mini");
		title.setStyle("-fx-text-fill: whitesmoke; -fx-font-family: 'Press Start 2P'; -fx-font-size: 70px; -fx-effect: dropshadow(three-pass-box, #000000, 4, 0, 2, 2);");
		
		StackPane wheelStack = new StackPane();
		
		// Marco de la rueda proporcionado por el usuario
		ImageView wheelVisuals = new ImageView(new Image(getClass().getResourceAsStream("/assets/RULETA_ALEATORIA.png")));
		wheelVisuals.setFitWidth(500);
		wheelVisuals.setFitHeight(500);
		wheelVisuals.setPreserveRatio(true);
		wheelVisuals.setSmooth(false); // Para mantener el estilo pixelart sin blur
		
		// Iconos de los items en la rueda
		Pane itemsPane = new Pane();
		itemsPane.setPrefSize(500, 500);
		
		String[] itemImages = {"Pez.png", "BolasNieve.png", "Dado_Rapido.png", "Dado_Lento.png", "MOTO_DE_NIEVE.png", "perder_turno.png", "perder_item.png"};
		double angleStep = 360.0 / 7.0;
		for (int i = 0; i < 7; i++) {
			ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/assets/" + itemImages[i])));
			
			// Hacemos la moto de nieve más destacada
			double size = itemImages[i].equals("MOTO_DE_NIEVE.png") ? 160 : 110;
			iv.setFitWidth(size);
			iv.setFitHeight(size);
			iv.setPreserveRatio(true);
			
			// Posicionar en los cuadrantes (Dividido por 7)
			double angle = Math.toRadians(i * angleStep);
			iv.setLayoutX(250 + 165 * Math.cos(angle) - (size / 2));
			iv.setLayoutY(250 + 165 * Math.sin(angle) - (size / 2));
			iv.setRotate(i * angleStep + 90);
			itemsPane.getChildren().add(iv);
		}
		
		// Envolvemos la parte que gira en un StackPane separado
		StackPane rotatingWheel = new StackPane(wheelVisuals, itemsPane);
		rotatingWheel.setMaxSize(500, 500);
		
		// Puntero (flecha asset) - Lo centramos respecto a la rueda usando un StackPane común
		ImageView pointer = new ImageView(new Image(getClass().getResourceAsStream("/assets/Flecha ruleta.png")));
		pointer.setFitWidth(100);
		pointer.setPreserveRatio(true);
		pointer.setSmooth(false); 
		// Ajustamos el translateY respecto al centro (0,0) de la rueda
		pointer.setTranslateY(-30); 
		
		StackPane wheelAndPointer = new StackPane(rotatingWheel, pointer);
		wheelAndPointer.setMaxSize(500, 500);
		
		rouletteContainer.getChildren().addAll(title, wheelAndPointer);
		
		rootOverlay.getChildren().addAll(dim, rouletteContainer);
		
		// Animación entrada
		FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dim);
		fadeIn.setToValue(1);
		fadeIn.play();
		
		ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), rouletteContainer);
		scaleIn.setFromX(0); scaleIn.setFromY(0);
		scaleIn.setToX(1); scaleIn.setToY(1);
		scaleIn.play();
		
		// Animación giro (Ahora apunta a rotatingWheel en lugar del contenedor completo)
		int rotations = 5 + new Random().nextInt(3);
		// El index 0 está en 0 grados (derecha). El puntero está arriba (-90 grados).
		// Para que el index I quede arriba, rotamos -90 - (I * angleStep)
		double targetAngle = rotations * 360 - (itemIndex * angleStep) - 90;
		
		RotateTransition rotate = new RotateTransition(Duration.seconds(3), rotatingWheel);
		rotate.setByAngle(targetAngle);
		rotate.setInterpolator(Interpolator.SPLINE(0.1, 0.5, 0.2, 1)); // Slow down effect
		
		rotate.setOnFinished(e -> {
			lanzarConfeti(boardRoot);
			
			PauseTransition wait = new PauseTransition(Duration.seconds(2));
			wait.setOnFinished(e2 -> {
				FadeTransition fadeOut = new FadeTransition(Duration.millis(300), dim);
				fadeOut.setToValue(0);
				FadeTransition fadeOutC = new FadeTransition(Duration.millis(300), rouletteContainer);
				fadeOutC.setToValue(0);
				fadeOutC.setOnFinished(e3 -> {
					rootOverlay.getChildren().removeAll(dim, rouletteContainer);
					if (onFinished != null) onFinished.run();
				});
				fadeOut.play();
				fadeOutC.play();
			});
			wait.play();
		});
		
		PauseTransition startWait = new PauseTransition(Duration.seconds(0.5));
		startWait.setOnFinished(e -> rotate.play());
		startWait.play();
	}

	/**
	 * Mostra la Ruleta Malvada de la Foca amb 2 opcions.
	 */
	public void mostrarRuletaMalvada(Jugador j, int actionIndex, Runnable onFinished) {
		StackPane rootOverlay = boardRoot;
		
		Pane dim = new Pane();
		dim.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
		dim.setOpacity(0);
		
		VBox rouletteContainer = new VBox(25);
		rouletteContainer.setAlignment(javafx.geometry.Pos.CENTER);
		rouletteContainer.setMaxSize(750, 750);
		
		Label title = new Label("¡FOCA ATACA!");
		title.getStyleClass().add("big-text-mini");
		title.setStyle("-fx-text-fill: whitesmoke; -fx-font-family: 'Press Start 2P'; -fx-font-size: 70px; -fx-effect: dropshadow(three-pass-box, #000000, 4, 0, 2, 2);");
		
		StackPane wheelStack = new StackPane();
		
		// Unificamos con la lógica de items
		String wheelPath = "/assets/Ruleta_Malvada.png";
		ImageView wheelVisuals = new ImageView(new Image(getClass().getResourceAsStream(wheelPath)));
		wheelVisuals.setFitWidth(500);
		wheelVisuals.setFitHeight(500);
		wheelVisuals.setPreserveRatio(true);
		wheelVisuals.setSmooth(false);
		
		Pane itemsPane = new Pane();
		itemsPane.setPrefSize(500, 500);
		
		// Dues opcions: 0=Pegar, 1=Aplastar
		String[] options = {"PegarPingu.png", "AplastarPingu.png"};
		for (int i = 0; i < options.length; i++) {
			ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/assets/" + options[i])));
			iv.setFitWidth(160);
			iv.setFitHeight(160);
			iv.setPreserveRatio(true);
			
			// Colocamos en 0° (Derecha) y 180° (Izquierda) para seguir la lógica de la ruleta base
			double angle = Math.toRadians(i * 180);
			iv.setLayoutX(250 + 160 * Math.cos(angle) - 80);
			iv.setLayoutY(250 + 160 * Math.sin(angle) - 80);
			iv.setRotate(i * 180 + 90); // Mantenemos los iconos rectos al llegar arriba
			itemsPane.getChildren().add(iv);
		}
		
		// Unificamos estructura con mostrarRuletaItem
		StackPane rotatingWheel = new StackPane(wheelVisuals, itemsPane);
		rotatingWheel.setMaxSize(500, 500);
		
		// Puntero uniforme
		ImageView pointer = new ImageView(new Image(getClass().getResourceAsStream("/assets/Flecha ruleta.png")));
		pointer.setFitWidth(100);
		pointer.setPreserveRatio(true);
		pointer.setSmooth(false);
		pointer.setTranslateY(-30);
		
		StackPane wheelAndPointer = new StackPane(rotatingWheel, pointer);
		wheelAndPointer.setMaxSize(500, 500);
		
		rouletteContainer.getChildren().addAll(title, wheelAndPointer);
		rootOverlay.getChildren().addAll(dim, rouletteContainer);
		
		FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dim);
		fadeIn.setToValue(1); fadeIn.play();
		
		ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), rouletteContainer);
		scaleIn.setFromX(0); scaleIn.setFromY(0); scaleIn.setToX(1); scaleIn.setToY(1);
		scaleIn.play();
		
		int rotations = 6 + new Random().nextInt(3);
		double targetAngle = (rotations * 360) - (actionIndex * 180) - 90;
		
		RotateTransition rotate = new RotateTransition(Duration.seconds(3), rotatingWheel);
		rotate.setByAngle(targetAngle);
		rotate.setInterpolator(Interpolator.SPLINE(0.1, 0.5, 0.2, 1));
		
		rotate.setOnFinished(e -> {
			PauseTransition wait = new PauseTransition(Duration.seconds(2));
			wait.setOnFinished(e2 -> {
				FadeTransition fadeOut = new FadeTransition(Duration.millis(300), dim);
				fadeOut.setToValue(0);
				FadeTransition fadeOutC = new FadeTransition(Duration.millis(300), rouletteContainer);
				fadeOutC.setToValue(0);
				fadeOutC.setOnFinished(e3 -> {
					rootOverlay.getChildren().removeAll(dim, rouletteContainer);
					if (onFinished != null) onFinished.run();
				});
				fadeOut.play();
				fadeOutC.play();
			});
			wait.play();
		});
		
		PauseTransition startWait = new PauseTransition(Duration.seconds(0.5));
		startWait.setOnFinished(e -> rotate.play());
		startWait.play();
	}

	private void lanzarConfeti(StackPane parent) {
		Pane confettiLayer = new Pane();
		confettiLayer.setMouseTransparent(true);
		parent.getChildren().add(confettiLayer);
		
		Random rnd = new Random();
		Color[] colors = {Color.RED, Color.GOLD, Color.CYAN, Color.LIME, Color.MAGENTA, Color.ORANGE};
		
		for (int i = 0; i < 100; i++) {
			Rectangle r = new Rectangle(8, 8, colors[rnd.nextInt(colors.length)]);
			r.setLayoutX(parent.getWidth() / 2);
			r.setLayoutY(parent.getHeight() / 2 - 50);
			confettiLayer.getChildren().add(r);
			
			double angle = rnd.nextDouble() * 360;
			double distance = 100 + rnd.nextDouble() * 400;
			double x = Math.cos(Math.toRadians(angle)) * distance;
			double y = Math.sin(Math.toRadians(angle)) * distance - 200; // Un poco hacia arriba inicial
			
			TranslateTransition tt = new TranslateTransition(Duration.seconds(1 + rnd.nextDouble() * 1.5), r);
			tt.setByX(x);
			tt.setByY(y);
			
			FadeTransition ft = new FadeTransition(Duration.seconds(1.5 + rnd.nextDouble()), r);
			ft.setFromValue(1);
			ft.setToValue(0);
			
			RotateTransition rt = new RotateTransition(Duration.seconds(1 + rnd.nextDouble()), r);
			rt.setByAngle(360 + rnd.nextInt(720));
			
			ParallelTransition pt = new ParallelTransition(r, tt, ft, rt);
			pt.play();
		}
		
		PauseTransition cleanup = new PauseTransition(Duration.seconds(4));
		cleanup.setOnFinished(e -> parent.getChildren().remove(confettiLayer));
		cleanup.play();
	}

	@FXML
	private void handleToggleMute(ActionEvent event) {
		AudioManager.getInstance().toggleMusicMute();
		updateMuteUI();
	}

	@FXML
	private void handleToggleSfxMute(ActionEvent event) {
		AudioManager.getInstance().toggleSfxMute();
		updateMuteUI();
	}

	@FXML
	private void handleToggleFullScreen(ActionEvent event) {
		boolean current = controlador.Main.isFullScreenEnabled();
		controlador.Main.setFullScreenEnabled(!current);
		updateMuteUI();
	}

	private void updateMuteUI() {
		if (menuMute != null) {
			boolean musicMuted = AudioManager.getInstance().isMusicMuted();
			menuMute.setText(musicMuted ? "Música: OFF" : "Música: ON");
		}
		if (menuMuteSfx != null) {
			boolean sfxMuted = AudioManager.getInstance().isSfxMuted();
			menuMuteSfx.setText(sfxMuted ? "Efectos: OFF" : "Efectos: ON");
		}
		if (menuFullScreen != null) {
			boolean fs = controlador.Main.isFullScreenEnabled();
			menuFullScreen.setText(fs ? "Pantalla Completa: ON" : "Pantalla Completa: OFF");
		}
	}


	// Menu actions

	private Connection getBDConnection() {
		return GestorBBDD.conectarBaseDatos();
	}

	@FXML
	private void handleSaveGame() {
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
	private void handleCopySeed() {
		mostrarVentanaSeed();
	}

	private void exportarSemillaLogica(String seed) {
		javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
		fileChooser.setTitle("Exportar Semilla");
		fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Archivo de texto (*.txt)", "*.txt"));
		fileChooser.setInitialFileName("semilla_pingu.txt");
		
		java.io.File file = fileChooser.showSaveDialog(boardContainer.getScene().getWindow());
		
		if (file != null) {
			try {
				java.nio.file.Files.writeString(file.toPath(), seed);
				registrarEvento("Semilla exportada a: " + file.getName(), "log-info");
			} catch (Exception e) {
				registrarEvento("Error al exportar la semilla.", "log-warning");
			}
		}
	}

	private void mostrarVentanaSeed() {
		GestorTaulell gt = new GestorTaulell();
		String seed = gt.obtenirSeedTaulell(gestorPartida.getPartida().getTaulell());
		
		TextInputDialog dialog = new TextInputDialog(seed);
		estilar(dialog);
		dialog.setTitle("Semilla de la Partida");
		dialog.setHeaderText("Copia esta semilla para volver a jugar en este tablero:");
		dialog.setContentText("Semilla:");
		
		// Añadir botón de "Exportar a .txt"
		ButtonType btnExportar = new ButtonType("Exportar a .txt", javafx.scene.control.ButtonBar.ButtonData.LEFT);
		dialog.getDialogPane().getButtonTypes().add(btnExportar);
		
		Button exportButton = (Button) dialog.getDialogPane().lookupButton(btnExportar);
		exportButton.addEventFilter(ActionEvent.ACTION, event -> {
			event.consume(); // Evitar que el diálogo se cierre
			exportarSemillaLogica(seed);
		});
		
		dialog.showAndWait();
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
			controlador.Main.cambiarEscena("/resources/PantallaMenu.fxml", true);
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

	@FXML
	private void handleToggleAutoPlay() {
		autoPlayActivo = !autoPlayActivo;
		updateAutoPlayUI();
		if (autoPlayActivo) {
			registrarEvento("Auto-Play ACTIVADO. El pingüino jugará solo.", "log-info");
			// Si ya es el turno de un Pinguino, disparar inmediatamente
			if (gestorPartida.getPartida().getJugadorActual() instanceof Pinguino) {
				checkTurnoCPU();
			}
		} else {
			registrarEvento("Auto-Play DESACTIVADO.", "log-info");
		}
	}

	private void updateAutoPlayUI() {
		if (menuAutoPlay == null)
			return;
		if (autoPlayActivo) {
			menuAutoPlay.setText("⏹ Desactivar Auto-Play");
			menuAutoPlay.setStyle("-fx-text-fill: #e74c3c;");
			// Mostrar indicador con fade-in
			if (lblAutoPlaying != null) {
				lblAutoPlaying.setVisible(true);
				lblAutoPlaying.setOpacity(0);
				FadeTransition ft = new FadeTransition(Duration.millis(400), lblAutoPlaying);
				ft.setToValue(1.0);
				ft.play();
				// Pulso continuo para que llame la atención
				FadeTransition pulse = new FadeTransition(Duration.seconds(1.2), lblAutoPlaying);
				pulse.setFromValue(1.0);
				pulse.setToValue(0.35);
				pulse.setAutoReverse(true);
				pulse.setCycleCount(FadeTransition.INDEFINITE);
				lblAutoPlaying.setUserData(pulse);
				ft.setOnFinished(e -> pulse.play());
			}
		} else {
			menuAutoPlay.setText("▶ Activar Auto-Play");
			menuAutoPlay.setStyle("-fx-text-fill: #2ecc71;");
			// Ocultar indicador con fade-out
			if (lblAutoPlaying != null) {
				// Detener el pulso si estaba activo
				if (lblAutoPlaying.getUserData() instanceof FadeTransition pulse) {
					pulse.stop();
				}
				FadeTransition ft = new FadeTransition(Duration.millis(400), lblAutoPlaying);
				ft.setToValue(0.0);
				ft.setOnFinished(e -> lblAutoPlaying.setVisible(false));
				ft.play();
			}
		}
	}


	// Button actions
	@FXML
	private void handleDado(ActionEvent event) {
	    if (gestorPartida.getPartida().getJugadorActual() instanceof Pinguino) {
	        executartorn();
	    }
	}

	@FXML
	private void handlePausa() {
		bloquearControles(true);

		Alert pausaAlert = new Alert(AlertType.CONFIRMATION);
		estilar(pausaAlert);
		pausaAlert.setTitle("Pausa");
		pausaAlert.setHeaderText("Juego en Pausa");
		pausaAlert.setContentText("¿Qué deseas hacer?");

		String autoPlayBtnText = autoPlayActivo
				? "⏹ Desactivar Auto-Play"
				: "▶ Activar Auto-Play";

		ButtonType btnContinuar  = new ButtonType("Continuar");
		ButtonType btnAutoPlay   = new ButtonType(autoPlayBtnText);
		ButtonType btnMenu       = new ButtonType("Ir al Menú");
		pausaAlert.getButtonTypes().setAll(btnContinuar, btnAutoPlay, btnMenu);

		// Colorear el botón de auto-play después de que el diálogo cree los nodos
		pausaAlert.getDialogPane().lookupButton(btnAutoPlay).setStyle(
				autoPlayActivo
						? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
						: "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
		);

		pausaAlert.showAndWait().ifPresent(result -> {
			if (result == btnMenu) {
				handleGoToMenu();
			} else if (result == btnAutoPlay) {
				handleToggleAutoPlay();
			} else {
				// "Continuar" o cierre directo
				actualizarUI();
			}
		});
	}

	/**
	 * Mostra un banner animat anunciant el torn del jugador.
	 * El banner entra des de l'esquerra, es queda al centre, i surt per la dreta.
	 */
	private void mostrarBannerTurno(Jugador jugador, Runnable onComplete) {
		HBox banner = new HBox(25);
		banner.setAlignment(javafx.geometry.Pos.CENTER);
		banner.getStyleClass().add("turn-banner");
		banner.setMaxHeight(110);
		banner.setMaxWidth(420);
		banner.setMouseTransparent(true);

		// Imagen del personaje del jugador
		try {
			Image img = obtenerImagenJugador(jugador);
			if (img != null) {
				ImageView playerImg = new ImageView(img);
				playerImg.setFitWidth(90);
				playerImg.setFitHeight(90);
				playerImg.setPreserveRatio(true);
				playerImg.setSmooth(false);
				banner.getChildren().add(playerImg);
			}
		} catch (Exception e) {
			// Continuar sin imagen si falla
		}

		// Textos del banner
		VBox textBox = new VBox(8);
		textBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

		Label turnLabel = new Label("Turno de:");
		turnLabel.setStyle("-fx-text-fill: #7FD4F0; -fx-font-family: 'Press Start 2P'; -fx-font-size: 16px;");

		Label nameLabel = new Label(jugador.getNickname());
		nameLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Press Start 2P'; -fx-font-size: 24px; -fx-effect: dropshadow(three-pass-box, #000000, 3, 0, 2, 2);");

		textBox.getChildren().addAll(turnLabel, nameLabel);
		banner.getChildren().add(textBox);

		// Empezar fuera de pantalla a la izquierda
		banner.setTranslateX(-2000);

		boardRoot.getChildren().add(banner);

		// Animación: entrar desde la izquierda
		TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), banner);
		slideIn.setToX(0);
		slideIn.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1));

		// Pausa en el centro
		PauseTransition pausaCentro = new PauseTransition(Duration.seconds(1.5));

		// Salir hacia la derecha
		TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), banner);
		slideOut.setToX(2000);
		slideOut.setInterpolator(Interpolator.SPLINE(0.55, 0.0, 0.675, 0.19));

		SequentialTransition seq = new SequentialTransition(slideIn, pausaCentro, slideOut);
		seq.setOnFinished(e -> {
			boardRoot.getChildren().remove(banner);
			if (onComplete != null) onComplete.run();
		});
		seq.play();
	}

	private void executartorn() {
		Partida p = gestorPartida.getPartida();
		Jugador actual = p.getJugadorActual();
		
		if (p.isFinalitzada()) {
			registrarEvento("¡Partida finalizada! Ganador: " + p.getGuanyador().getNickname(), "log-warning");
			return;
		}

		// ── INICIO TOMA DE DECISIONES DE LA FOCA (IA) Y SALTO DE TURNO ──
		if (actual instanceof model.entitats.Foca f) {
			
			// 1. La foca analiza el tablero y decide si usar algún objeto (Dados/Bolas) táctico.
			Dau dIa = f.jugarTurnoTactico(p);
			if (dIa != null) {
				dauSeleccionat = dIa; // Se vincula el dado inteligente para que sea tirado después.
			}
			
			// 2. Si después de todo la foca estuviese bajo efecto de hielo u oso (bloqueada), se detiene aquí.
			if (f.getBloqueix() > 0) {
				int restan = f.getBloqueix() - 1;
				f.setBloqueix(restan);
				registrarEvento(f.getNickname() + " está bloqueada y no se mueve. Turnos restantes: " + restan, "log-info");
				finalizarTurno(f);
				return;
			}
		// ── FIN LÓGICA IA FOCA ──
		} else if (actual instanceof Pinguino && actual.getTornsBloquejat() > 0) {
			int restan = actual.getTornsBloquejat() - 1;
			actual.setTornsBloquejat(restan);
			registrarEvento(actual.getNickname() + " está bloqueado y no puede moverse. Turnos restantes: " + restan, "log-warning");
			finalizarTurno(actual);
			return;
		}

		bloquearControles(true);
		registrarEvento("Turno de: " + actual.getNickname(), "log-turn");

		// Mostrar banner animado de turno y después proceder con el dado
		mostrarBannerTurno(actual, () -> {
			Dau d;
			if (dauSeleccionat != null) {
				d = dauSeleccionat;
				dauSeleccionat = null; 
			} else {
				d = new Dau();
			}

			int resultado = gestorPartida.tirarDau(actual, d);
			dadoResultText.setText("Dado: " + resultado);
			
			if (d.esEspecial()) {
			    if (d.getMax() > 6) {
			        dadoResultText.setStyle("-fx-fill: #E67E22;"); 
			    } else if (d.getMax() <= 3) {
			        dadoResultText.setStyle("-fx-fill: #27AE60;"); 
			    }
			} else {
			    dadoResultText.setStyle("-fx-fill: white;");
			}
			
			moverPieza(actual, resultado);
		});
	}

	private void moverPieza(Jugador j, int steps) {
		if (steps <= 0) {
			return;
		}
		bloquearControles(true);
	    ImageView pieza = getPiezaParaJugador(j);
	    if (pieza == null) {
			return;
		}

	    int oldPos = j.getPosicio();
	    int totalCaselles = gestorPartida.getPartida().getTaulell().getCaselles().size();
	    int indexFinal = totalCaselles - 1;
	    int newPos;
	    
	    if (oldPos + steps > indexFinal) {
	        int sobrante = (oldPos + steps) - indexFinal;
	        newPos = indexFinal - sobrante;
	    } else {
	        newPos = oldPos + steps;
	    }

	    int numEnNuevaCasilla = 0;
	    ArrayList<Jugador> js = gestorPartida.getPartida().getJugadors();
	    for (int i = 0; i < js.indexOf(j); i++) {
	    	if (js.get(i).getPosicio() == newPos) {
	    		numEnNuevaCasilla++;
	    	}
	    }
	    
	    double targetTX = 0, targetTY = 0;
	    double offsetSeparacion = 15.0;
	    int totalEnDestino = 0;
	    for (Jugador other : js) {
			if (other.getPosicio() == newPos) {
				totalEnDestino++;
			}
	    }
	    totalEnDestino++; 

	    if (totalEnDestino > 1) {
		    switch (numEnNuevaCasilla) {
		    	case 0: targetTX = -offsetSeparacion; targetTY = -offsetSeparacion; break;
		    	case 1: targetTX =  offsetSeparacion; targetTY = -offsetSeparacion; break;
		    	case 2: targetTX = -offsetSeparacion; targetTY =  offsetSeparacion; break;
		    	case 3: targetTX =  offsetSeparacion; targetTY =  offsetSeparacion; break;
		    }
	    }

	    double currentTX = pieza.getTranslateX();
	    double currentTY = pieza.getTranslateY();
	    
	    SequentialTransition sequence = new SequentialTransition();
	    double cellWidth = tablero.getPrefWidth() * 0.10;   
	    double cellHeight = tablero.getPrefHeight() * 0.20; 

	    double accumTX = currentTX;
	    double accumTY = currentTY;

	    for (int i = 1; i <= steps; i++) {
	        int pA, pB;
	        if (oldPos + i <= indexFinal) {
	            pA = oldPos + i - 1;
	            pB = oldPos + i;
	        } else {
	            int sobrante = (oldPos + i) - indexFinal;
	            pB = indexFinal - sobrante;
	            pA = (sobrante == 1) ? indexFinal : indexFinal - (sobrante - 1);
	        }

	        int[] cA = getGridCoords(pA);
	        int[] cB = getGridCoords(pB);

	        final double stepDx = (cB[1] - cA[1]) * cellWidth + ((targetTX - currentTX) / (double)steps);
	        final double stepDy = (cB[0] - cA[0]) * cellHeight + ((targetTY - currentTY) / (double)steps);
	        final double startX = accumTX;
	        final double startY = accumTY;

	        final ImageView sombraFwd = getSombraParaJugador(j);
	        // Girar el personaje según la dirección horizontal
	        if (stepDx > 0) {
				pieza.setScaleX(1.0);
			} else if (stepDx < 0) {
				pieza.setScaleX(-1.0);
			}
	        
	        // -------------------------------------------------------------------------
	        // --- Detección de colisiones en ruta EXCLUSIVA para la Foca ---
	        // Mientras la foca hace la animación de moverse X espacios, cada vez que llegue "virtualmente"
	        // a una casilla de transición (pB != newPos), comprueba si ese espacio intercepta a un jugador.
	        // Si lo intercepta (Paso por encima) -> Aplasta al pingüino y le roba la mitad de los items.
	        if (j instanceof model.entitats.Foca fActual && pB != newPos) {
	        	for (Jugador evalRival : js) {
	        		if (evalRival instanceof Pinguino pingRival && evalRival.getPosicio() == pB) {
	        			// Si está en la misma casilla y NO es la casilla final (newPos), es un pase por encima.
	        			// La Foca detecta al pingüino y usa "aplastar" para fastidiarle su equipo instantáneamente.
	        			fActual.aplastarPingu(pingRival);
	        		}
	        	}
	        }
	        // -------------------------------------------------------------------------

	        Transition jump = new Transition() {
	            { setCycleDuration(Duration.millis(450)); }
	            @Override protected void interpolate(double frac) {
	                double curX = startX + stepDx * frac;
	                double curY = startY + stepDy * frac;
	                double hopY = -45 * Math.sin(Math.PI * frac);
	                pieza.setTranslateX(curX);
	                pieza.setTranslateY(curY + hopY);
	                // La sombra se desliza por el suelo (sin salto)
	                if (sombraFwd != null) {
	                    sombraFwd.setTranslateX(curX);
	                    sombraFwd.setTranslateY(curY);
	                }
	            }
	        };
	        sequence.getChildren().add(jump);
	        accumTX += stepDx;
	        accumTY += stepDy;
	    }

	    final double finalTX = targetTX;
	    final double finalTY = targetTY;

	    sequence.setOnFinished(e -> Platform.runLater(() -> {
	        pieza.setTranslateX(finalTX);
	        pieza.setTranslateY(finalTY);
	        
	        Platform.runLater(() -> {
	            j.setPosicio(newPos);
	            actualizarUI();
	            bloquearControles(true);
	            
	            final Runnable finishTurnCallback = () -> finalizarTurno(j);
	            boolean interactionOccurred = false;
	            boolean collisionHandled = false;

	            if (j instanceof Pinguino pActual) {
	                ArrayList<Jugador> jugadors = gestorPartida.getPartida().getJugadors();
	                for (int i = 0; i < jugadors.size() && !collisionHandled; i++) {
	                    Jugador rival = jugadors.get(i);
	                    if (rival != pActual && rival.getPosicio() == newPos) {
	                        if (rival instanceof Pinguino pRival) {
	                            if (!(pActual.getInventari().getBoles() == 0 && pRival.getInventari().getBoles() == 0)) {
	                                interactionOccurred = true;
	                                collisionHandled = true;
	                                registrarEvento("¡Colisión! Batalla entre " + pActual.getNickname() + " y " + pRival.getNickname(), "log-warning");
	                                
	                                int posJ1Abans = pActual.getPosicio();
	                                int posJ2Abans = pRival.getPosicio();
	                                
	                                mostrarOverlayBatalla(() -> {
	                                    pActual.gestionarBatalla(pRival);

	                                    String resultMsg;
	                                    if (pActual.getPosicio() < posJ1Abans) {
	                                        resultMsg = pRival.getNickname() + " ¡gana! " + pActual.getNickname() + " retrocede.";
	                                    } else if (pRival.getPosicio() < posJ2Abans) {
	                                        resultMsg = pActual.getNickname() + " ¡gana! " + pRival.getNickname() + " retrocede.";
	                                    } else {
	                                        resultMsg = "¡Empate! Ambos pierden todas las bolas de nieve.";
	                                    }
	                                    registrarEvento("Batalla: " + resultMsg, "log-warning");

	                                    // En auto-play se omite el diálogo de resultado; en modo normal se muestra
	                                    Runnable continueAfterBattle = () -> {
	                                        if (pActual.getPosicio() != posJ1Abans) {
	                                            animarRetroceso(pActual, posJ1Abans, pActual.getPosicio(), () -> {
	                                                if (pRival.getPosicio() != posJ2Abans) {
	                                                    animarRetroceso(pRival, posJ2Abans, pRival.getPosicio(), finishTurnCallback);
	                                                } else {
	                                                    finishTurnCallback.run();
	                                                }
	                                            });
	                                        } else if (pRival.getPosicio() != posJ2Abans) {
	                                            animarRetroceso(pRival, posJ2Abans, pRival.getPosicio(), finishTurnCallback);
	                                        } else {
	                                            finishTurnCallback.run();
	                                        }
	                                    };

	                                    if (autoPlayActivo) {
	                                        // Auto-play: primera opción automáticamente sin diálogo
	                                        continueAfterBattle.run();
	                                    } else {
	                                        Alert batallaAlert = new Alert(AlertType.INFORMATION);
	                                        estilar(batallaAlert);
	                                        batallaAlert.setTitle("Resultado de la Batalla");
	                                        batallaAlert.setHeaderText("¡Combate de bolas de nieve!");
	                                        batallaAlert.setContentText(resultMsg);
	                                        batallaAlert.showAndWait();
	                                        continueAfterBattle.run();
	                                    }
	                                });
	                            }
	                        } else if (rival instanceof model.entitats.Foca fRival) {
	                            interactionOccurred = true;
	                            collisionHandled = true;
	                            registrarEvento(pActual.getNickname() + " ha chocado con la foca " + fRival.getNickname(), "log-warning");
	                            int posPinguAbans = pActual.getPosicio();
	                            
	                            // En auto-play no se usan ítems: saltamos el soborno
	                            if (!autoPlayActivo) {
	                                fRival.sobornarFoca(pActual);
	                            } else {
	                                registrarEvento("(Auto-Play) Se omite el soborno a la foca.", "log-info");
	                            }
	                            
                                // Aplicamos directamente el ataque de "pegar" de la Foca si no fue sobornada previamente.
                                // Esto enviará al Pingüino al agujero anterior.
	                            fRival.pegarPingu(pActual, gestorPartida.getPartida());
                                
                                // Si "pegarPingu" alteró la posición del jugador atacado, animamos visualmente su retroceso.
                                if (pActual.getPosicio() != posPinguAbans) {
                                    animarRetroceso(pActual, posPinguAbans, pActual.getPosicio(), finishTurnCallback);
                                } else {
                                    finishTurnCallback.run();
                                }
	                        }
	                    }
	                }
	            } else if (j instanceof model.entitats.Foca fActual) {
	                ArrayList<Jugador> jugadorsF = gestorPartida.getPartida().getJugadors();
	                for (int i = 0; i < jugadorsF.size() && !collisionHandled; i++) {
	                    Jugador rival = jugadorsF.get(i);
	                    if (rival instanceof Pinguino pRival && rival.getPosicio() == newPos) {
	                        interactionOccurred = true;
	                        collisionHandled = true;
	                        registrarEvento("La foca " + fActual.getNickname() + " ha caído sobre " + pRival.getNickname(), "log-warning");
	                        int posPAbans = pRival.getPosicio();
	                        
	                        // Opción de alimentar a la foca para evitar el ataque (en auto-play se omite el soborno)
	                        if (!autoPlayActivo) {
	                            fActual.sobornarFoca(pRival);
	                        } else {
	                            registrarEvento("(Auto-Play) Se omite el soborno a la foca.", "log-info");
	                        }
	                        
	                        // Aplicamos el ataque en el que la foca le pega al Pingüino con la cola
	                        fActual.pegarPingu(pRival, gestorPartida.getPartida());
	                        
	                        // Si el ataque ha movido al Pingüino (lo ha mandado al agujero), ejecutamos la animación de retroceso.
                            if (pRival.getPosicio() != posPAbans) {
                                animarRetroceso(pRival, posPAbans, pRival.getPosicio(), finishTurnCallback);
                            } else {
                                finishTurnCallback.run();
                            }
	                    }
	                }
	            }
	            
	            if (interactionOccurred) return;

	            GestorTaulell gt = new GestorTaulell();
	            Casella c = gestorPartida.getPartida().getTaulell().getCaselles().get(j.getPosicio());
	            if (c instanceof model.caselles.Event && j instanceof Pinguino) {
	                ((model.caselles.Event)c).setCallbackFinalizacion(finishTurnCallback);
	                gt.executarCasella(gestorPartida.getPartida(), j, c);
	                return;
	            } else {
	                gt.executarCasella(gestorPartida.getPartida(), j, c);
	                if (j.getPosicio() != newPos && c instanceof model.caselles.Forat) {
	                    animarEfectoForat(j, newPos, j.getPosicio(), finishTurnCallback);
	                    return;
	                }
	            }
	            finishTurnCallback.run();
	        });
	    }));
	    sequence.play();
	}

	private void finalizarTurno(Jugador j) {
		// Al finalizar el turno, si es una foca, decrementamos sus sobornos
		if (j instanceof model.entitats.Foca f) {
			f.actualizarSobornos();
		}
		
		GestorTaulell gt = new GestorTaulell();
		gt.comprovarFiTorn(gestorPartida.getPartida());
		
		if (gestorPartida.getPartida().isFinalitzada()) {
			actualizarUI();
			Jugador guanyador = gestorPartida.getPartida().getGuanyador();

			mostrarAlertaGanador(guanyador);
			return;
		}

		gestorPartida.seguentTorn();
		actualizarUI();
		checkTurnoCPU();
	}

	/**
	 * Helper para obtener coordenadas (fila, columna) de una posición lógica del tablero.
	 */
	private int[] getGridCoords(int pos) {
		int logicalRow = pos / COLUMNS;
		int logicalCol = pos % COLUMNS;
		if (logicalRow % 2 != 0) {
			logicalCol = (COLUMNS - 1) - logicalCol;
		}
		int row = (ROWS - 1) - logicalRow;
		int col = logicalCol;
		return new int[] { row, col };
	}

	/**
	 * Muestra un overlay de batalla en el centro de la pantalla y ejecuta una acción al terminar.
	 */
	private void mostrarOverlayBatalla(Runnable onComplete) {
		try {
			Image img = new Image(getClass().getResourceAsStream("/assets/GestionarBatallaTEXTO.png"));
			ImageView overlay = new ImageView(img);
			overlay.setPreserveRatio(true);
			overlay.setFitWidth(800);
			overlay.setMouseTransparent(true);

			Platform.runLater(() -> {
				StackPane wrapper = new StackPane(overlay);
				wrapper.setMouseTransparent(true);
				wrapper.setPrefSize(1920, 1080); // Resolucion base
				
				boardContainer.getChildren().add(wrapper);
				
				javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(1500), wrapper);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.setAutoReverse(true);
				ft.setCycleCount(2);
				ft.setOnFinished(e -> {
					boardContainer.getChildren().remove(wrapper);
					if (onComplete != null) {
						javafx.application.Platform.runLater(onComplete);
					}
				});
				ft.play();
			});
		} catch (Exception e) {
			System.err.println("Error cargando GestionarBatallaTEXTO: " + e.getMessage());
			if (onComplete != null) {
				onComplete.run();
			}
		}
	}

	public void animarRetroceso(Jugador j, int oldPos, int newPos, Runnable onComplete) {
	    ImageView pieza = getPiezaParaJugador(j);
	    if (pieza == null) {
	        if (onComplete != null) {
				onComplete.run();
			}
	        return;
	    }
	    if (oldPos <= newPos) {
	        actualizarUI();
	        if (onComplete != null) {
				onComplete.run();
			}
	        return;
	    }

	    SequentialTransition sequence = new SequentialTransition();
	    double cellWidth = tablero.getPrefWidth() * 0.10;
	    double cellHeight = tablero.getPrefHeight() * 0.20;

	    double currentTX = pieza.getTranslateX();
	    double currentTY = pieza.getTranslateY();

	    double accumTX = currentTX;
	    double accumTY = currentTY;

	    int steps = oldPos - newPos;

	    for (int i = 1; i <= steps; i++) {
	        int pA = oldPos - i + 1;
	        int pB = oldPos - i;

	        int[] cA = getGridCoords(pA);
	        int[] cB = getGridCoords(pB);

	        final double stepDx = (cB[1] - cA[1]) * cellWidth;
	        final double stepDy = (cB[0] - cA[0]) * cellHeight;
	        final double startX = accumTX;
	        final double startY = accumTY;

	        final ImageView sombraBack = getSombraParaJugador(j);
	        // Girar el personaje según la dirección horizontal del retroceso
	        if (stepDx > 0) {
				pieza.setScaleX(1.0);
			} else if (stepDx < 0) {
				pieza.setScaleX(-1.0);
			}
	        Transition jump = new Transition() {
	            { setCycleDuration(Duration.millis(450)); }
	            @Override protected void interpolate(double frac) {
	                double curX = startX + stepDx * frac;
	                double curY = startY + stepDy * frac;
	                double hopY = -45 * Math.sin(Math.PI * frac);
	                pieza.setTranslateX(curX);
	                pieza.setTranslateY(curY + hopY);
	                // La sombra se desliza por el suelo (sin salto)
	                if (sombraBack != null) {
	                    sombraBack.setTranslateX(curX);
	                    sombraBack.setTranslateY(curY);
	                }
	            }
	        };

	        sequence.getChildren().add(jump);
	        accumTX += stepDx;
	        accumTY += stepDy;
	    }

	    sequence.setOnFinished(e -> Platform.runLater(() -> {
	        pieza.setTranslateX(0);
	        pieza.setTranslateY(0);
	        actualizarUI();
	        if (onComplete != null) {
				onComplete.run();
			}
	    }));
	    sequence.play();
	}

	private void animarRetroceso(Jugador j, int oldPos, int newPos, boolean processCell) {
	    animarRetroceso(j, oldPos, newPos, () -> {
	        if (processCell) {
	            procesarEfectoCasella(j);
	        }
	    });
	}

	private void procesarEfectoCasella(Jugador j) {
	    int posActual = j.getPosicio();
	    Casella c = gestorPartida.getPartida().getTaulell().getCaselles().get(posActual);
	    
	    if (!(c instanceof model.caselles.Event)) {
	        new GestorTaulell().executarCasella(gestorPartida.getPartida(), j, c);
	        
	        if (j.getPosicio() != posActual) {
	            int nuevaPos = j.getPosicio();
	            if (nuevaPos < posActual) {
	                animarRetroceso(j, posActual, nuevaPos, false);
	            } else {
	                actualizarUI();
	            }
	        }
	    }
	}

	private void animarEfectoForat(Jugador j, int posEntrada, int posSalida, Runnable onFinished) {
		ImageView pieza = getPiezaParaJugador(j);
		if (pieza == null) {
			if (onFinished != null) onFinished.run();
			return;
		}

		RotateTransition rtIn = new RotateTransition(Duration.millis(800), pieza);
		rtIn.setByAngle(360 * 3); 
		
		ScaleTransition stIn = new ScaleTransition(Duration.millis(800), pieza);
		stIn.setToX(0);
		stIn.setToY(0);
		
		ParallelTransition ptIn = new ParallelTransition(pieza, rtIn, stIn);
		
		ptIn.setOnFinished(e -> {
			actualizarUI();
			bloquearControles(true);
			
			pieza.setScaleX(0);
			pieza.setScaleY(0);
			
			RotateTransition rtOut = new RotateTransition(Duration.millis(800), pieza);
			rtOut.setByAngle(-360 * 3);
			
			ScaleTransition stOut = new ScaleTransition(Duration.millis(800), pieza);
			stOut.setToX(1.0);
			stOut.setToY(1.0);

			ParallelTransition ptOut = new ParallelTransition(pieza, rtOut, stOut);
			ptOut.setOnFinished(e2 -> {
				if (onFinished != null) onFinished.run();
			});
			ptOut.play();
		});
		
		ptIn.play();
	}

	public static void animarRetrocesoEstatico(Jugador j, int oldPos, int newPos) {
		animarRetrocesoEstatico(j, oldPos, newPos, null);
	}

	public static void animarRetrocesoEstatico(Jugador j, int oldPos, int newPos, Runnable onComplete) {
		if (instanciaActual != null) {
			Platform.runLater(() -> instanciaActual.animarRetroceso(j, oldPos, newPos, true));
		}
	}

	private void mostrarAlertaGanador(Jugador g) {
	    Platform.runLater(() -> {
	        Alert alert = new Alert(AlertType.INFORMATION);
	        estilar(alert);
	        alert.setTitle("¡Fin de la partida!");
	        alert.setHeaderText("¡Tenemos un ganador!");
	        alert.setContentText("Enhorabuena " + g.getNickname() + ", ¡has llegado a la meta!");

	        ButtonType btnGuardar = new ButtonType("Guardar y Salir");
	        ButtonType btnCopiar = new ButtonType("Copiar semilla");
	        ButtonType btnSalir = new ButtonType("Salir sin Guardar");
	        alert.getButtonTypes().setAll(btnGuardar, btnCopiar, btnSalir);

	        alert.showAndWait().ifPresent(result -> {
	            if (result == btnGuardar) {
	                handleSaveGame();
	                goToMenu();
	            } else if (result == btnCopiar) {
	                mostrarVentanaSeed();
	                mostrarAlertaGanador(g); 
	            } else if (result == btnSalir) {
	                goToMenu();
	            }
	        });
	    });
	}

	private void checkTurnoCPU() {
		if (gestorPartida.getPartida().isFinalitzada()) return;

		Jugador proximo = gestorPartida.getPartida().getJugadorActual();
		if (proximo instanceof model.entitats.Foca) {
		    bloquearControles(true);
			new Thread(() -> {
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
				javafx.application.Platform.runLater(this::executartorn);
			}).start();
		} else if (proximo instanceof Pinguino && autoPlayActivo) {
			// Auto-play: el pingüino humano juega automáticamente sin usar ítems
			bloquearControles(true);
			new Thread(() -> {
				try { Thread.sleep(1200); } catch (InterruptedException e) {}
				javafx.application.Platform.runLater(this::executartorn);
			}).start();
		}
	}

	@FXML
	private void handleRapido() {
		Jugador actual = gestorPartida.getPartida().getJugadorActual();
		if (!(actual instanceof Pinguino pingu)) return;

		Dau dRapid = null;
		java.util.List<model.items.Item> items = pingu.getInventari().getLlista();
		for (int i = 0; i < items.size() && dRapid == null; i++) {
			model.items.Item obj = items.get(i);
			if (obj instanceof Dau d && d.getMax() > 6 && d.getQuantitat() > 0) {
				dRapid = d;
			}
		}

		if (dRapid == null) {
			registrarEvento("No tienes dado rápido.", "log-warning");
			return;
		}

		dauSeleccionat = dRapid;
		registrarEvento(pingu.getNickname() + " usa dado rápido (" + dRapid.getMin() + "-" + dRapid.getMax() + ")", "log-info");
		executartorn();
	}

	@FXML
	private void handleLento() {
		Jugador actual = gestorPartida.getPartida().getJugadorActual();
		if (!(actual instanceof Pinguino pingu)) return;

		Dau dLent = null;
		java.util.List<model.items.Item> itemsL = pingu.getInventari().getLlista();
		for (int i = 0; i < itemsL.size() && dLent == null; i++) {
			model.items.Item obj = itemsL.get(i);
			if (obj instanceof Dau d && d.getMax() <= 3 && d.getQuantitat() > 0) {
				dLent = d;
			}
		}

		if (dLent == null) {
			registrarEvento("No tienes dado lento.", "log-warning");
			return;
		}

		dauSeleccionat = dLent;
		String range = (dLent.getMin() == dLent.getMax()) ? String.valueOf(dLent.getMin()) : dLent.getMin() + "-" + dLent.getMax();
		if (dLent.getNom().equals("Dau lent")) range = "1 o 3";
		
		registrarEvento(pingu.getNickname() + " usa dado lento (" + range + ")", "log-info");
		executartorn();
	}

	@FXML
	private void handlePeces() {
		Jugador actual = gestorPartida.getPartida().getJugadorActual();
		if (!(actual instanceof Pinguino pingu)) return;

		boolean usat = pingu.getInventari().usarPrimer(Peix.class);
		if (!usat) {
			registrarEvento("No tienes peces.", "log-warning");
			return;
		}

		registrarEvento(pingu.getNickname() + " usa un pez: avanza 2 casillas extra.", "log-info");
		int maxPos = gestorPartida.getPartida().getTaulell().getCaselles().size() - 1;
		pingu.setPosicio(Math.min(pingu.getPosicio() + 2, maxPos));
		actualizarUI();
		
		if (pingu.getPosicio() == maxPos) {
			controlador.GestorTaulell gt = new controlador.GestorTaulell();
			gt.comprovarFiTorn(gestorPartida.getPartida());
			if (gestorPartida.getPartida().isFinalitzada()) {
				mostrarAlertaGanador(gestorPartida.getPartida().getGuanyador());
			}
		}
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

		ArrayList<Jugador> js = gestorPartida.getPartida().getJugadors();
		int idxSeguent = (js.indexOf(pingu) + 1) % js.size();
		Jugador seguent = js.get(idxSeguent);
		
		int posAnterior = seguent.getPosicio();
		int nuevaPos = Math.max(0, posAnterior - 1);
		
		seguent.setPosicio(nuevaPos);
		animarRetroceso(seguent, posAnterior, nuevaPos, true);
	}


	private void aplicarOffsetDeSeparacion(ImageView pieza, int index, int total) {
		if (total <= 1) {
			pieza.setTranslateX(0);
			pieza.setTranslateY(0);
			return;
		}
		
		double offset = 15.0; 
		switch (index) {
			case 0: 
				pieza.setTranslateX(-offset);
				pieza.setTranslateY(-offset);
				break;
			case 1: 
				pieza.setTranslateX(offset);
				pieza.setTranslateY(-offset);
				break;
			case 2: 
				pieza.setTranslateX(-offset);
				pieza.setTranslateY(offset);
				break;
			case 3: 
				pieza.setTranslateX(offset);
				pieza.setTranslateY(offset);
				break;
			default:
				pieza.setTranslateX(0);
				pieza.setTranslateY(0);
				break;
		}
	}

	public void estilar(javafx.scene.control.Dialog<?> d) {
		try {
			if (boardContainer != null && boardContainer.getScene() != null) {
				d.initOwner(boardContainer.getScene().getWindow());
			}

			javafx.scene.control.DialogPane pane = d.getDialogPane();
			
			int numButtons = d.getDialogPane().getButtonTypes().size();
			if (numButtons > 2) {
				pane.setMinWidth(1000); 
			} else {
				pane.setMinWidth(500); 
			}

			pane.getButtonTypes().forEach(bt -> {
				javafx.scene.Node node = pane.lookupButton(bt);
				if (node instanceof javafx.scene.control.Button) {
					javafx.scene.control.Button btn = (javafx.scene.control.Button) node;
					btn.setMaxWidth(Double.MAX_VALUE);
					javafx.scene.control.ButtonBar.setButtonUniformSize(btn, false);
				}
			});
			String css = getClass().getResource("/resources/PantallaMenu.css").toExternalForm();
			pane.getStylesheets().add(css);
		} catch (Exception e) {
			System.err.println("No se pudo aplicar el CSS al diálogo: " + e.getMessage());
		}
	}
}
package vista;

import controlador.AudioManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.util.Optional;

import java.sql.Connection;
import java.util.ArrayList;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.entitats.Foca;
import controlador.GestorBBDD;
import controlador.GestorTaulell;

public class PantallaMenu {

	/**
	 * Interfaz funcional pròpia per substituir java.util.concurrent.Callable sense
	 * dependències il·legals.
	 */
	private interface TascaDB {
		void executar() throws Exception;
	}

	@FXML
	private MenuItem saveGame;
	@FXML
	private MenuItem loadGame;
	@FXML
	private MenuItem menuItem;
	@FXML
	private MenuItem quitGame;

	@FXML
	private TextField userField;
	@FXML
	private PasswordField passField;

	@FXML
	private ListView<String> playersList;
	@FXML
	private ListView<String> savedGamesList;
	@FXML
	private ListView<String> rankingList;
	@FXML
	private TabPane mainTabPane;
	@FXML
	private Label deleteFeedbackLabel;
	@FXML
	private Label seedStatusLabel;
	@FXML
	private VBox landingContainer;
	@FXML
	private VBox rulesContainer;
	@FXML
	private VBox contentContainer;
	@FXML
	private StackPane menuRoot;
	@FXML
	private AnchorPane menuContainer;
	@FXML
	private Button btnMute;
	@FXML
	private ImageView imgMute;
	@FXML
	private Button btnMuteSfx;
	@FXML
	private ImageView imgMuteSfx;
	@FXML
	private Button btnSettings;
	@FXML
	private Button btnLoginHumano;
	@FXML
	private Button btnAddCPU;
	@FXML
	private Button startGameButton;
	@FXML
	private VBox settingsPane;
	@FXML
	private Slider musicSlider;
	@FXML
	private Slider sfxSlider;
	@FXML
	private javafx.scene.control.CheckBox fullScreenCheck;

	// Landing buttons for animation
	@FXML
	private Button btnNewGame;
	@FXML
	private Button btnLoadGame;
	@FXML
	private Button btnRanking;
	@FXML
	private Button btnQuit;
	// btnSettings is already defined on line 71

	private ArrayList<Jugador> joinedPlayers = new ArrayList<>();
	private int cpuCount = 0;
	private GestorBBDD dbManager = new GestorBBDD();

	@FXML
	private Label dbStatusLabel;
	@FXML
	private javafx.scene.control.ProgressBar dbProgressBar;

	private String loadedSeed = "";

	@FXML
	private void initialize() {
		// Efecto de entrada (Fade In)
		menuRoot.setOpacity(0.0);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(500), menuRoot);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		fadeIn.play();

		System.out.println("PantallaMenu inicializada");

		// ── Escalado dinámico PERFECTO para Laptops sin romper la config base ──
		// Limpiamos el StackPane principal y forzamos que pueda encogerse a cualquier
		// tamaño
		menuRoot.getChildren().clear();
		menuRoot.setMinSize(0, 0);
		// Evitamos que los contenedores superiores fuercen tamaños mínimos (ej. el
		// BorderPane raíz)
		if (menuRoot.getParent() instanceof javafx.scene.layout.Region) {
			((javafx.scene.layout.Region) menuRoot.getParent()).setMinSize(0, 0);
		}

		// Creamos un wrapper que se ajustará a la ventana real, dándonos las
		// dimensiones correctas
		javafx.scene.layout.Pane wrapper = new javafx.scene.layout.Pane();
		menuRoot.getChildren().add(wrapper);

		// Añadimos nuestro diseño original de 1920x1080
		wrapper.getChildren().add(menuContainer);

		// Transformación directa sobre el contenedor original
		javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(1, 1, 0, 0);
		menuContainer.getTransforms().clear();
		menuContainer.getTransforms().add(scaleTransform);

		// Recalculamos la escala y la posición en cuanto la ventana cambie de
		// resolución
		javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
			double w = wrapper.getWidth();
			double h = wrapper.getHeight();
			if (w != 0 && h != 0) {
				// Factor de escala respetando aspecto 16:9
				double scaleFactor = Math.min(w / 1920.0, h / 1080.0);
				scaleTransform.setX(scaleFactor);
				scaleTransform.setY(scaleFactor);

				// Centrar manualmente para que los botones nunca se queden fuera ni sus zonas
				// de clic se desplacen
				double scaledWidth = 1920.0 * scaleFactor;
				double scaledHeight = 1080.0 * scaleFactor;
				menuContainer.setLayoutX((w - scaledWidth) / 2.0);
				menuContainer.setLayoutY((h - scaledHeight) / 2.0);
			}
		};

		wrapper.widthProperty().addListener(resizeListener);
		wrapper.heightProperty().addListener(resizeListener);

		// ── Efecto de nieve cayendo ──
		new EfectoNieve(wrapper);

		// Personalización de colores del ranking por posición (Se configura ANTES de
		// cargar datos)
		rankingList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
					getStyleClass().removeAll("rank-1", "rank-2", "rank-3", "rank-4-5", "rank-default");
				} else {
					setText(item);
					getStyleClass().removeAll("rank-1", "rank-2", "rank-3", "rank-4-5", "rank-default");
					int index = getIndex();
					if (index == 0) {
						getStyleClass().add("rank-1");
					} else if (index == 1) {
						getStyleClass().add("rank-2");
					} else if (index == 2) {
						getStyleClass().add("rank-3");
					} else if (index == 3 || index == 4) {
						getStyleClass().add("rank-4-5");
					} else {
						getStyleClass().add("rank-default");
					}
				}
			}
		});

		// Celda personalizada para playersList para incluir botón de eliminar
		playersList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
			private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(10);
			private final javafx.scene.control.Label nameLabel = new javafx.scene.control.Label();
			private final javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
			private final javafx.scene.control.Button removeBtn = new javafx.scene.control.Button("-");

			{
				container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
				removeBtn.getStyleClass().add("delete-button-small");
				removeBtn.setOnAction(e -> {
					int index = getIndex();
					if (index >= 0 && index < joinedPlayers.size()) {
						joinedPlayers.remove(index);
						playersList.getItems().remove(index);
					}
				});
				nameLabel.setTextFill(javafx.scene.paint.Color.WHITESMOKE);
				container.getChildren().addAll(nameLabel, spacer, removeBtn);
			}

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					nameLabel.setText(item);
					setGraphic(container);
				}
			}
		});

		showLanding();

		// Cargar datos asíncronamente al iniciar
		ejecutarTareaDB("Cargando datos...", () -> {
			try (Connection con = GestorBBDD.conectarBaseDatos()) {
				if (con != null) {
					ArrayList<String> games = dbManager.llistarPartides(con);
					ArrayList<String> ranking = dbManager.obtenerRanking(con);
					Platform.runLater(() -> {
						savedGamesList.getItems().setAll(games);
						rankingList.getItems().setAll(ranking);
					});
				}
			}
		});

		updateMuteUI();

		// Registrar sonidos para todos los botones del menú
		registrarSonsBotons(menuContainer);

		// Inicializar sliders de volumen
		if (musicSlider != null) {
			musicSlider.setValue(AudioManager.getInstance().getMusicVolume());
			musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
				AudioManager.getInstance().setMusicVolume(newVal.doubleValue());
			});
		}
		if (sfxSlider != null) {
			sfxSlider.setValue(AudioManager.getInstance().getSfxVolume());
			sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
				AudioManager.getInstance().setSfxVolume(newVal.doubleValue());
			});
		}

		// Inicializar checkbox de pantalla completa
		if (fullScreenCheck != null) {
			fullScreenCheck.setSelected(controlador.Main.isFullScreenEnabled());
			fullScreenCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
				controlador.Main.setFullScreenEnabled(newVal);
			});
		}

		updateMuteUI();

		// ── Preparar estado inicial para las animaciones (evitar parpadeo) ──
		if (landingContainer != null) {
			landingContainer.setOpacity(0);
			landingContainer.setTranslateY(-400);
		}
		if (rulesContainer != null) {
			rulesContainer.setOpacity(0);
			rulesContainer.setTranslateX(-800);
		}
		Button[] buttons = { btnNewGame, btnLoadGame, btnRanking, btnSettings, btnQuit };
		for (Button b : buttons) {
			if (b != null) {
				b.setOpacity(0);
				b.setTranslateX(800);
			}
		}

		// ── Animaciones de entrada escalonadas para los botones ──
		// Usamos un listener para detectar cuando el menú se muestra realmente en
		// pantalla
		// (ya que se pre-carga en segundo plano mientras está la pantalla de carga)
		menuRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
			if (newScene != null) {
				Platform.runLater(() -> {
					// Animación para el título (desde arriba)
					animarPanelArriba(landingContainer, 400);

					// Animación para el panel de reglas (desde la izquierda)
					animarPanelIzquierda(rulesContainer, 600);

					// Animación para los botones (desde la derecha)
					animarBotonEntrada(btnNewGame, 700);
					animarBotonEntrada(btnLoadGame, 850);
					animarBotonEntrada(btnRanking, 1000);
					animarBotonEntrada(btnSettings, 1150);
					animarBotonEntrada(btnQuit, 1300);
				});
			}
		});
	}

	/**
	 * Aplica una animación de entrada (desplazamiento + fade) a un botón con un
	 * retraso.
	 */
	private void animarBotonEntrada(Node node, int delayMillis) {
		if (node != null) {
			// El estado inicial ya se ha configurado en initialize() para evitar parpadeos

			Timeline timeline = new Timeline();
			timeline.setDelay(Duration.millis(delayMillis));

			// KeyFrame 1: Movimiento rápido hacia la izquierda con un ligero overshoot
			// (rebote)
			KeyFrame kf1 = new KeyFrame(Duration.millis(600),
					new KeyValue(node.translateXProperty(), -30, Interpolator.EASE_OUT),
					new KeyValue(node.opacityProperty(), 1.0, Interpolator.EASE_OUT));

			// KeyFrame 2: Volver a la posición final (0) suavemente
			KeyFrame kf2 = new KeyFrame(Duration.millis(850),
					new KeyValue(node.translateXProperty(), 0, Interpolator.EASE_IN));

			timeline.getKeyFrames().addAll(kf1, kf2);
			timeline.play();
		}
	}

	/**
	 * Aplica una animación de entrada desde la izquierda con rebote.
	 */
	private void animarPanelIzquierda(Node node, int delayMillis) {
		if (node != null) {
			// El estado inicial ya se ha configurado en initialize() para evitar parpadeos

			Timeline timeline = new Timeline();
			timeline.setDelay(Duration.millis(delayMillis));

			KeyFrame kf1 = new KeyFrame(Duration.millis(700),
					new KeyValue(node.translateXProperty(), 30, Interpolator.EASE_OUT),
					new KeyValue(node.opacityProperty(), 1.0, Interpolator.EASE_OUT));

			KeyFrame kf2 = new KeyFrame(Duration.millis(950),
					new KeyValue(node.translateXProperty(), 0, Interpolator.EASE_IN));

			timeline.getKeyFrames().addAll(kf1, kf2);
			timeline.play();
		}
	}

	/**
	 * Aplica una animación de entrada desde arriba con rebote.
	 */
	private void animarPanelArriba(Node node, int delayMillis) {
		if (node != null) {
			// El estado inicial ya se ha configurado en initialize() para evitar parpadeos

			Timeline timeline = new Timeline();
			timeline.setDelay(Duration.millis(delayMillis));

			KeyFrame kf1 = new KeyFrame(Duration.millis(700),
					new KeyValue(node.translateYProperty(), 30, Interpolator.EASE_OUT),
					new KeyValue(node.opacityProperty(), 1.0, Interpolator.EASE_OUT));

			KeyFrame kf2 = new KeyFrame(Duration.millis(950),
					new KeyValue(node.translateYProperty(), 0, Interpolator.EASE_IN));

			timeline.getKeyFrames().addAll(kf1, kf2);
			timeline.play();
		}
	}

	/**
	 * Registra recursivamente los sonidos de hover y click para todos los botones
	 * dentro de un nodo padre.
	 */
	private void registrarSonsBotons(Node node) {
		if (node instanceof Button) {
			Button btn = (Button) node;

			// Sonido al pasar el ratón (hover)
			btn.setOnMouseEntered(e -> {
				AudioManager.getInstance().playSound("/assets/Hover_boton_hielo.mp3");
			});

			// Sonido al hacer clic (ACTION para que conviva con FXML onAction)
			btn.addEventHandler(ActionEvent.ACTION, e -> {
				AudioManager.getInstance().playSound("/assets/Audio_click_hielo.mp3");
			});

		} else if (node instanceof javafx.scene.Parent) {
			// Recorrer hijos si es un contenedor
			for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
				registrarSonsBotons(child);
			}
		}
	}

	private void ejecutarTareaDB(String mensaje, TascaDB tarea) {
		dbStatusLabel.setText(mensaje);
		dbStatusLabel.setVisible(true);
		dbProgressBar.setVisible(true);
		dbProgressBar.setProgress(-1); // Indeterminado

		try {
			tarea.executar();
		} catch (Exception e) {
			e.printStackTrace();
			dbStatusLabel.setText("Error en la DB");
		} finally {
			dbProgressBar.setVisible(false);
			dbStatusLabel.setVisible(false);
		}
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
	private void handleToggleSettings(ActionEvent event) {
		if (settingsPane != null) {
			boolean isVisible = settingsPane.isVisible();
			settingsPane.setVisible(!isVisible);
			settingsPane.setManaged(!isVisible);
		}
	}

	private void updateMuteUI() {
		// Actualizar música
		if (imgMute != null) {
			boolean musicMuted = AudioManager.getInstance().isMusicMuted();
			String musicIcon = musicMuted ? "/assets/speaker_off.png" : "/assets/speaker_on.png";
			try {
				imgMute.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(musicIcon)));
			} catch (Exception e) {
				System.err.println("Error actualizando icono música: " + e.getMessage());
			}
		}

		// Actualizar SFX
		if (imgMuteSfx != null) {
			boolean sfxMuted = AudioManager.getInstance().isSfxMuted();
			String sfxIcon = sfxMuted ? "/assets/speaker_off.png" : "/assets/speaker_on.png";
			try {
				imgMuteSfx.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(sfxIcon)));
				// Opcional: podrías cambiar el color o añadir un pequeño texto/overlay
				// para diferenciar música de efectos si usas el mismo icono
			} catch (Exception e) {
				System.err.println("Error actualizando icono SFX: " + e.getMessage());
			}
		}
	}

	@FXML
	private void handleLogin(ActionEvent event) {
		if (joinedPlayers.size() >= 6) {
			Alert alert = new Alert(AlertType.WARNING, "Máximo 6 jugadores permitidos.", ButtonType.OK);
			estilar(alert);
			alert.showAndWait();
		} else {
			String username = userField.getText().trim();
			String password = passField.getText().trim();

			if (username.isEmpty() || password.isEmpty()) {
				Alert alert = new Alert(AlertType.WARNING, "Debes introducir un usuario y una contraseña.",
						ButtonType.OK);
				estilar(alert);
				alert.showAndWait();
			} else {
				// Validar si el usuario ya está en la lista de jugadores unidos
				boolean yaExiste = false;
				for (Jugador j : joinedPlayers) {
					if (j.getNickname().equalsIgnoreCase(username)) {
						Alert alert = new Alert(AlertType.WARNING,
								"El usuario '" + username + "' ya se ha unido a la partida.", ButtonType.OK);
						estilar(alert);
						alert.showAndWait();
						yaExiste = true;
					}
				}

				if (!yaExiste) {
					// --- VALIDACIÓN DE NOMBRE RESERVADO (FOCA) ---
					if (username.toLowerCase().contains("foca")) {
						Alert alert = new Alert(AlertType.WARNING,
								"El nombre '" + username + "' no está permitido (palabra reservada).", ButtonType.OK);
						estilar(alert);
						alert.showAndWait();
					} else {
						// --- PREVENCIÓN DE DOBLE CLIC Y LIMPIEZA RÁPIDA ---
						btnLoginHumano.setDisable(true);
						userField.clear();
						passField.clear();

						ejecutarTareaDB("Validando usuario...", () -> {
							try (Connection con = GestorBBDD.conectarBaseDatos()) {
								if (con != null) {
									boolean valid = dbManager.validarLogin(username, password, con);
									Platform.runLater(() -> {
										btnLoginHumano.setDisable(false); // Re-habilitar al terminar
										if (valid) {
											Pinguino p = new Pinguino(username, "Azul", new model.items.Inventari());
											p.setContrasenya(password);
											joinedPlayers.add(p);
											playersList.getItems().add(username + " (Humano)");
											System.out.println("Jugador añadido: " + username);
										} else {
											Alert alert = new Alert(AlertType.ERROR,
													"Contraseña incorrecta para el usuario: " + username,
													ButtonType.OK);
											estilar(alert);
											alert.showAndWait();
										}
									});
								} else {
									Platform.runLater(() -> btnLoginHumano.setDisable(false));
								}
							} catch (Exception e) {
								Platform.runLater(() -> btnLoginHumano.setDisable(false));
								throw e;
							}

						});
					}
				}
			}
		}
	}

	@FXML
	private void handleAddCPU(ActionEvent event) {
		if (joinedPlayers.size() >= 6) {
			Alert alert = new Alert(AlertType.WARNING, "Máximo 6 jugadores permitidos.", ButtonType.OK);
			estilar(alert);
			alert.showAndWait();
		} else {
			// Deshabilitar temporalmente para evitar spam
			btnAddCPU.setDisable(true);
			PauseTransition cooldown = new PauseTransition(Duration.millis(500));
			cooldown.setOnFinished(e -> btnAddCPU.setDisable(false));
			cooldown.play();

			cpuCount++;
			String cpuName = "Foca " + cpuCount;
			Foca cpu = new Foca(cpuName, "tempColor");
			joinedPlayers.add(cpu);
			playersList.getItems().add(cpuName + " (CPU)");
			System.out.println("CPU añadida: " + cpuName);
		}
	}

	@FXML
	private void handleClearPlayers(ActionEvent event) {
		joinedPlayers.clear();
		playersList.getItems().clear();
		cpuCount = 0;
		System.out.println("Lista de jugadores limpiada.");
	}

	@FXML
	private void handleRefreshGames() {
		ejecutarTareaDB("Cargando partidas...", () -> {
			try (Connection con = GestorBBDD.conectarBaseDatos()) {
				if (con != null) {
					ArrayList<String> games = dbManager.llistarPartides(con);
					Platform.runLater(() -> savedGamesList.getItems().setAll(games));
				}
			}

		});
	}

	@FXML
	private void handleRefreshRanking() {
		ejecutarTareaDB("Cargando ranking...", () -> {
			try (Connection con = GestorBBDD.conectarBaseDatos()) {
				if (con != null) {
					ArrayList<String> ranking = dbManager.obtenerRanking(con);
					Platform.runLater(() -> rankingList.getItems().setAll(ranking));
				}
			}

		});
	}

	@FXML
	private void handleLoadSeed(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog(loadedSeed);
		estilar(dialog);
		dialog.setTitle("Cargar Semilla");
		dialog.setHeaderText("Introduce una semilla de tablero (50 dígitos)");
		dialog.setContentText("Semilla:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(seed -> {
			GestorTaulell gt = new GestorTaulell();
			if (seed.isEmpty()) {
				loadedSeed = "";
				seedStatusLabel.setText("Semilla: Aleatoria");
			} else if (gt.esSeedValid(seed)) {
				loadedSeed = seed;
				seedStatusLabel.setText("Semilla: " + seed.substring(0, 5) + "...");
			} else {
				Alert alert = new Alert(AlertType.ERROR,
						"La semilla no es válida.\nDebe tener 50 dígitos (0-5) y cumplir las reglas de diseño.",
						ButtonType.OK);
				estilar(alert);
				alert.showAndWait();
			}
		});
	}

	@FXML
	private void handleStartGame(ActionEvent event) {
		if (startGameButton != null)
			startGameButton.setDisable(true);
		Partida partida = null;
		boolean continuar = true;

		int selectedTabIndex = mainTabPane.getSelectionModel().getSelectedIndex();

		if (selectedTabIndex == 1) { // Tab "Cargar Partida"
			String selected = savedGamesList.getSelectionModel().getSelectedItem();
			if (selected == null) {
				Alert alert = new Alert(AlertType.WARNING, "Por favor, selecciona una partida para cargar.",
						ButtonType.OK);
				estilar(alert);
				alert.showAndWait();
				if (startGameButton != null)
					startGameButton.setDisable(false);
				continuar = false;
			} else {
				int id = Integer.parseInt(selected.split(":")[1].trim().split(" ")[0]);

				try (Connection con = GestorBBDD.conectarBaseDatos()) {
					partida = dbManager.carregarBBDD(id, con);
				} catch (Exception e) {
					e.printStackTrace();
					if (startGameButton != null)
						startGameButton.setDisable(false);
					continuar = false;
				}

				// Verificar contraseña de cada jugador humano de la partida
				if (continuar && partida != null) {
					try (Connection con = GestorBBDD.conectarBaseDatos()) {
						for (int i = 0; i < partida.getJugadors().size() && continuar; i++) {
							Jugador j = partida.getJugadors().get(i);
							if (j instanceof Pinguino) {
								// Diálogo con PasswordField para ocultar la contraseña
								javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
								estilar(dialog);
								dialog.setTitle("Verificación de identidad");
								dialog.setHeaderText("Jugador: " + j.getNickname());
								dialog.setContentText("Introduce tu contraseña:");

								ButtonType okBtn = new ButtonType("Aceptar", ButtonType.OK.getButtonData());
								dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

								PasswordField pwField = new PasswordField();
								pwField.setPromptText("Contraseña");
								dialog.getDialogPane().setContent(pwField);

								// Convertir resultado al texto del campo
								dialog.setResultConverter(btn -> btn == okBtn ? pwField.getText() : null);

								Optional<String> result = dialog.showAndWait();
								if (!result.isPresent() || result.get() == null) {
									// El usuario canceló
									Alert alert = new Alert(AlertType.WARNING, "Carga cancelada.", ButtonType.OK);
									estilar(alert);
									alert.showAndWait();
									if (startGameButton != null)
										startGameButton.setDisable(false);
									continuar = false;
								} else {
									String enteredPass = result.get().trim();
									boolean valid = dbManager.validarLogin(j.getNickname(), enteredPass, con);
									if (!valid) {
										Alert alert = new Alert(
												AlertType.ERROR, "Contraseña incorrecta para el jugador: "
														+ j.getNickname() + "\nNo se puede cargar la partida.",
												ButtonType.OK);
										estilar(alert);
										alert.showAndWait();
										if (startGameButton != null)
											startGameButton.setDisable(false);
										continuar = false;
									}
								}
							}
						}
					} catch (Exception e) {
						System.err.println("Error validando contraseñas al cargar: " + e.getMessage());
						if (startGameButton != null)
							startGameButton.setDisable(false);
						continuar = false;
					}
				}
			}
		} else { // Tab "Nueva Partida"
			boolean hasHuman = false;
			for (int i = 0; i < joinedPlayers.size() && !hasHuman; i++) {
				if (joinedPlayers.get(i) instanceof Pinguino) {
					hasHuman = true;
				}
			}
			if (!hasHuman) {
				Alert alert = new Alert(AlertType.WARNING, "Debe haber al menos 1 jugador humano.", ButtonType.OK);
				estilar(alert);
				alert.showAndWait();
				if (startGameButton != null)
					startGameButton.setDisable(false);
				continuar = false;
			} else if (joinedPlayers.size() < 2) {
				Alert alert = new Alert(AlertType.WARNING,
						"Se necesitan al menos 2 jugadores (humanos o CPU) para jugar.", ButtonType.OK);
				estilar(alert);
				alert.showAndWait();
				if (startGameButton != null)
					startGameButton.setDisable(false);
				continuar = false;
			} else {
				ArrayList<Jugador> allPlayers = new ArrayList<>(joinedPlayers);
				String[] availableColors = { "Rojo", "Azul", "Verde", "Amarillo", "Morado", "Naranja" };

				// Asignar colores a los jugadores según su orden en el lobby para garantizar
				// unicidad
				for (int i = 0; i < allPlayers.size(); i++) {
					String assignedColor = availableColors[i % availableColors.length];
					allPlayers.get(i).setColor(assignedColor);
					System.out.println("Jugador: " + allPlayers.get(i).getNickname() + " -> Color: " + assignedColor);
				}

				GestorTaulell gt = new GestorTaulell();
				String seedToUse = loadedSeed;
				if (seedToUse == null || seedToUse.isEmpty() || !gt.esSeedValid(seedToUse)) {
					seedToUse = gt.generarSeedAleatori();
				}
				partida = new Partida(gt.generarTaulell(seedToUse), allPlayers);
			}
		}

		if (continuar && partida != null) {
			try {
				// Pasar la partida a la siguiente pantalla (se guardará estáticamente en
				// PantallaJuego)
				PantallaJuego.setPartidaInicial(partida);

				// IR A LA PANTALLA DE CARGA DE PARTIDA (con tips)
				// Forzamos recarga para que los tips y la barra se reinicien
				controlador.Main.cambiarEscena("/resources/PantallaCargaPartida.fxml", true);
			} catch (Exception e) {
				e.printStackTrace();
				if (startGameButton != null)
					startGameButton.setDisable(false);
			}
		} else {
			if (startGameButton != null)
				startGameButton.setDisable(false);
		}
	}

	@FXML
	private void handleDeleteGame() {
		String selected = savedGamesList.getSelectionModel().getSelectedItem();
		if (selected == null) {
			Alert alert = new Alert(AlertType.WARNING, "Por favor, selecciona una partida para borrar.", ButtonType.OK);
			estilar(alert);
			alert.showAndWait();
		} else {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			estilar(alert);
			alert.setTitle("Borrar Partida");
			alert.setHeaderText("¿Estás seguro de que quieres borrar esta partida?");
			alert.setContentText("Esta acción no se puede deshacer.");

			alert.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					int id = Integer.parseInt(selected.split(":")[1].trim().split(" ")[0]);
					ejecutarTareaDB("Borrando partida...", () -> {
						try (Connection con = GestorBBDD.conectarBaseDatos()) {
							if (con != null) {
								boolean exito = dbManager.esborrarPartida(id, con);
								Platform.runLater(() -> {
									if (exito) {
										System.out.println("Partida " + id + " borrada correctamente.");
										handleRefreshGames(); // Refrescar la lista (esto lanzará otra tarea)

										deleteFeedbackLabel.setText("Partida borrada correctamente.");
										deleteFeedbackLabel.setVisible(true);
										PauseTransition pause = new PauseTransition(Duration.seconds(3));
										pause.setOnFinished(e -> deleteFeedbackLabel.setVisible(false));
										pause.play();
									} else {
										Alert error = new Alert(AlertType.ERROR, "Error al borrar la partida.",
												ButtonType.OK);
										estilar(error);
										error.showAndWait();
									}
								});
							}
						}
					});
				}
			});
		}
	}

	@FXML
	private void handleLoadGame() {
		mainTabPane.getSelectionModel().select(1);
		handleRefreshGames(); // Aprovechar para refrescar la lista
	}

	@FXML
	private void handleOpenNewGame() {
		showContent();
		mainTabPane.getSelectionModel().select(0);
	}

	@FXML
	private void handleOpenLoadGame() {
		showContent();
		mainTabPane.getSelectionModel().select(1);
		handleRefreshGames();
	}

	@FXML
	private void handleOpenRanking() {
		showContent();
		mainTabPane.getSelectionModel().select(2);
		handleRefreshRanking();
	}

	@FXML
	private void handleBackToLanding() {
		showLanding();
	}

	private void showLanding() {
		landingContainer.setVisible(true);
		landingContainer.setManaged(true);
		if (rulesContainer != null) {
			rulesContainer.setVisible(true);
			rulesContainer.setManaged(true);
		}
		contentContainer.setVisible(false);
		contentContainer.setManaged(false);
	}

	private void showContent() {
		landingContainer.setVisible(false);
		landingContainer.setManaged(false);
		if (rulesContainer != null) {
			rulesContainer.setVisible(false);
			rulesContainer.setManaged(false);
		}
		contentContainer.setVisible(true);
		contentContainer.setManaged(true);
	}

	@FXML
	private void handleGoToMenu() {
		handleBackToLanding();
	}

	@FXML
	private void handleSaveGame() {
		Alert alert = new Alert(AlertType.INFORMATION);
		estilar(alert);
		alert.setTitle("Guardar Partida");
		alert.setHeaderText(null);
		alert.setContentText("No hay ninguna partida activa para guardar. Comienza una partida primero.");
		alert.showAndWait();
	}

	@FXML
	private void handleQuitGame() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		estilar(alert);
		alert.setTitle("Salir del Juego");
		alert.setHeaderText("¿Estás seguro de que quieres salir?");
		alert.setContentText("Se perderá cualquier progreso no guardado.");

		ButtonType buttonTypeNo = new ButtonType("Sí, salir");
		ButtonType buttonTypeCancel = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());

		alert.getButtonTypes().setAll(buttonTypeNo, buttonTypeCancel);

		alert.showAndWait().ifPresent(response -> {
			if (response == buttonTypeNo) {
				System.exit(0);
			}
		});
	}

	/**
	 * Aplica el stylesheet polar del menú a cualquier Alert o Dialog, de forma que
	 * todos los popups compartan la estética del juego.
	 */
	private void estilar(javafx.scene.control.Dialog<?> d) {
		try {
			// Establecer el owner para intentar que no se salga de pantalla completa
			if (landingContainer != null && landingContainer.getScene() != null) {
				d.initOwner(landingContainer.getScene().getWindow());
			} else if (contentContainer != null && contentContainer.getScene() != null) {
				d.initOwner(contentContainer.getScene().getWindow());
			}

			javafx.scene.control.DialogPane pane = d.getDialogPane();
			String css = getClass().getResource("/resources/PantallaMenu.css").toExternalForm();
			pane.getStylesheets().add(css);
		} catch (Exception e) {
			System.err.println("No se pudo aplicar el CSS al diálogo: " + e.getMessage());
		}
	}
}
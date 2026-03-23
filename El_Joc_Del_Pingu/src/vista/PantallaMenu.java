package vista;

import controlador.AudioManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
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

    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem menuItem;
    @FXML private MenuItem quitGame;

    @FXML private TextField userField;
    @FXML private PasswordField passField;

    @FXML private ListView<String> playersList;
    @FXML private ListView<String> savedGamesList;
    @FXML private ListView<String> rankingList;
    @FXML private TabPane mainTabPane;
    @FXML private Label deleteFeedbackLabel;
    @FXML private VBox landingContainer;
    @FXML private VBox contentContainer;
    @FXML private StackPane menuRoot;
    @FXML private AnchorPane menuContainer;
    @FXML private Button btnMute;
    @FXML private ImageView imgMute;

    private ArrayList<Jugador> joinedPlayers = new ArrayList<>();
    private int cpuCount = 0;
    private GestorBBDD dbManager = new GestorBBDD();

    @FXML
    private void initialize() {
        System.out.println("PantallaMenu inicializada");

        // ── Escalado dinámico PERFECTO para Laptops sin romper la config base ──
        // Limpiamos el StackPane principal y forzamos que pueda encogerse a cualquier tamaño
        menuRoot.getChildren().clear();
        menuRoot.setMinSize(0, 0);
        // Evitamos que los contenedores superiores fuercen tamaños mínimos (ej. el BorderPane raíz)
        if (menuRoot.getParent() instanceof javafx.scene.layout.Region) {
            ((javafx.scene.layout.Region) menuRoot.getParent()).setMinSize(0, 0);
        }

        // Creamos un wrapper que se ajustará a la ventana real, dándonos las dimensiones correctas
        javafx.scene.layout.Pane wrapper = new javafx.scene.layout.Pane();
        menuRoot.getChildren().add(wrapper);

        // Añadimos nuestro diseño original de 1920x1080
        wrapper.getChildren().add(menuContainer);

        // Transformación directa sobre el contenedor original
        javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(1, 1, 0, 0);
        menuContainer.getTransforms().clear();
        menuContainer.getTransforms().add(scaleTransform);

        // Recalculamos la escala y la posición en cuanto la ventana cambie de resolución
        javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
            double w = wrapper.getWidth();
            double h = wrapper.getHeight();
            if (w == 0 || h == 0) return;

            // Factor de escala respetando aspecto 16:9
            double scaleFactor = Math.min(w / 1920.0, h / 1080.0);
            scaleTransform.setX(scaleFactor);
            scaleTransform.setY(scaleFactor);

            // Centrar manualmente para que los botones nunca se queden fuera ni sus zonas de clic se desplacen
            double scaledWidth = 1920.0 * scaleFactor;
            double scaledHeight = 1080.0 * scaleFactor;
            menuContainer.setLayoutX((w - scaledWidth) / 2.0);
            menuContainer.setLayoutY((h - scaledHeight) / 2.0);
        };

        wrapper.widthProperty().addListener(resizeListener);
        wrapper.heightProperty().addListener(resizeListener);


        // Personalización de colores del ranking por posición (Se configura ANTES de cargar datos)
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
                    if (index == 0) getStyleClass().add("rank-1");
                    else if (index == 1) getStyleClass().add("rank-2");
                    else if (index == 2) getStyleClass().add("rank-3");
                    else if (index == 3 || index == 4) getStyleClass().add("rank-4-5");
                    else getStyleClass().add("rank-default");
                }
            }
        });

        showLanding();

        // Una única connexió per carregar totes les dades inicials
        try (Connection con = GestorBBDD.conectarBaseDatos()) {
            if (con != null) {
                refreshGames(con);
                refreshRanking(con);
            }
        } catch (Exception e) {
            System.err.println("Error en la inicialització de dades: " + e.getMessage());
        }

        updateMuteUI();
    }

    @FXML
    private void handleToggleMute(ActionEvent event) {
        AudioManager.getInstance().toggleMute();
        updateMuteUI();
    }

    private void updateMuteUI() {
        if (imgMute == null) return;
        boolean isMuted = AudioManager.getInstance().isMuted();
        String iconPath = isMuted ? "/assets/speaker_off.png" : "/assets/speaker_on.png";
        try {
            imgMute.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            System.err.println("Error actualizando icono de silencio: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        if (joinedPlayers.size() >= 4) {
            Alert alert = new Alert(AlertType.WARNING, "Máximo 4 jugadores permitidos.", ButtonType.OK);
            estilar(alert);
            alert.showAndWait();
            return;
        }

        String username = userField.getText().trim();
        String password = passField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING, "Debes introducir un usuario y una contraseña.", ButtonType.OK);
            estilar(alert);
            alert.showAndWait();
            return;
        }

        // Validar si el usuario ya está en la lista de jugadores unidos
        for (Jugador j : joinedPlayers) {
            if (j.getNickname().equalsIgnoreCase(username)) {
                Alert alert = new Alert(AlertType.WARNING, "El usuario '" + username + "' ya se ha unido a la partida.", ButtonType.OK);
                estilar(alert);
                alert.showAndWait();
                return;
            }
        }

        try (Connection con = GestorBBDD.conectarBaseDatos()) {
            if (con != null) {
                boolean valid = dbManager.validarLogin(username, password, con);
                if (!valid) {
                    Alert alert = new Alert(AlertType.ERROR, "Contraseña incorrecta para el usuario: " + username, ButtonType.OK);
                    estilar(alert);
                    alert.showAndWait();
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Error validando login: " + e.getMessage());
        }

        // Añadir a la lista de jugadores (Si pasa, o es nuevo o está OK)
        Pinguino p = new Pinguino(username, "Azul", new model.items.Inventari());
        p.setContrasenya(password);
        
        joinedPlayers.add(p);
        playersList.getItems().add(username + " (Humano)");
        
        userField.clear();
        passField.clear();
        System.out.println("Jugador añadido: " + username);
    }

    @FXML
    private void handleAddCPU(ActionEvent event) {
        if (joinedPlayers.size() >= 4) {
            Alert alert = new Alert(AlertType.WARNING, "Máximo 4 jugadores permitidos.", ButtonType.OK);
            estilar(alert);
            alert.showAndWait();
            return;
        }
        cpuCount++;
        String cpuName = "CPU " + cpuCount;
        Foca cpu = new Foca(cpuName, "tempColor");
        joinedPlayers.add(cpu);
        playersList.getItems().add(cpuName + " (CPU)");
        System.out.println("CPU añadida: " + cpuName);
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
        try (Connection con = GestorBBDD.conectarBaseDatos()) { 
            if (con != null) {
                refreshGames(con);
            }
        } catch (Exception e) {
            System.err.println("Error cargando partidas: " + e.getMessage());
        }
    }

    private void refreshGames(Connection con) {
        ArrayList<String> games = dbManager.llistarPartides(con);
        savedGamesList.getItems().setAll(games);
    }

    @FXML
    private void handleRefreshRanking() {
        try (Connection con = GestorBBDD.conectarBaseDatos()) {
            if (con != null) {
                refreshRanking(con);
            }
        } catch (Exception e) {
            System.err.println("Error cargando ranking: " + e.getMessage());
        }
    }

    private void refreshRanking(Connection con) {
        ArrayList<String> ranking = dbManager.obtenerRanking(con);
        rankingList.getItems().setAll(ranking);
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        Partida partida;

        int selectedTabIndex = mainTabPane.getSelectionModel().getSelectedIndex();
        
        if (selectedTabIndex == 1) { // Tab "Cargar Partida"
            String selected = savedGamesList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Alert alert = new Alert(AlertType.WARNING, "Por favor, selecciona una partida para cargar.", ButtonType.OK);
                estilar(alert);
                alert.showAndWait();
                return;
            }
            int id = Integer.parseInt(selected.split(":")[1].trim().split(" ")[0]);

            try (Connection con = GestorBBDD.conectarBaseDatos()) {
                partida = dbManager.carregarBBDD(id, con);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Verificar contraseña de cada jugador humano de la partida
            if (partida != null) {
                try (Connection con = GestorBBDD.conectarBaseDatos()) {
                    for (Jugador j : partida.getJugadors()) {
                        if (!(j instanceof Pinguino)) continue; // Las CPUs no tienen contraseña

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
                            return;
                        }

                        String enteredPass = result.get().trim();
                        boolean valid = dbManager.validarLogin(j.getNickname(), enteredPass, con);
                        if (!valid) {
                            Alert alert = new Alert(AlertType.ERROR,
                                    "Contraseña incorrecta para el jugador: " + j.getNickname() + "\nNo se puede cargar la partida.",
                                    ButtonType.OK);
                            estilar(alert);
                            alert.showAndWait();
                            return;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error validando contraseñas al cargar: " + e.getMessage());
                    return;
                }
            }
        } else { // Tab "Nueva Partida"
            boolean hasHuman = false;
            for (Jugador j : joinedPlayers) {
                if (j instanceof Pinguino) {
                    hasHuman = true;
                    break;
                }
            }

            if (!hasHuman) {
                Alert alert = new Alert(AlertType.WARNING, "Debe haber al menos 1 jugador humano.", ButtonType.OK);
                estilar(alert);
                alert.showAndWait();
                return;
            }
            if (joinedPlayers.size() < 2) {
                Alert alert = new Alert(AlertType.WARNING, "Se necesitan al menos 2 jugadores (humanos o CPU) para jugar.", ButtonType.OK);
                estilar(alert);
                alert.showAndWait();
                return;
            }

            ArrayList<Jugador> allPlayers = new ArrayList<>(joinedPlayers);
            String[] availableColors = {"Rojo", "Azul", "Verde", "Amarillo"};
            
            // Asignar colores a los jugadores según su orden en el lobby para garantizar unicidad
            for (int i = 0; i < allPlayers.size(); i++) {
                String assignedColor = availableColors[i % availableColors.length];
                allPlayers.get(i).setColor(assignedColor);
                System.out.println("Jugador: " + allPlayers.get(i).getNickname() + " -> Color: " + assignedColor);
            }

            GestorTaulell gt = new GestorTaulell();
            partida = new Partida(gt.generarTaulell(gt.generarSeedAleatori()), allPlayers);
            
            // Opcional: limpiar joinedPlayers después de empezar para una sesión limpia la próxima vez
            // joinedPlayers.clear(); 
            // playersList.getItems().clear();
        }

        if (partida != null) {
            try {
                // Pasar la partida a la siguiente pantalla
                PantallaJuego.setPartidaInicial(partida);
                
                // Usar el método centralizado para mantener resolución y estado
                controlador.Main.cambiarEscena("/resources/PantallaJuego.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void handleDeleteGame() {
        String selected = savedGamesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(AlertType.WARNING, "Por favor, selecciona una partida para borrar.", ButtonType.OK);
            estilar(alert);
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        estilar(alert);
        alert.setTitle("Borrar Partida");
        alert.setHeaderText("¿Estás seguro de que quieres borrar esta partida?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int id = Integer.parseInt(selected.split(":")[1].trim().split(" ")[0]);
                try (Connection con = GestorBBDD.conectarBaseDatos()) {
                    if (con != null) {
                        boolean exito = dbManager.esborrarPartida(id, con);
                        if (exito) {
                            System.out.println("Partida " + id + " borrada correctamente.");
                            handleRefreshGames(); // Refrescar la lista
                            
                            deleteFeedbackLabel.setText("Partida borrada correctamente.");
                            deleteFeedbackLabel.setVisible(true);
                            PauseTransition pause = new PauseTransition(Duration.seconds(3));
                            pause.setOnFinished(e -> deleteFeedbackLabel.setVisible(false));
                            pause.play();
                            
                        } else {
                            Alert error = new Alert(AlertType.ERROR, "Error al borrar la partida de la base de datos.", ButtonType.OK);
                            estilar(error);
                            error.showAndWait();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al borrar partida: " + e.getMessage());
                }
            }
        });
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
        contentContainer.setVisible(false);
        contentContainer.setManaged(false);
    }

    private void showContent() {
        landingContainer.setVisible(false);
        landingContainer.setManaged(false);
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
     * Aplica el stylesheet polar del menú a cualquier Alert o Dialog,
     * de forma que todos los popups compartan la estética del juego.
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
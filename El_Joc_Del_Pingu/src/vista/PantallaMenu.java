package vista;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

    private ArrayList<Jugador> joinedPlayers = new ArrayList<>();
    private int cpuCount = 0;
    private GestorBBDD dbManager = new GestorBBDD();

    @FXML
    private void initialize() {
        System.out.println("PantallaMenu inicializada");
        handleRefreshGames();
        handleRefreshRanking();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        if (joinedPlayers.size() >= 4) {
            Alert alert = new Alert(AlertType.WARNING, "Máximo 4 jugadores permitidos.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String username = userField.getText().trim();
        String password = passField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING, "Debes introducir un usuario y una contraseña.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try (Connection con = GestorBBDD.conectarBaseDatos()) {
            if (con != null) {
                boolean valid = dbManager.validarLogin(username, password, con);
                if (!valid) {
                    Alert alert = new Alert(AlertType.ERROR, "Contraseña incorrecta para el usuario: " + username, ButtonType.OK);
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
        try (Connection con = GestorBBDD.conectarBaseDatos()) { // Hardcoded for demo/dev
            if (con != null) {
                ArrayList<String> games = dbManager.llistarPartides(con);
                savedGamesList.getItems().setAll(games);
            }
        } catch (Exception e) {
            System.err.println("Error cargando partidas: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshRanking() {
        try (Connection con = GestorBBDD.conectarBaseDatos()) {
            if (con != null) {
                ArrayList<String> ranking = dbManager.obtenerRanking(con);
                rankingList.getItems().setAll(ranking);
            }
        } catch (Exception e) {
            System.err.println("Error cargando ranking: " + e.getMessage());
        }
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        Partida partida;

        int selectedTabIndex = mainTabPane.getSelectionModel().getSelectedIndex();
        
        if (selectedTabIndex == 1) { // Tab "Cargar Partida"
            String selected = savedGamesList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                System.out.println("Por favor, selecciona una partida para cargar.");
                return;
            }
            int id = Integer.parseInt(selected.split(":")[1].trim().split(" ")[0]);
            
            try (Connection con = GestorBBDD.conectarBaseDatos()) {
                partida = dbManager.carregarBBDD(id, con);
            } catch (Exception e) {
                e.printStackTrace();
                return;
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
                alert.showAndWait();
                return;
            }
            if (joinedPlayers.size() < 2) {
                Alert alert = new Alert(AlertType.WARNING, "Se necesitan al menos 2 jugadores (humanos o CPU) para jugar.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            ArrayList<Jugador> allPlayers = new ArrayList<>(joinedPlayers);
            String[] availableColors = {"Rojo", "Azul", "Verde", "Amarillo"};
            
            // Asignar colores a los jugadores
            for (int i = 0; i < allPlayers.size(); i++) {
                allPlayers.get(i).setColor(availableColors[i % availableColors.length]);
            }

            GestorTaulell gt = new GestorTaulell();
            partida = new Partida(gt.generarTaulell(gt.generarSeedAleatori()), allPlayers);
        }

        if (partida != null) {
            try {
                // Pasar la partida a la siguiente pantalla
                PantallaJuego.setPartidaInicial(partida);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaJuego.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("El Juego del Pingüino");
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
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
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
    private void handleGoToMenu() {
        mainTabPane.getSelectionModel().select(0);
    }

    @FXML
    private void handleSaveGame() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Guardar Partida");
        alert.setHeaderText(null);
        alert.setContentText("No hay ninguna partida activa para guardar. Comienza una partida primero.");
        alert.showAndWait();
    }

    @FXML
    private void handleQuitGame() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
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
}
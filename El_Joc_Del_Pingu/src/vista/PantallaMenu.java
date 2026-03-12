package vista;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    @FXML private TextField userField;
    @FXML private PasswordField passField;

    @FXML private ListView<String> playersList;
    @FXML private ListView<String> savedGamesList;
    @FXML private TabPane mainTabPane;

    private ArrayList<Jugador> humanPlayers = new ArrayList<>();
    private GestorBBDD dbManager = new GestorBBDD();

    @FXML
    private void initialize() {
        System.out.println("PantallaMenu inicializada");
        handleRefreshGames();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userField.getText().trim();
        if (username.isEmpty()) return;

        // Añadir a la lista de humanos
        Pinguino p = new Pinguino(username, "Azul", new model.items.Inventari());
        humanPlayers.add(p);
        playersList.getItems().add(username + " (Humano)");
        
        userField.clear();
        passField.clear();
        System.out.println("Jugador añadido: " + username);
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
            if (humanPlayers.isEmpty()) {
                System.out.println("Añade al menos un jugador humano.");
                return;
            }

            ArrayList<Jugador> allPlayers = new ArrayList<>(humanPlayers);
            String[] colors = {"Rojo", "Verde", "Amarillo", "Violeta"};
            
            // Rellenar con CPUs hasta llegar a 4
            while (allPlayers.size() < 4) {
                int cpuNum = allPlayers.size() + 1;
                Foca cpu = new Foca("CPU " + cpuNum, colors[allPlayers.size() % colors.length]);
                allPlayers.add(cpu);
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
    private void handleQuitGame() {
        System.exit(0);
    }
}
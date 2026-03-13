package controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {

		try {
			// Carrega la pantalla de menú inicial
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
			Parent root = loader.load();

			primaryStage.setTitle("El Joc del Pingüí - Menú");
			primaryStage.setScene(new Scene(root));
			primaryStage.setWidth(1090);
			primaryStage.setHeight(830);
			primaryStage.centerOnScreen();
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		launch(args);
	}
}

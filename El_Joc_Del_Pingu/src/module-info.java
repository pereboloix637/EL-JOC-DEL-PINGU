/**
 * 
 */
/**
 * 
 */
module El_Joc_Del_Pingu {
	requires java.sql;
	requires javafx.fxml;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.media;
	opens vista to javafx.fxml;
	exports vista;
	exports controlador;
}
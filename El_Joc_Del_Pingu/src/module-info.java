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
	opens vista to javafx.fxml;
	exports vista;
	exports controlador;
}
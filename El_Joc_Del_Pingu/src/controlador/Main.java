package controlador;

import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        
        Scanner scan = new Scanner(System.in);
        
        // Conectamos usando el método del archivo BBDD.java
        Connection con = BBDD.conectarBaseDatos(scan);
        
        // Comprobamos si la conexión ha funcionado
        if (con != null) {
            System.out.println("¡Conexión exitosa!");
        } else {
            System.out.println("No se ha podido conectar.");
        }
        
        // Cerramos la conexión
        BBDD.cerrar(con);
        scan.close();
    }
}

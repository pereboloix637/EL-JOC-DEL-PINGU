package controlador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LlegirEnv { // Para leer las creedenciales de la Base de Datos (BBDD o BD)
    private static Map<String, String> envVars = new HashMap<>();

    static {
        cargarEnv();
    }

    private static void cargarEnv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String lineaTrim = linea.trim();

                // Solo procesamos si la línea NO está vacía Y NO empieza por '#'
                if (!lineaTrim.isEmpty() && !lineaTrim.startsWith("#")) {

                    String[] partes = lineaTrim.split("=", 2);
                    if (partes.length == 2) {
                        String clave = partes[0].trim();
                        String valor = partes[1].trim();
                        envVars.put(clave, valor);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Fichero .env no encontrado. Usando valores por defecto.");
        }
    }

    public static String get(String clave) {
        return envVars.getOrDefault(clave, "");
    }
}

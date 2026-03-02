package controlador;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import model.core.Partida;
import model.entitats.Foca;
import model.entitats.Jugador;
import model.entitats.Pingui;

/**
 * Clase que proporciona métodos para interactuar con una base de datos Oracle.
 */
public class GestorBBDD {

	public static void main(String[] args) {

		Scanner scan = new Scanner(System.in);

		// Conectamos usando el método del archivo BBDD.java
		Connection con = GestorBBDD.conectarBaseDatos(scan);

		// Comprobamos si la conexión ha funcionado
		if (con != null) {
			System.out.println("¡Conexión exitosa!");
		} else {
			System.out.println("No se ha podido conectar.");
		}

		// Cerramos la conexión
		GestorBBDD.cerrar(con);
		scan.close();
	}

	/**
	 * Intenta establecer una conexión a la base de datos Oracle. NO HACE FALTA QUE
	 * ENTENDÁIS CÓMO FUNCIONA, SE HACE TODO DE MANERA AUTOMÁTICA.
	 *
	 * @param scan Scanner de main con el que vais a leer por consola
	 * @return Objeto Connection si la conexión es exitosa, null en caso contrario.
	 *         LA VARIABLE QUE DEVUELVE LA TENÉIS QUE GUARDAR PARA LAS DEMÁS
	 *         FUNCIONES
	 */
	public static Connection conectarBaseDatos(Scanner scan) {
		System.out.println("Intentando conectarse a la base de datos...");

		// 1) Elegir entorno con validación
		String entorno = "";
		boolean valido = false;
		while (!valido) {
			// PODEIS HARDCODEAR ESTAS VARIABLES SI VAIS A USAR SIEMPRE LAS MISMAS
			// VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
			System.out.println("Selecciona centro o fuera de centro (CENTRO/FUERA):");
			entorno = scan.nextLine().trim().toLowerCase();

			if (entorno.equalsIgnoreCase("centro") || entorno.equalsIgnoreCase("fuera")) {
				valido = true;
			} else {
				System.out.println("Entrada no válida. Escribe CENTRO o FUERA.");
			}
		}

		String url = entorno.equals("centro") ? "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2"
				: "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";

		// 2) Pedir credenciales (con trim para evitar espacios raros)
		// PODEIS HARDCODEAR ESTAS CREDENCIALES SI VAIS A USAR SIEMPRE LAS MISMAS
		// VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
		System.out.println("¿Usuario?");
		String user = scan.nextLine().trim();

		System.out.println("¿Contraseña?");
		String pwd = scan.nextLine(); // aquí NO hago trim por si la contraseña tuviera espacios

		// 3) Conectar
		try {
			// En muchos casos con JDBC moderno no hace falta, pero lo dejamos por si acaso
			Class.forName("oracle.jdbc.driver.OracleDriver");

			Connection con = DriverManager.getConnection(url, user, pwd);

			// 4) Comprobar que la conexión es válida (timeout 5 s)
			if (con.isValid(5)) {
				System.out.println("Conectados a la base de datos (" + entorno.toUpperCase() + ").");
			} else {
				System.out.println("Conexión creada, pero no parece válida. Revisa red/URL.");
			}

			return con;

		} catch (ClassNotFoundException e) {
			System.out.println("No se ha encontrado el driver de Oracle. ¿Está el ojdbc en el proyecto?");
		} catch (SQLException e) {
			System.out.println("No se pudo conectar. Revisa URL/usuario/contraseña.");
			System.out.println("Detalle: " + e.getMessage());
		}

		return null;
	}

	/**
	 * Cierra la conexión con la BBDD.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 */
	public static void cerrar(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException ignored) {
			}
		}
	}

	/**
	 * Realiza una inserción en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de inserción que hayáis creado.
	 */
	public static int insert(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Insert");
	}

	/**
	 * Realiza una actualización en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de actualización que hayáis creado.
	 */
	public static int update(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Update");
	}

	/**
	 * Realiza una eliminación en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de eliminación que hayáis creado.
	 */
	public static int delete(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Delete");
	}

	/**
	 * Realiza una consulta en la base de datos y devuelve los resultados.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de consulta.
	 * @return Devuelve un ArrayList con todas las filas del SELECT. Cada fila es un
	 *         Map con sus columnas (columna -> valor).
	 */
	public static ArrayList<LinkedHashMap<String, String>> select(Connection con, String sql) {

		ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();

		if (con == null) {
			System.out.println("No hay conexión. Llama antes a conectarBaseDatos().");
			return resultados;
		}

		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

			ResultSetMetaData meta = rs.getMetaData();
			int numColumnas = meta.getColumnCount();

			while (rs.next()) {
				LinkedHashMap<String, String> fila = new LinkedHashMap<>();

				for (int i = 1; i <= numColumnas; i++) {
					String columna = meta.getColumnLabel(i);
					String valor = rs.getString(i);
					fila.put(columna, valor);
				}

				resultados.add(fila);
			}

		} catch (SQLException e) {
			System.out.println("Error en SELECT: " + e.getMessage());
		}

		return resultados;
	}

	/**
	 * Imprime los resultados de una consulta SELECT en la base de datos. EN ESTE
	 * CASO SÍ PODÉIS IMPRIMIR MÁS DE UNA FILA.
	 *
	 * @param con                         Objeto Connection que representa la
	 *                                    conexión a la base de datos.
	 * @param sql                         Sentencia SQL de consulta.
	 * @param listaElementosSeleccionados Array de Strings con los nombres de las
	 *                                    columnas seleccionadas.
	 */
	public static void print(Connection con, String sql, String[] listaElementosSeleccionados) {
		if (con == null) {
			System.out.println("No hay conexión. Llama antes a conectarBaseDatos().");
			return;
		}

		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

			int fila = 0;
			boolean hayResultados = false;

			while (rs.next()) {
				hayResultados = true;
				fila++;
				System.out.println("---- Fila " + fila + " ----");
				for (String col : listaElementosSeleccionados) {
					System.out.println(col + ": " + rs.getString(col));
				}
			}

			if (!hayResultados) {
				System.out.println("No se ha encontrado nada");
			}

		} catch (SQLException e) {
			System.out.println("Error en SELECT: " + e.getMessage());
		}
	}

	/**
	 * Ejecuta las consultas Insert, Update o Delete.
	 *
	 * @param con      Objeto Connection que representa la conexión a la base de
	 *                 datos.
	 * @param sql      Sentencia SQL que se va a ejecutar.
	 * @param etiqueta Consulta a ejecutar -> Insert / Update / Delete
	 * @return Número de filas afectadas
	 */
	public static int executeInsUpDel(Connection con, String sql, String etiqueta) {
		if (con == null) {
			System.out.println("No hay conexión. Llama antes a conectarBaseDatos().");
			return 0;
		}

		try (Statement st = con.createStatement()) {
			int filas = st.executeUpdate(sql);
			System.out.println(etiqueta + " hecho correctamente. Filas afectadas: " + filas);
			return filas;
		} catch (SQLException e) {
			System.out.println("Ha habido un error en " + etiqueta + ": " + e.getMessage());
			return 0;
		}
	}

	/**
	 * Guarda l'estat actual d'una partida a la base de dades. Si la partida és nova
	 * (id == 0), fa un INSERT de totes les taules relacionades. Si la partida ja
	 * existeix (id != 0), fa un UPDATE amb l'estat actual.
	 *
	 * @param partida Objecte Partida amb totes les dades a guardar.
	 * @param con     Connexió activa a la base de dades.
	 */
	public void guardarBBDD(Partida partida, Connection con) {

	    try (Statement st = con.createStatement()) {

	        if (partida.getId() == 0) {
	            // ── NOVA PARTIDA: INSERT ──────────────────────────────────────────

	            // Obtenim el pròxim ID disponible per a la partida
	            ArrayList<LinkedHashMap<String, String>> resultat = select(con,
	                    "SELECT MAX(id) AS MAX_ID FROM partida");

	            int nouId = 1;
	            if (!resultat.isEmpty() && resultat.get(0).get("MAX_ID") != null) {
	                nouId = Integer.parseInt(resultat.get(0).get("MAX_ID")) + 1;
	            }

	            // Insertem la partida
	            String sqlPartida = "INSERT INTO partida (id) VALUES (" + nouId + ")";
	            insert(con, sqlPartida);

	            // Insertem el taulell
	            String sqlTaulell = "INSERT INTO taulell (partida_id, seed) VALUES (" + nouId + ", '"
	                    + partida.getSeed() + "')";
	            insert(con, sqlTaulell);

	            // Insertem cada jugador
	            for (Jugador jugador : partida.getJugadors()) {

	                // ── Si el jugador encara no existeix a la taula jugador, l'inserim ──
	                if (jugador.getId() == 0) {
	                    ArrayList<LinkedHashMap<String, String>> resultatJug = select(con,
	                            "SELECT MAX(id) AS MAX_ID FROM jugador");

	                    int nouIdJug = 1;
	                    if (!resultatJug.isEmpty() && resultatJug.get(0).get("MAX_ID") != null) {
	                        nouIdJug = Integer.parseInt(resultatJug.get(0).get("MAX_ID")) + 1;
	                    }
	                    jugador.setId(nouIdJug);

	                    // Si és una Foca és CPU, si és un Pingui és humà
	                    int esCpu = jugador instanceof Foca ? 1 : 0;
	                    
	                    String sqlJugador = "INSERT INTO jugador (id, nom, color, es_cpu) VALUES ("
	                            + nouIdJug + ", '"
	                            + jugador.getNickname() + "', '"
	                            + jugador.getColor() + "', "
	                            + esCpu + ")";
	                    insert(con, sqlJugador);
	                }

	                // Insertem el jugador a jugador_partida
	                String sqlJP = "INSERT INTO jugador_partida (jugador_id, partida_id) VALUES ("
	                        + jugador.getId() + ", " + nouId + ")";
	                insert(con, sqlJP);

	                // Només els pinguins tenen inventari
	                if (jugador instanceof Pingui pingu) {
	                    String sqlInv = "INSERT INTO inventari (jugador_id, partida_id) VALUES ("
	                            + pingu.getId() + ", " + nouId + ")";
	                    insert(con, sqlInv);
	                }
	            }

	        } else {
	            // ── PARTIDA EXISTENT: UPDATE ──────────────────────────────────────

	            // Actualitzem torn actual i finalitzada
	            int finalitzada = partida.isFinalitzada() ? 1 : 0;
	            String sqlPartida = "UPDATE partida SET torn_actual = " + partida.getTorns()
	                    + ", finalitzada = " + finalitzada
	                    + " WHERE id = " + partida.getId();
	            update(con, sqlPartida);

	            // Actualitzem cada jugador
	            for (Jugador jugador : partida.getJugadors()) {

	                int esGuanyador = (partida.getGuanyador() != null
	                        && partida.getGuanyador().getId() == jugador.getId()) ? 1 : 0;

	                String sqlJP = "UPDATE jugador_partida SET posicio = " + jugador.getPosicio()
	                        + ", torns_bloquejat = " + jugador.getTornsBloquejat()
	                        + ", es_guanyador = " + esGuanyador
	                        + " WHERE jugador_id = " + jugador.getId()
	                        + " AND partida_id = " + partida.getId();
	                update(con, sqlJP);

	                // Només els pinguins tenen inventari
	                if (jugador instanceof Pingui pingu) {
	                    String sqlInv = "UPDATE inventari SET daus = " + pingu.getInventari().getDausEspecials()
	                            + ", peixos = " + pingu.getInventari().getPeixos()
	                            + ", boles_neu = " + pingu.getInventari().getBoles()
	                            + " WHERE jugador_id = " + pingu.getId()
	                            + " AND partida_id = " + partida.getId();
	                    update(con, sqlInv);
	                }
	            }
	        }

	    } catch (SQLException e) {
	        System.out.println("Error en guardarBBDD: " + e.getMessage());
	    }
	}

	public Partida carregarBBDD(int id, Connection con) {
		// Falta fer
		return null;
	}
}
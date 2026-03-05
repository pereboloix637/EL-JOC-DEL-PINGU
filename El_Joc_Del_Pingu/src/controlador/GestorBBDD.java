package controlador;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

import model.core.Partida;
import model.entitats.Foca;
import model.entitats.Jugador;
import model.entitats.Pingui;

/**
 * Clase que proporciona métodos para interactuar con una base de datos Oracle.
 */
public class GestorBBDD {

	// GESTOR DE TABLERO USADO EN GuardarBBDD()
	GestorTaulell Gtaula = new GestorTaulell();

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
		try {
			// 1. Gestionar l'ID de la Partida i la seva existència
			int pId = partida.getId();
			int torns = partida.getTorns();
			int finalitzada = partida.isFinalitzada() ? 1 : 0;

			if (pId == 0) {
				ArrayList<LinkedHashMap<String, String>> resultat = select(con, "SELECT MAX(id) AS MAX_ID FROM partida");
				int nouId = (resultat.isEmpty() || resultat.get(0).get("MAX_ID") == null) ? 1
						: Integer.parseInt(resultat.get(0).get("MAX_ID")) + 1;
				partida.setId(nouId);
				pId = nouId;

				String sqlInsPartida = "INSERT INTO partida (id, torn_actual, finalitzada) VALUES (" + pId + ", " + torns
						+ ", " + finalitzada + ")";
				insert(con, sqlInsPartida);

				String seed = Gtaula.obtenirSeedTaulell(partida.getTaulell());
				String sqlInsTaulell = "INSERT INTO taulell (partida_id, seed) VALUES (" + pId + ", '" + seed + "')";
				insert(con, sqlInsTaulell);
			} else {
				String sqlUpdPartida = "UPDATE partida SET torn_actual = " + torns + ", finalitzada = " + finalitzada
						+ " WHERE id = " + pId;
				update(con, sqlUpdPartida);
			}

			// 2. Gestionar cada Jugador
			for (Jugador j : partida.getJugadors()) {
				// ── 2.1 Garantir que el jugador existeix a la taula 'jugador' (global) ──
				if (j.getId() == 0) {
					String nomJ = j.getNickname();
					String sqlCheck = "SELECT id FROM jugador WHERE nom = '" + nomJ + "'";
					ArrayList<LinkedHashMap<String, String>> resCheck = select(con, sqlCheck);

					if (!resCheck.isEmpty()) {
						j.setId(Integer.parseInt(resCheck.get(0).get("ID")));
					} else {
						ArrayList<LinkedHashMap<String, String>> resMax = select(con,
								"SELECT MAX(id) AS MAX_ID FROM jugador");
						int nouIdJ = (resMax.isEmpty() || resMax.get(0).get("MAX_ID") == null) ? 1
								: Integer.parseInt(resMax.get(0).get("MAX_ID")) + 1;
						j.setId(nouIdJ);

						String colorJ = j.getColor();
						int esCpu = (j instanceof Foca ? 1 : 0);
						String sqlInsJ = "INSERT INTO jugador (id, nom, color, es_cpu) VALUES (" + nouIdJ + ", '" + nomJ
								+ "', '" + colorJ + "', " + esCpu + ")";
						insert(con, sqlInsJ);
					}
				}

				// ── 2.2 Upsert a 'jugador_partida' (estat del jugador en aquesta partida) ──
				int jId = j.getId();
				int pos = j.getPosicio();
				int tBloq = j.getTornsBloquejat();
				int esGuanyador = (partida.getGuanyador() != null
						&& partida.getGuanyador().getNickname().equals(j.getNickname())) ? 1 : 0;

				String sqlCheckJP = "SELECT * FROM jugador_partida WHERE jugador_id = " + jId + " AND partida_id = "
						+ pId;
				ArrayList<LinkedHashMap<String, String>> resJP = select(con, sqlCheckJP);

				if (resJP.isEmpty()) {
					String sqlInsJP = "INSERT INTO jugador_partida (jugador_id, partida_id, posicio, torns_bloquejat, es_guanyador) VALUES ("
							+ jId + ", " + pId + ", " + pos + ", " + tBloq + ", " + esGuanyador + ")";
					insert(con, sqlInsJP);
				} else {
					String sqlUpdJP = "UPDATE jugador_partida SET posicio = " + pos + ", torns_bloquejat = " + tBloq
							+ ", es_guanyador = " + esGuanyador + " WHERE jugador_id = " + jId + " AND partida_id = "
							+ pId;
					update(con, sqlUpdJP);
				}

				// ── 2.3 Upsert a 'inventari' (només Pinguins) ──
				if (j instanceof Pingui p) {
					int daus = p.getInventari().getDausEspecials();
					int peixos = p.getInventari().getPeixos();
					int boles = p.getInventari().getBoles();

					String sqlCheckInv = "SELECT * FROM inventari WHERE jugador_id = " + jId + " AND partida_id = "
							+ pId;
					ArrayList<LinkedHashMap<String, String>> resInv = select(con, sqlCheckInv);

					if (resInv.isEmpty()) {
						String sqlInsInv = "INSERT INTO inventari (jugador_id, partida_id, daus, peixos, boles_neu) VALUES ("
								+ jId + ", " + pId + ", " + daus + ", " + peixos + ", " + boles + ")";
						insert(con, sqlInsInv);
					} else {
						String sqlUpdInv = "UPDATE inventari SET daus = " + daus + ", peixos = " + peixos
								+ ", boles_neu = " + boles + " WHERE jugador_id = " + jId + " AND partida_id = " + pId;
						update(con, sqlUpdInv);
					}
				}
			}
			System.out.println("Partida guardada amb èxit.");
		} catch (Exception e) {
			System.err.println("Error en guardarBBDD: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Partida carregarBBDD(int id, Connection con) {

		try {
			// 1. Carregar dades de Partida i Taulell
			String sqlPartida = "SELECT p.*, t.seed FROM partida p JOIN taulell t ON p.id = t.partida_id WHERE p.id = "
					+ id;
			ArrayList<LinkedHashMap<String, String>> resPartida = select(con, sqlPartida);

			if (resPartida.isEmpty()) {
				System.out.println("No s'ha trobat cap partida amb ID: " + id);
				return null;
			}

			LinkedHashMap<String, String> dadesPartida = resPartida.get(0);
			String seed = dadesPartida.get("SEED");
			int tornActual = dadesPartida.get("TORN_ACTUAL") != null ? Integer.parseInt(dadesPartida.get("TORN_ACTUAL"))
					: 0;
			boolean finalitzada = dadesPartida.get("FINALITZADA") != null
					&& dadesPartida.get("FINALITZADA").equals("1");

			// 2. Carregar Jugadors associats a la partida
			String sqlJugadors = "SELECT j.*, jp.posicio, jp.torns_bloquejat, jp.es_guanyador " + "FROM jugador j "
					+ "JOIN jugador_partida jp ON j.id = jp.jugador_id " + "WHERE jp.partida_id = " + id;
			ArrayList<LinkedHashMap<String, String>> resJugadors = select(con, sqlJugadors);

			ArrayList<Jugador> llistaJugadors = new ArrayList<>();
			Jugador guanyador = null;

			for (LinkedHashMap<String, String> dJ : resJugadors) {
				int jId = Integer.parseInt(dJ.get("ID"));
				String nom = dJ.get("NOM");
				String color = dJ.get("COLOR");
				int pos = Integer.parseInt(dJ.get("POSICIO"));
				int tBloq = Integer.parseInt(dJ.get("TORNS_BLOQUEJAT"));
				boolean esCpu = dJ.get("ES_CPU").equals("1");

				Jugador j;
				if (esCpu) {
					// És una Foca (CPU)
					j = new Foca(nom, color);
				} else {
					// És un Pingüí, carreguem el seu inventari
					model.items.Inventari inv = new model.items.Inventari();
					String sqlInv = "SELECT * FROM inventari WHERE jugador_id = " + jId + " AND partida_id = " + id;
					ArrayList<LinkedHashMap<String, String>> resInv = select(con, sqlInv);

					if (!resInv.isEmpty()) {
						LinkedHashMap<String, String> dI = resInv.get(0);
						// Recuperem les quantitats de cada ítem
						int daus = dI.get("DAUS") != null ? Integer.parseInt(dI.get("DAUS")) : 0;
						int peixos = dI.get("PEIXOS") != null ? Integer.parseInt(dI.get("PEIXOS")) : 0;
						int boles = dI.get("BOLES_NEU") != null ? Integer.parseInt(dI.get("BOLES_NEU")) : 0;

						// Afegim els ítems a l'inventari
						for (int i = 0; i < daus; i++) {
							inv.afegirItem(new model.items.Dau("Dau ràpid", 1, 5, 10));
						}
						for (int i = 0; i < peixos; i++) {
							inv.afegirItem(new model.items.Peix("Peix", 1));
						}
						for (int i = 0; i < boles; i++) {
							inv.afegirItem(new model.items.BolaNeu("Bola de Neu", 1));
						}
					}
					j = new Pingui(nom, color, inv);
				}

				j.setId(jId);
				j.setPosicio(pos);
				j.setTornsBloquejat(tBloq);
				llistaJugadors.add(j);

				if (dJ.get("ES_GUANYADOR") != null && dJ.get("ES_GUANYADOR").equals("1")) {
					guanyador = j;
				}
			}

			// 3. Reconstruir l'objecte Partida
			GestorTaulell gt = new GestorTaulell();
			Partida partida = new Partida(gt.generarTaulell(seed), llistaJugadors);
			partida.setId(id);
			partida.setTorns(tornActual);
			partida.setFinalitzada(finalitzada);
			partida.setGuanyador(guanyador);

			System.out.println("Partida amb ID " + id + " carregada correctament.");
			return partida;

		} catch (Exception e) {
			System.out.println("Error en carregarBBDD: " + e.getMessage());
			return null;
		}
	}
}
package controlador;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import model.core.Partida;
import model.entitats.Foca;
import model.entitats.Jugador;
import model.entitats.Pinguino;

/**
 * Clase que proporciona métodos para interactuar con una base de datos Oracle.
 */
public class GestorBBDD {

    // GESTOR DE TABLERO USADO EN GuardarBBDD()
    GestorTaulell Gtaula = new GestorTaulell();

    /**
     * Intenta conectar a CENTRO, si falla intenta FUERA
     */
    public static Connection conectarBaseDatos() {
        System.out.println("Intentando conectarse a la base de datos...");

        // Intenta CENTRO primero
        Connection con = intentarConexion("CENTRO");
        if (con != null) {
            return con;
        }

        // Si falla, intenta FUERA
        System.out.println("CENTRO no disponible. Intentando FUERA...");
        con = intentarConexion("FUERA");
        if (con != null) {
            return con;
        }

        // Si ambas fallan
        System.out.println("No se pudo conectar a ningún entorno.");
        return null;
    }

    /**
     * Intenta conectar a un entorno específico (CENTRO o FUERA)
     */
    private static Connection intentarConexion(String entorno) {
        try {
            // Carga el driver de Oracle
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Obtiene credenciales del .env
            String url = LlegirEnv.get("DB_URL_" + entorno);
            String user = LlegirEnv.get("DB_USER");
            String pwd = LlegirEnv.get("DB_PASSWORD");

            // Crea la conexión
            Connection con = DriverManager.getConnection(url, user, pwd);

            // Valida que funcione
            if (con.isValid(5)) {
                System.out.println("✓ Conectado a " + entorno + ".");
                return con;
            }

        } catch (SQLException e) {
            // Fallo de conexión (silencioso)
        } catch (ClassNotFoundException e) {
            System.out.println("✗ Driver Oracle no encontrado.");
        }

        return null;
    }

    /**
     * Cierra la conexión con la BBDD.
     *
     * @param con Objeto Connection que representa la conexión a la base de
     * datos.
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
     * @param con Objeto Connection que representa la conexión a la base de
     * datos.
     * @param sql Sentencia SQL de inserción que hayáis creado.
     */
    public static int insert(Connection con, String sql) {
        return executeInsUpDel(con, sql, "Insert");
    }

    /**
     * Realiza una actualización en la base de datos.
     *
     * @param con Objeto Connection que representa la conexión a la base de
     * datos.
     * @param sql Sentencia SQL de actualización que hayáis creado.
     */
    public static int update(Connection con, String sql) {
        return executeInsUpDel(con, sql, "Update");
    }

    /**
     * Realiza una eliminación en la base de datos.
     *
     * @param con Objeto Connection que representa la conexión a la base de
     * datos.
     * @param sql Sentencia SQL de eliminación que hayáis creado.
     */
    public static int delete(Connection con, String sql) {
        return executeInsUpDel(con, sql, "Delete");
    }

    /**
     * Realiza una consulta en la base de datos y devuelve los resultados.
     *
     * @param con Objeto Connection que representa la conexión a la base de
     * datos.
     * @param sql Sentencia SQL de consulta.
     * @return Devuelve un ArrayList con todas las filas del SELECT. Cada fila
     * es un Map con sus columnas (columna -> valor).
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
     * Ejecuta las consultas Insert, Update o Delete.
     *
     * @param con Objeto Connection que representa la conexión a la base de
     * datos.
     * @param sql Sentencia SQL que se va a ejecutar.
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

    public int obtenirRecordVictories(Connection con) {
        if (con == null) return 0;

        String sql = "SELECT MAX(victories) AS MAX_VIC FROM jugador";
        ArrayList<LinkedHashMap<String, String>> res = select(con, sql);
        
        if (!res.isEmpty() && res.get(0).get("MAX_VIC") != null) {
            try {
                return Integer.parseInt(res.get(0).get("MAX_VIC"));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Guarda l'estat actual d'una partida a la base de dades. Si la partida és
     * nova (id == 0), fa un INSERT de totes les taules relacionades. Si la
     * partida ja existeix (id != 0), fa un UPDATE amb l'estat actual.
     *
     * @param partida Objecte Partida amb totes les dades a guardar.
     * @param con Connexió activa a la base de dades.
     */
    public void guardarBBDD(Partida partida, Connection con) {
        try {
            // 1. Gestionar l'ID de la Partida i la seva existència
            int pId = partida.getId();
            int torns = partida.getTorns();
            int finalitzada = partida.isFinalitzada() ? 1 : 0;

            if (pId == 0) {
                // Usamos la secuencia Oracle para obtener el ID de forma segura
                ArrayList<LinkedHashMap<String, String>> resultat = select(con,
                        "SELECT SEQ_PARTIDA.NEXTVAL AS NOU_ID FROM DUAL");
                int nouId = Integer.parseInt(resultat.get(0).get("NOU_ID"));

                partida.setId(nouId);
                pId = nouId;

                String sqlInsPartida = "INSERT INTO partida (id, torn_actual, finalitzada) VALUES (" + pId + ", "
                        + torns + ", " + finalitzada + ")";
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
                System.out.println("Procesando jugador para guardar: " + j.getNickname() + " (ID actual: " + j.getId() + ")");

                // ── 2.1 Garantir que el jugador existeix a la taula 'jugador' (global) ──
                if (j.getId() == 0) {
                    String nomJ = j.getNickname();
                    String sqlCheck = "SELECT id FROM jugador WHERE nom = '" + nomJ + "'";
                    ArrayList<LinkedHashMap<String, String>> resCheck = select(con, sqlCheck);

                    if (!resCheck.isEmpty()) {
                        // Recuperar ID de forma robusta (Oracle suele devolver mayúsculas)
                        String dbIdStr = resCheck.get(0).get("ID");
                        if (dbIdStr == null) {
                            dbIdStr = resCheck.get(0).get("id"); // Intento en minúsculas por si acaso
                        }
                        if (dbIdStr != null) {
                            j.setId(Integer.parseInt(dbIdStr));
                            System.out.println("  -> Jugador encontrado en DB. Asignando ID: " + j.getId());

                            // Actualizar color y asegurar que es_cpu sea correcto (por si acaso)
                            int esCpu = (j instanceof Foca ? 1 : 0);
                            update(con, "UPDATE jugador SET color = '" + j.getColor() + "', es_cpu = " + esCpu + " WHERE id = " + j.getId());
                        }
                    } else {
                        ArrayList<LinkedHashMap<String, String>> resMax = select(con,
                                "SELECT MAX(id) AS MAX_ID FROM jugador");
                        String maxIdStr = resMax.get(0).get("MAX_ID");
                        if (maxIdStr == null) {
                            maxIdStr = resMax.get(0).get("max_id");
                        }

                        int nouIdJ = (maxIdStr == null) ? 1 : Integer.parseInt(maxIdStr) + 1;
                        j.setId(nouIdJ);
                        System.out.println("  -> Jugador nuevo. Generando ID: " + j.getId());

                        String colorJ = j.getColor();
                        int esCpu = (j instanceof Foca ? 1 : 0);
                        String pass = "";
                        int vic = 0;
                        if (j instanceof Pinguino pingu) {
                            pass = pingu.getContrasenya() != null ? pingu.getContrasenya() : "";
                        }
                        // Insertamos usando UTL_RAW.CAST_TO_RAW para los nuevos campos RAW(64) y RAW(16)
                        String sqlInsJ = "INSERT INTO jugador (id, nom, color, es_cpu, contrasenya, salt, victories) VALUES ("
                                + nouIdJ + ", '" + nomJ + "', '" + colorJ + "', " + esCpu + ", "
                                + "UTL_RAW.CAST_TO_RAW('" + pass + "'), UTL_RAW.CAST_TO_RAW(''), " + vic + ")";
                        insert(con, sqlInsJ);
                    }
                } else {
                    System.out.println("  -> El jugador ya tenía ID " + j.getId() + ". Actualizando color.");
                    update(con, "UPDATE jugador SET color = '" + j.getColor() + "' WHERE id = " + j.getId());
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
                if (j instanceof Pinguino p) {
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

            // Si la partida ha finalitzat, incrementem les victòries del guanyador manualment (sense triggers)
            if (partida.isFinalitzada() && partida.getGuanyador() != null) {
                int idGuanyador = partida.getGuanyador().getId();
                if (idGuanyador != 0) {
                    update(con, "UPDATE jugador SET victories = victories + 1 WHERE id = " + idGuanyador);
                }
            }
            System.out.println("Partida guardada con éxito.");
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
                System.out.println("No se ha encontrado ninguna partida con ID: " + id);
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
                            inv.afegirItem(new model.items.Dau("Dado rápido", 1, 5, 10));
                        }
                        for (int i = 0; i < peixos; i++) {
                            inv.afegirItem(new model.items.Peix("Pez", 1));
                        }
                        for (int i = 0; i < boles; i++) {
                            inv.afegirItem(new model.items.BolaNeu("Bola de Nieve", 1));
                        }
                    }
                    j = new Pinguino(nom, color, inv);
                }

                j.setId(jId);
                j.setPosicio(pos);
                j.setTornsBloquejat(tBloq);
                if (j instanceof Pinguino pingu) {
                    int vic = (dJ.get("VICTORIES") != null) ? Integer.parseInt(dJ.get("VICTORIES")) : 0;
                    pingu.setVictories(vic);
                }

                llistaJugadors.add(j);

                if (dJ.get("ES_GUANYADOR") != null && dJ.get("ES_GUANYADOR").equals("1")) {
                    guanyador = j;
                }
            }

            // 2.5 Ordenar els jugadors per color per garantir que coincideixin amb els slots visual (P1=Rojo, P2=Azul, P3=Verde, P4=Amarillo)
            String[] colorPool = {"Rojo", "Azul", "Verde", "Amarillo"};
            java.util.List<String> colorOrder = java.util.Arrays.asList(colorPool);
            llistaJugadors.sort((j1, j2) -> {
                int idx1 = colorOrder.indexOf(j1.getColor());
                int idx2 = colorOrder.indexOf(j2.getColor());
                // Si un color no está en el pool (inesperado), lo mandamos al final
                return Integer.compare(idx1 != -1 ? idx1 : 99, idx2 != -1 ? idx2 : 99);
            });

            // 3. Reconstruir l'objecte Partida
            GestorTaulell gt = new GestorTaulell();
            Partida partida = new Partida(gt.generarTaulell(seed), llistaJugadors);
            partida.setId(id);
            partida.setTorns(tornActual);
            partida.setFinalitzada(finalitzada);
            partida.setGuanyador(guanyador);

            System.out.println("Partida con ID " + id + " cargada correctamente.");
            return partida;

        } catch (Exception e) {
            System.out.println("Error en carregarBBDD: " + e.getMessage());
            return null;
        }
    }

    public ArrayList<String> llistarPartides(Connection con) {
        ArrayList<String> llista = new ArrayList<>();
        // Query que obté la partida i concatena els noms dels jugadors humans associats
        // Oracle LISTAGG concatena els strings de les files
        String sql = "SELECT p.id, p.torn_actual, p.finalitzada, "
                + "(SELECT LISTAGG(j.nom, ', ') WITHIN GROUP (ORDER BY j.nom) "
                + " FROM jugador_partida jp "
                + " JOIN jugador j ON jp.jugador_id = j.id "
                + " WHERE jp.partida_id = p.id AND j.es_cpu = 0) AS JUGADORS "
                + "FROM partida p ORDER BY p.id DESC";

        ArrayList<LinkedHashMap<String, String>> res = select(con, sql);
        for (LinkedHashMap<String, String> row : res) {
            String id = row.get("ID");
            String torn = row.get("TORN_ACTUAL");
            String jugadors = row.get("JUGADORS") != null ? row.get("JUGADORS") : "SENSE JUGADORS";
            String fin = "1".equals(row.get("FINALITZADA")) ? "Finalizada" : "En curso";
            llista.add("ID: " + id + " | " + jugadors + " | Turno: " + torn + " | " + fin);
        }
        return llista;
    }

    public boolean esborrarPartida(int id, Connection con) {
        try {
            // Esborrar dependències primer si no hi ha ON DELETE CASCADE configurat
            delete(con, "DELETE FROM jugador_partida WHERE partida_id = " + id);
            delete(con, "DELETE FROM inventari WHERE partida_id = " + id);
            delete(con, "DELETE FROM taulell WHERE partida_id = " + id);

            // Esborrar la partida
            int rows = delete(con, "DELETE FROM partida WHERE id = " + id);

            // Esborrar la partida i les seves relacions directes.
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Error esborrant partida: " + e.getMessage());
            return false;
        }
    }

    public boolean validarLogin(String username, String password, Connection con) {
        // Al ser una columna RAW, debemos convertirla a VARCHAR2 en la consulta para comparar el texto
        String sql = "SELECT UTL_RAW.CAST_TO_VARCHAR2(contrasenya) AS CONTRASENYA FROM jugador WHERE nom = '" + username + "'";
        ArrayList<LinkedHashMap<String, String>> result = select(con, sql);

        if (!result.isEmpty()) {
            String dbPassword = result.get(0).get("CONTRASENYA");
            // Comparamos el texto recuperado con la contraseña introducida
            if (dbPassword != null && dbPassword.equals(password)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true; // Si no existe, permitimos el paso (se creará al guardar)
        }
    }

    /**
     * Obtiene el ránking de jugadores basándose en el total de partidas jugadas.
     * 
     * FUNCIONAMIENTO:
     * 1. Llama al procedimiento PRC_RANKING_PARTIDAS para validar (si el jugador existe y tiene partidas).
     * 2. Si el procedimiento lanza un error (RAISE_APPLICATION_ERROR), se captura y se muestra el mensaje.
     * 3. Si la validación pasa, se ejecuta una consulta SQL estándar para obtener el conteo de partidas.
     * 4. Se eliminan los cursores complejos (SYS_REFCURSOR) para simplificar la conexión.
     */
    public ArrayList<String> obtenerRanking(Connection con) {
        return obtenerRanking("", con);
    }

    public ArrayList<String> obtenerRanking(String buscador, Connection con) {
        ArrayList<String> ranking = new ArrayList<>();
        if (con == null) return ranking;

        if (buscador == null) buscador = "";

        try {
            // 1. VALIDACIÓN mediante procedimiento (Sin cursores de salida)
            // Solo validamos si hay un buscador (nombre de jugador) especificado
            if (!buscador.isEmpty()) {
                try (CallableStatement cs = con.prepareCall("{ call PRC_RANKING_PARTIDAS(?) }")) {
                    cs.setString(1, buscador);
                    cs.execute();
                } catch (SQLException e) {
                    // Capturamos el error de validación del procedimiento (ej. "Jugador no existe")
                    ranking.add("ERROR: " + e.getMessage());
                    return ranking;
                }
            }

            // 2. OBTENCIÓN DE DATOS mediante consulta estándar
            // Obtenemos el ranking completo con nombre, victorias y total de partidas.
            String sql = "SELECT j.nom, j.victories, COUNT(jp.partida_id) AS TOTAL_PARTIDAS " +
                         "FROM jugador j " +
                         "JOIN jugador_partida jp ON j.id = jp.jugador_id " +
                         "WHERE j.es_cpu = 0 " +
                         "GROUP BY j.nom, j.victories " +
                         "ORDER BY TOTAL_PARTIDAS DESC";

            ArrayList<LinkedHashMap<String, String>> players = select(con, sql);

            // 3. Procesar resultados y filtrar por el buscador si es necesario
            int visualPos = 1;
            for (LinkedHashMap<String, String> row : players) {
                String nom = row.get("NOM");
                int total = Integer.parseInt(row.get("TOTAL_PARTIDAS"));
                int vics = (row.get("VICTORIES") != null) ? Integer.parseInt(row.get("VICTORIES")) : 0;

                // 4. Obtener el porcentaje de superación usando la función de la BD (F_PERCENTATGE_MENYS_VICTORIES)
                double pctSuperacion = 0;
                try (CallableStatement csPct = con.prepareCall("{ ? = call F_PERCENTATGE_MENYS_VICTORIES(?) }")) {
                    csPct.registerOutParameter(1, Types.DOUBLE);
                    csPct.setInt(2, vics);
                    csPct.execute();
                    pctSuperacion = csPct.getDouble(1);
                } catch (SQLException ex) {
                    System.err.println("Error calculando porcentaje para " + nom + ": " + ex.getMessage());
                }

                if (!buscador.isEmpty() && !nom.toLowerCase().contains(buscador.toLowerCase())) {
                    continue;
                }

                // Formato en bloque (2 líneas por jugador):
                // El CellFactory de PantallaMenu usará el \n para separar el título de las stats
                String bloqueInfo = visualPos + ". " + nom + " - Partidas jugadas: " + total + 
                                   "\nVictorias: " + vics + " (supera al " + String.format("%.1f", pctSuperacion) + "%)";
                
                ranking.add(bloqueInfo);
                visualPos++;
            }

            if (ranking.isEmpty() && !buscador.isEmpty()) {
                ranking.add("No se encontraron resultados para: " + buscador);
            }

        } catch (Exception e) {
            System.err.println("Error procesando ranking: " + e.getMessage());
            ranking.add("Error al cargar el ranking.");
        }
        
        return ranking;
    }
}

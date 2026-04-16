import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;
import java.io.FileInputStream;

public class ResetVictories {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
        String user = "DW2526_GR10_PINGU";
        String pass = "ACBPBIK";

        System.out.println("Connecting to database...");
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connected. Executing reset...");
            int rows = stmt.executeUpdate("UPDATE jugador SET victories = 0");
            System.out.println("Success! Rows affected: " + rows);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

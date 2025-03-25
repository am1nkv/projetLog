package DB;

import java.sql.*;

public class Bd {
    private static final String BD = "sqlite:DB";

    // Méthode pour établir une connexion
    public static Connection connect() {
        Connection conn = null;
        try {
            // Charger le driver SQLite JDBC (nécessaire seulement pour certaines versions de Java)
            Class.forName("org.sqlite.JDBC");

            // Établir la connexion
            conn = DriverManager.getConnection("jdbc:sqlite:DB.sqlite");
            System.out.println("Connexion réussie à SQLite.");
        } catch (ClassNotFoundException e) {
            System.out.println("Erreur : Driver SQLite JDBC introuvable.");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
        return conn;
    }


    // Méthode pour ajouter une réservation
    public static void reservation(int numAbo, int numDoc) {
        String checkQuery = "SELECT * FROM Reservation WHERE idabo = ? AND iddoc = ?";
        String insertQuery = "INSERT INTO Reservation (idabo, iddoc) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, numAbo);
            checkStmt.setInt(2, numDoc);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) { // Vérifier si la réservation existe
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, numAbo);
                    insertStmt.setInt(2, numDoc);
                    insertStmt.executeUpdate();
                    System.out.println("Réservation ajoutée avec succès.");
                }
            } else {
                System.out.println("Réservation déjà existante.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la réservation : " + e.getMessage());
        }
    }
}

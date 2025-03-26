package DB;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        String checkReservationQuery = "SELECT * FROM Reservation WHERE idabo = ? AND iddoc = ?"; // Vérifier si la réservation existe déjà
        String checkEmpruntQuery = "SELECT disponible FROM Document WHERE id_doc = ?"; // Vérifier si le document est disponible
        String insertQuery = "INSERT INTO Reservation (idabo, iddoc, date_resa) VALUES (?, ? , ?)"; // Ajouter la réservation

        try (Connection conn = connect();
             PreparedStatement checkReservationStmt = conn.prepareStatement(checkReservationQuery)) {

            // Vérifier si la réservation existe déjà
            checkReservationStmt.setInt(1, numAbo);
            checkReservationStmt.setInt(2, numDoc);
            ResultSet reservationRs = checkReservationStmt.executeQuery();

            if (!reservationRs.next()) { // Si la réservation n'existe pas déjà
                try (PreparedStatement checkEmpruntStmt = conn.prepareStatement(checkEmpruntQuery)) {

                    //expiration
                    LocalDateTime expirationTime = LocalDateTime.now().plusSeconds(10);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String expirationStr = expirationTime.format(formatter);

                    // Vérifier si le document est emprunté (disponible = 0)
                    checkEmpruntStmt.setInt(1, numDoc);
                    ResultSet empruntRs = checkEmpruntStmt.executeQuery();

                    if (empruntRs.next()) {
                        int disponible = empruntRs.getInt("disponible");

                        if (disponible == 0) { // Si le document n'est pas disponible (emprunté)
                            System.out.println("Le document est déjà emprunté.");
                        } else {
                            // Si le document est disponible, procéder à la réservation
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                                insertStmt.setInt(1, numAbo);
                                insertStmt.setInt(2, numDoc);
                                //ajout date expiration
                                insertStmt.setString(3, expirationStr);
                                insertStmt.executeUpdate();
                                System.out.println("Réservation ajoutée avec succès de " + LocalDateTime.now() +  " jusqu'à " + expirationStr);
                                checkReservation(numAbo,numDoc);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Réservation déjà existante.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la réservation : " + e.getMessage());
        }
    }


    public static void emprunt(int numAbo, int numDoc) {
        String checkEmpruntQuery = "SELECT disponible FROM Document WHERE id_doc = ?"; // Vérifier si le document est disponible
        String updateEmpruntQuery = "UPDATE Document SET disponible = 0 WHERE id_doc = ?"; // Mettre à jour le document en emprunté
        String insertEmpruntQuery = "INSERT INTO Emprunt (idabo, iddoc) VALUES (?, ?)"; // Ajouter l'emprunt

        try (Connection conn = connect();
             PreparedStatement checkEmpruntStmt = conn.prepareStatement(checkEmpruntQuery)) {

            // Vérifier si le document est déjà emprunté
            checkEmpruntStmt.setInt(1, numDoc);
            ResultSet empruntRs = checkEmpruntStmt.executeQuery();

            if (empruntRs.next()) {
                int disponible = empruntRs.getInt("disponible");

                if (disponible == 0) { // Si le document est déjà emprunté
                    System.out.println("Le document est déjà emprunté, il n'est pas disponible.");
                } else {
                    // Si le document est disponible, procéder à l'emprunt
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateEmpruntQuery);
                         PreparedStatement insertStmt = conn.prepareStatement(insertEmpruntQuery)) {

                        // Mettre à jour l'état du document pour le marquer comme emprunté
                        updateStmt.setInt(1, numDoc);
                        updateStmt.executeUpdate();

                        // Ajouter l'emprunt dans la table Emprunt
                        insertStmt.setInt(1, numAbo);
                        insertStmt.setInt(2, numDoc);
                        insertStmt.executeUpdate();

                        System.out.println("Emprunt effectué avec succès.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'emprunt : " + e.getMessage());
        }}

    public static void retour(int numAbo, int numDoc) {
        String checkQuery = "SELECT * FROM Emprunt WHERE idabo = ? AND iddoc = ?";
        String deleteQuery = "DELETE FROM Emprunt WHERE idabo = ? AND iddoc = ?";
        String updateQuery = "UPDATE Document SET disponible = 1 WHERE id_doc = ?";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, numAbo);
            checkStmt.setInt(2, numDoc);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) { // Vérifier si l'emprunt existe
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                     PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

                    //Supprimer l'entrée d'emprunt
                    deleteStmt.setInt(1, numAbo);
                    deleteStmt.setInt(2, numDoc);
                    deleteStmt.executeUpdate();

                    // Mettre à jour la disponibilité du document
                    updateStmt.setInt(1, numDoc);
                    updateStmt.executeUpdate();

                    System.out.println("Retour effectué avec succès.");
                }
            } else {
                System.out.println("Aucun emprunt trouvé pour ce document.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du retour du document : " + e.getMessage());
        }
    }

    public static void checkReservation(int numAbo, int numDoc) throws SQLException {
        String checkQuery = "SELECT date_resa FROM Reservation WHERE idabo = ? AND iddoc = ?";
        try(Connection conn = connect();
            PreparedStatement p = conn.prepareStatement(checkQuery)){

            p.setInt(1, numAbo);
            p.setInt(2, numDoc);
            ResultSet rs = p.executeQuery();

            if (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("date_resa");
                LocalDateTime dateResa = timestamp.toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();

                if (dateResa.isBefore(now)) {
                    System.out.println("La réservation a expiré.");
                } else {
                    System.out.println("La réservation est encore valide.");
                }
            }
        }
    }

}

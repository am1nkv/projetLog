package DB;

import Documents.Abonne;
import Documents.DVD;
import Documents.IDocument;
import Documents.Livres;
import org.w3c.dom.Document;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Bd {
    private static final String BD = "sqlite:DB";
    private HashMap<Integer, IDocument> documentHashMap;
    private HashMap<Integer, Abonne> abonneHashMap;
    private HashMap<Integer, Integer> emprunt;
    private HashMap<Integer, Integer> reservation;

    public Bd() {
        documentHashMap = new HashMap<>();
        abonneHashMap = new HashMap<>();
        emprunt = new HashMap<>();
        reservation = new HashMap<>();
        recupAbo();
        recupDoc();
    }

    public synchronized HashMap<Integer, IDocument> getDocuments() {
        return documentHashMap;
    }

    public synchronized HashMap<Integer, Abonne> getAbonnes() {
        return abonneHashMap;
    }

    public synchronized HashMap<Integer, Integer> getEmprunt() {
        return emprunt;
    }

    public synchronized HashMap<Integer, Integer> getReservation() {
        return reservation;
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:DB.sqlite");
            System.out.println("Connexion reussie à SQLite.");
        } catch (ClassNotFoundException e) {
            System.out.println("Erreur : Driver SQLite JDBC introuvable.");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
        return conn;
    }

    public void recupAbo() {
        try (Connection conn = connect();) {

            String query = "SELECT * FROM abonne";
            PreparedStatement stmt = conn.prepareStatement(query);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id_abo = rs.getInt("id_abo");
                String dateStr = rs.getString("date_de_naissance");
                LocalDate nddn = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                String nom = rs.getString("nom_abo");

                Abonne abonne = new Abonne(id_abo, nom, nddn);
                getAbonnes().put(id_abo, abonne);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recupDoc() {
        try (Connection conn = connect();) {
            String queryd = "SELECT * FROM Document";
            String queryr = "SELECT * FROM Reservation";
            String querye = "SELECT * FROM Emprunt";
            PreparedStatement stmt = conn.prepareStatement(queryd);
            PreparedStatement stmtr = conn.prepareStatement(queryr);
            PreparedStatement stmte = conn.prepareStatement(querye);
            ResultSet rs = stmt.executeQuery();
            ResultSet rsr = stmtr.executeQuery();
            ResultSet rse = stmte.executeQuery();

            while (rs.next()) {
                int id_doc = rs.getInt("id_doc");
                String nom_doc = rs.getString("nom_doc");
                String disponible = rs.getString("disponible");
                int nbPage = rs.getInt("nb_pages");
                boolean adulte = rs.getBoolean("adulte");
                int reserveurNumero = rsr.getInt("idabo");
                int emprunteurNumero = rse.getInt("idabo");

                IDocument document = null;

                if (reserveurNumero != -1) {
                    getReservation().put(reserveurNumero, id_doc);
                }
                if (emprunteurNumero != -1) {
                    getEmprunt().put(emprunteurNumero, id_doc);
                }
                if (disponible.equals("livre")) {
                    document = new Livres(nom_doc, id_doc, nbPage);
                } else if (disponible.equals("dvd")) {
                    document = new DVD(id_doc, adulte, nom_doc);
                }
                getDocuments().put(id_doc, document);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public void suppReservations(int numAbo, int numDoc) {
        try (Connection conn = connect()) {
            String deleteQuery = "DELETE FROM reservation WHERE idabo = ? AND iddoc = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteQuery);
            stmt.setInt(1, numAbo);
            stmt.setInt(2, numDoc);
            //int rows = stmt.executeUpdate();
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int tempsRestant(int numDoc) {
        String selectQuery = "SELECT date_fin_resa, date_resa FROM reservation WHERE iddoc = ?";
        LocalDateTime currentDateTime = LocalDateTime.now();

        try (Connection connection = connect();
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {

            selectStmt.setInt(1, numDoc);
            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                LocalDateTime dateFinReservation = resultSet.getTimestamp("date_fin_resa").toLocalDateTime();
                LocalDateTime dateReservation = resultSet.getTimestamp("date_resa").toLocalDateTime();

                if (dateFinReservation.isAfter(currentDateTime)) {
                    Duration duration = Duration.between(currentDateTime, dateFinReservation);
                    long secondsRestants = duration.getSeconds();
                    return (int) secondsRestants;
                } else {
                    return 0;
                }
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String tempsRestantHeure(int numDoc) {
        String selectQuery = "SELECT date_fin_resa FROM reservation WHERE iddoc = ?";
        LocalDateTime currentDateTime = LocalDateTime.now();

        try (Connection connection = connect();
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {

            selectStmt.setInt(1, numDoc);
            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                LocalDateTime dateFinReservation = resultSet.getTimestamp("date_fin_resa").toLocalDateTime();

                if (dateFinReservation.isAfter(currentDateTime)) {
                    Duration duration = Duration.between(currentDateTime, dateFinReservation);

                    long heures = duration.toHours();
                    long minutes = duration.toMinutesPart();
                    long secondes = duration.toSecondsPart();

                    return String.format("%02d:%02d:%02d", heures, minutes, secondes);
                } else {
                    return "00:00:00";
                }
            } else {
                return "-1";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
    }

    public boolean getAdulte(int num_doc) {
        String Selectquery = "SELECT adulte FROM Document WHERE id_doc = ?";
        try (Connection connection = connect();
             PreparedStatement selectStmt = connection.prepareStatement(Selectquery)) {

            selectStmt.setInt(1, num_doc);
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                boolean ad = rs.getBoolean("adulte");
                return ad;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

//        public void addAlerte(int numAbo, int numDoc) {
//            String insertQuery = "INSERT INTO alerte (id_abo, id_doc) VALUES (?, ?)";
//            try(Connection conn = connect();
//                PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
//
//                stmt.setInt(1, numAbo);
//                stmt.setInt(2, numDoc);
//                //int rowsAffected = stmt.executeUpdate();
//                stmt.executeUpdate();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }


//        public void checkReservation() {
//            String selectQuery = "SELECT * FROM reservation";
//            String deleteQuery = "DELETE FROM reservation WHERE id_abo = ? AND id_doc = ?";
//            LocalDateTime currentDateTime = LocalDateTime.now();
//
//            try(Connection connection = connect();
//                PreparedStatement selectStmt = connection.prepareStatement(selectQuery);) {
//
//                ResultSet resultSet = selectStmt.executeQuery();
//
//                while (resultSet.next()) {
//                    int numAbo = resultSet.getInt("id_abo");
//                    int numDoc = resultSet.getInt("id_doc");
//                    LocalDateTime dateFinReservation = resultSet.getTimestamp("date_fin_resa").toLocalDateTime();
//
//                    if (dateFinReservation.isBefore(currentDateTime)) {
//                        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
//                            deleteStmt.setInt(1, numAbo);
//                            deleteStmt.setInt(2, numDoc);
//                            deleteStmt.executeUpdate();
//                            documentHashMap.get(numDoc).retourner();
//                            System.out.println("La réservation pour l'abonné " + numAbo + " et le document " + numDoc + " a été supprimée car elle a expiré.");
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

//    // Méthode pour ajouter une réservation
//    public static void reservation(int numAbo, int numDoc) {
//        String checkReservationQuery = "SELECT * FROM Reservation WHERE  iddoc = ?"; // Vérifier si la réservation existe déjà
//        String checkEmpruntQuery = "SELECT disponible FROM Document WHERE id_doc = ?"; // Vérifier si le document est disponible
//        String insertQuery = "INSERT INTO Reservation (idabo, iddoc, date_resa) VALUES (?, ? , ?)"; // Ajouter la réservation
//
//        try (Connection conn = connect();
//             PreparedStatement checkReservationStmt = conn.prepareStatement(checkReservationQuery)) {
//
//            // Vérifier si la réservation existe déjà
//
//            checkReservationStmt.setInt(1, numDoc);
//            ResultSet reservationRs = checkReservationStmt.executeQuery();
//
//            if (!reservationRs.next()) { // Si la réservation n'existe pas déjà
//                try (PreparedStatement checkEmpruntStmt = conn.prepareStatement(checkEmpruntQuery)) {
//
//                    //expiration
//                    LocalDateTime expirationTime = LocalDateTime.now().plusSeconds(60);
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                    String expirationStr = expirationTime.format(formatter);
//
//                    // Vérifier si le document est emprunté (disponible = 0)
//                    checkEmpruntStmt.setInt(1, numDoc);
//                    ResultSet empruntRs = checkEmpruntStmt.executeQuery();
//
//                    if (empruntRs.next()) {
//                        int disponible = empruntRs.getInt("disponible");
//
//                        if (disponible == 0) { // Si le document n'est pas disponible (emprunté)
//                            System.out.println("Le document est déjà emprunter");
//                        } else {
//                            // Si le document est disponible, procéder à la réservation
//                            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
//                                insertStmt.setInt(1, numAbo);
//                                insertStmt.setInt(2, numDoc);
//                                //ajout date expiration
//                                insertStmt.setString(3, expirationStr);
//                                insertStmt.executeUpdate();
//                                System.out.println("Réservation ajouter avec succes  de"+LocalDateTime.now()+"A" + expirationStr);
//                                checkReservation(numAbo,numDoc);
//                            }
//                        }
//                    }
//                }
//            } else {
//                System.out.println("Reservation deja existante.");
//            }
//        } catch (SQLException e) {
//            System.out.println("Erreur lors de la reservation : " + e.getMessage());
//        }
//    }
//
//
//    public static void emprunt(int numAbo, int numDoc) {
//        String checkEmpruntQuery = "SELECT disponible FROM Document WHERE id_doc = ?"; // Vérifier si le document est disponible
//        String updateEmpruntQuery = "UPDATE Document SET disponible = 0 WHERE id_doc = ?"; // Mettre à jour le document en emprunté
//        String insertEmpruntQuery = "INSERT INTO Emprunt (idabo, iddoc) VALUES (?, ?)"; // Ajouter l'emprunt
//
//        try (Connection conn = connect();
//             PreparedStatement checkEmpruntStmt = conn.prepareStatement(checkEmpruntQuery)) {
//
//            // Vérifier si le document est déjà emprunté
//            checkEmpruntStmt.setInt(1, numDoc);
//            ResultSet empruntRs = checkEmpruntStmt.executeQuery();
//
//            if (empruntRs.next()) {
//                int disponible = empruntRs.getInt("disponible");
//
//                if (disponible == 0) { // Si le document est déjà emprunté
//                    System.out.println("Le document est deja emprunter, il n'est pas disponible.");
//                } else {
//                    // Si le document est disponible, procéder à l'emprunt
//                    try (PreparedStatement updateStmt = conn.prepareStatement(updateEmpruntQuery);
//                         PreparedStatement insertStmt = conn.prepareStatement(insertEmpruntQuery)) {
//
//                        // Mettre à jour l'état du document pour le marquer comme emprunté
//                        updateStmt.setInt(1, numDoc);
//                        updateStmt.executeUpdate();
//
//                        // Ajouter l'emprunt dans la table Emprunt
//                        insertStmt.setInt(1, numAbo);
//                        insertStmt.setInt(2, numDoc);
//                        insertStmt.executeUpdate();
//
//                        System.out.println("Emprunt effectuer avec succes.");
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            System.out.println("Erreur lors de l'emprunt : " + e.getMessage());
//        }}
//
//    public static void retour(int numAbo, int numDoc) {
//        String checkQuery = "SELECT * FROM Emprunt WHERE idabo = ? AND iddoc = ?";
//        String deleteQuery = "DELETE FROM Emprunt WHERE idabo = ? AND iddoc = ?";
//        String updateQuery = "UPDATE Document SET disponible = 1 WHERE id_doc = ?";
//
//        try (Connection conn = connect();
//             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
//
//            checkStmt.setInt(1, numAbo);
//            checkStmt.setInt(2, numDoc);
//            ResultSet rs = checkStmt.executeQuery();
//
//            if (rs.next()) { // Vérifier si l'emprunt existe
//                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
//                     PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
//
//                    //Supprimer l'entrée d'emprunt
//                    deleteStmt.setInt(1, numAbo);
//                    deleteStmt.setInt(2, numDoc);
//                    deleteStmt.executeUpdate();
//
//                    // Mettre à jour la disponibilité du document
//                    updateStmt.setInt(1, numDoc);
//                    updateStmt.executeUpdate();
//
//                    System.out.println("Retour effectué avec succes.");
//                }
//            } else {
//                System.out.println("Aucun emprunt trouvé pour ce document.");
//            }
//        } catch (SQLException e) {
//            System.out.println("Erreur lors du retour du document : " + e.getMessage());
//        }
//    }
//
//    public  static void checkReservation(int numAbo, int numDoc) throws SQLException {
//        String deleteExpiredQuery = "DELETE FROM Reservation WHERE date_resa <= datetime('now')";
//        String checkQuery = "SELECT date_resa FROM Reservation WHERE idabo = ? AND iddoc = ?";
//
//        try (Connection conn = connect();
//             PreparedStatement deleteStmt = conn.prepareStatement(deleteExpiredQuery);
//             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
//
//            // Supprimer les réservations expirées
//            deleteStmt.executeUpdate();
//
//            // Vérifier si la réservation existe encore
//            checkStmt.setInt(1, numAbo);
//            checkStmt.setInt(2, numDoc);
//            ResultSet rs = checkStmt.executeQuery();
//
//            if (rs.next()) {
//                Timestamp timestamp = rs.getTimestamp("date_resa");
//                LocalDateTime dateResa = timestamp.toLocalDateTime();
//                LocalDateTime now = LocalDateTime.now();
//
//                if (dateResa.isBefore(now)) {
//                    System.out.println("La reservation a expirer");
//                } else {
//                    System.out.println("La reservation est encore valide.");
//                }
//            } else {
//                System.out.println("Aucune réservation trouvée (elle a peut-etre expirer).");
//            }
//        }
//    }
}

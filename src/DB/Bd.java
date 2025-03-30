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

public  class Bd {

    private HashMap<Integer, IDocument> documentHashMap;
    private HashMap<Integer, Abonne> abonneHashMap;
    private HashMap<Integer, Integer> emprunt;
    private HashMap<Integer, Integer> reservation;

    public  Bd() {
        synchronized (this){
            documentHashMap = new HashMap<>();
            abonneHashMap = new HashMap<>();
            emprunt = new HashMap<>();
            reservation = new HashMap<>();

            recupAbo();
            recupDoc();
            checkReservation();}
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
            conn = DriverManager.getConnection( "jdbc:sqlite:DB.sqlite?journal_mode=WAL&busy_timeout=30000");

        } catch (ClassNotFoundException e) {
            System.out.println("Erreur : Driver SQLite JDBC introuvable.");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
       ;
        return conn;
    }

    public synchronized void recupAbo() {
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

    public synchronized void recupDoc() {

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

            // Réinitialisation des listes pour éviter les doublons et assurer la cohérence
            getDocuments().clear();
            getReservation().clear();
            getEmprunt().clear();

            while (rs.next()) {
                int id_doc = rs.getInt("id_doc");
                String nom_doc = rs.getString("nom_doc");
                String disponible = rs.getString("disponible");
                String type = rs.getString("type_doc");
                int nbPage = rs.getInt("nb_pages");
                boolean adulte = rs.getBoolean("adulte");

                IDocument document = null;

                if (type.equals("Livre")) {
                    document = new Livres(nom_doc, id_doc, nbPage);
                } else if (type.equals("dvd")) {
                    document = new DVD(id_doc, adulte, nom_doc);
                }

                getDocuments().put(id_doc, document);
            }

            while (rsr.next()) {
                int reserveurNumero = rsr.getInt("idabo");
                int doc = rsr.getInt("iddoc");
                getReservation().put(reserveurNumero, doc);
            }

            while (rse.next()) {
                int emprunteurNumero = rse.getInt("idabo");
                int docu = rse.getInt("iddoc");
                getEmprunt().put(emprunteurNumero, docu);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void suppReservations(int numAbo, int numDoc) {
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

    public synchronized String tempsRestantHeure(int numDoc) {
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

    public void addAlerte(int numAbo, int numDoc) {
        try(Connection conn = connect()) {
            String insertQuery = "INSERT INTO alerte (idabo, iddoc) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            stmt.setInt(1, numAbo);
            stmt.setInt(2, numDoc);
            int rowsAffected = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public synchronized void checkReservation() {
        String selectQuery = "SELECT * FROM reservation";
        String deleteQuery = "DELETE FROM reservation WHERE idabo = ? AND iddoc = ?";
        LocalDateTime currentDateTime = LocalDateTime.now();

        try(Connection connection = connect();
            PreparedStatement selectStmt = connection.prepareStatement(selectQuery);) {

            ResultSet resultSet = selectStmt.executeQuery();

            while (resultSet.next()) {
                int numAbo = resultSet.getInt("idabo");
                int numDoc = resultSet.getInt("iddoc");
                LocalDateTime dateFinReservation = resultSet.getTimestamp("date_fin_resa").toLocalDateTime();

                if (dateFinReservation.isBefore(currentDateTime)) {
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                        deleteStmt.setInt(1, numAbo);
                        deleteStmt.setInt(2, numDoc);
                        deleteStmt.executeUpdate();
                        synchronized (documentHashMap.get(numDoc)) {
                            documentHashMap.get(numDoc).retourner(getAbonnes().get(numAbo));
                        }
                        System.out.println("La réservation pour l'abonné " + numAbo + " et le document " + numDoc + " a été supprimée car elle a expiré.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }}

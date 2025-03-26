package Documents;

import java.sql.*;
import java.time.LocalDateTime;

import static DB.Bd.connect;

public class Livres implements IDocument {
    private final String title;
    private final int numero;
    private final int nb_pages;
    private Connection connection;
    public Livres(String title, int numero, int nb_pages) {
        this.title = title;
        this.numero = numero;
        this.nb_pages = nb_pages;

        try {

            connection = connect();
        } catch(Exception e){}
    }

    @Override
    public int numero() {
        return numero;
    }


    public int emprunteur(Abonne ab){
        String query = "SELECT idabo FROM EMPRUNT WHERE iddoc = ?";
        PreparedStatement stmt = null;
        int r = -1;
        try{
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, numero());

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    r = rs.getInt("id_abo");
                }
            }
        }catch(SQLException e){
            throw new RuntimeException("Erreur SQL : " + e.getMessage(), e);
        }
        return r;
    }

    public int reserveur(Abonne ab) {
        String query = "SELECT idabo FROM reservation WHERE iddoc = ?";
        int r = -1; // Valeur par défaut si aucun résultat trouvé

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, numero());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { // Vérifie si un résultat existe
                    r = rs.getInt("idabo");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur SQL : " + e.getMessage(), e);
        }

        return r;
    }

    @Override
    public void reserver(Abonne ab) throws ReservationException {
        String insertQuery = "INSERT INTO Reservation (idabo, iddoc, date_resa, date_fin_resa) VALUES (?, ?, ?, ?)";

        PreparedStatement insertStmt = null;

        try{

            Connection conn = connect();
            insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, ab.getNum());  // ID de l'abonné
            insertStmt.setInt(2, numero());  // ID du document

            LocalDateTime debut = LocalDateTime.now();
            LocalDateTime fin = debut.plusHours(1); // Expiration après 1 heure

            insertStmt.setTimestamp(3, Timestamp.valueOf(debut)); // Convertir en Timestamp SQL
            insertStmt.setTimestamp(4, Timestamp.valueOf(fin)); // Convertir en Timestamp SQL
            insertStmt.executeUpdate();
            //FINITO PIPO
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if(insertStmt != null) {
                    insertStmt.close();
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void emprunter(Abonne ab) throws EmpruntException {
        String updateEmpruntQuery = "UPDATE Document SET disponible = 0 WHERE id_doc = ?";
        String insertEmpruntQuery = "INSERT INTO Emprunt (idabo, iddoc) VALUES (?, ?)";
        String updateResaQuery = "DELETE FROM reservation WHERE numAbo = ? AND numDoc = ?";

        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updaterStmt = null;

        try
        {
            Connection conn = connect();
            updateStmt = conn.prepareStatement(updateEmpruntQuery);
            insertStmt = conn.prepareStatement(insertEmpruntQuery);
            updaterStmt = conn.prepareStatement(updateResaQuery);
            updateStmt.setInt(1, numero());
            updateStmt.executeUpdate();

            insertStmt.setInt(1, ab.getNum());
            insertStmt.setInt(2, numero());
            insertStmt.executeUpdate();


            if(this.reserveur(ab) == ab.getNum()) {
                updaterStmt.setInt(1, ab.getNum());
                updaterStmt.setInt(2, numero());
                updaterStmt.executeUpdate();
            }
            //EMPRUNT FINI MANQUE DATE CERTIF


        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if(insertStmt != null || updateStmt !=null ||updaterStmt!=null ) {
                    insertStmt.close();
                    updaterStmt.close();
                    updateStmt.close();
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void retourner() {

            // 1 SUPP DANS EMPRUNT 2 SUPP DANS RESA 3 UPP DISPO
            String deleteQuery = "DELETE FROM Emprunt WHERE iddoc = ?";
            String updateQuery = "UPDATE Document SET disponible = 1 WHERE id_doc = ?";


            PreparedStatement deleteStmt = null;
            PreparedStatement updateStmt =null;
            try {
                Connection conn = connect();
                deleteStmt = conn.prepareStatement(deleteQuery);
                updateStmt = conn.prepareStatement(updateQuery);
                deleteStmt.setInt(1,numero() );
                deleteStmt.executeUpdate();
                updateStmt.setInt(1, numero());
                updateStmt.executeUpdate();

            }catch (SQLException e) {
                e.printStackTrace();
            }finally {
                try {
                    if( updateStmt != null || deleteStmt != null) {
                        updateStmt.close();
                        deleteStmt.close();
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
            }
    }
}


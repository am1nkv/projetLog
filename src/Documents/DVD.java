package Documents;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Random;

import static DB.Bd.connect;



public class DVD implements IDocument {
    private int numero;
    private String titre;
    private boolean adulte;

//
//    private String from = "service.mediatheque@gmail.com";
//    private String to = "ineskadjii@gmail.com";
//
//    private String host = "smtp.gmail.com";
//    private String username = "service.mediatheque@gmail.com";
//    private String password = "wsuy rody kyby kvty";
    private Properties props;

    private Connection connection;

//    private Abonne emprunteur;
//    private Abonne reserveur;

    public DVD(int num, boolean a, String t) {
        numero = num;
        adulte = a;
        titre = t;
//        emprunteur = e;
//        reserveur = r;
        props = new Properties();

        try {
           // connection = DriverManager.getConnection(DB_URL, USER, PASS);
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
    public void reserver(Abonne ab) throws EmpruntException {
        String insertQuery = "INSERT INTO Reservation (idabo, iddoc, date_resa, date_fin_resa) VALUES (?, ?, ?, ?)";

        if (ab.getAge() < 16 && adulte) {
            throw new EmpruntException("Vous n’avez pas l’âge pour ce DVD.");
        }
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

//        else {
//  //          reserveur = ab;
//            PreparedStatement stmt = null;
//            try {
//                String query = "UPDATE Document SET reserveur = ? WHERE Numero = ?";
//                stmt = connection.prepareStatement(query);
//                stmt.setInt(1, ab.getNum());
//                stmt.setInt(2, numero());
//                stmt.executeUpdate();
//
//                String insertQuery = "INSERT INTO reservation (numAbo, numDoc, dateReservation, dateFinReservation) VALUES (?, ?, ?, ?)";
//                stmt = connection.prepareStatement(insertQuery);
//                stmt.setInt(1, ab.getNum());
//                stmt.setInt(2, numero());
//
//                LocalDateTime currentDateTime = LocalDateTime.now();
//                stmt.setObject(3, currentDateTime);
//
//                LocalDateTime futureDateTime = currentDateTime.plusHours(2);
//                stmt.setObject(4, futureDateTime);
//
//                stmt.executeUpdate();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (stmt != null) stmt.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }



    @Override
    public void emprunter(Abonne ab) throws EmpruntException {
        String updateEmpruntQuery = "UPDATE Document SET disponible = 0 WHERE id_doc = ?";
        String insertEmpruntQuery = "INSERT INTO Emprunt (idabo, iddoc) VALUES (?, ?)";
        String updateResaQuery = "DELETE FROM reservation WHERE numAbo = ? AND numDoc = ?";
        if(ab.getAge() < 18 && adulte) {
            throw new EmpruntException("Vous n'avez pas l'age requis.");
        }
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
//        else {
//
//            PreparedStatement stmt = null;
//            try {
//                String query = "UPDATE Document SET emprunteur = ? WHERE Numero = ?";
//                stmt = (PreparedStatement) connection.prepareStatement(query);
//                stmt.setInt(1, ab.getNum());
//                stmt.setInt(2, numero());
//                stmt.executeUpdate();
//
//                String insertQuery = "INSERT INTO emprunt (numAbo, numDoc, dateEmprunt, datePourRetour) VALUES (?, ?, ?, ?)";
//                stmt = (PreparedStatement) connection.prepareStatement(insertQuery);
//                stmt.setInt(1, ab.getNum());
//                stmt.setInt(2, numero());
//                LocalDateTime now = LocalDateTime.now();
//                LocalDateTime datePourRetour = now.plusMonths(1);
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                String strDateEmprunt = now.format(formatter);
//                String strDatePourRetour = datePourRetour.format(formatter);
//                stmt.setString(3, strDateEmprunt);
//                stmt.setString(4, strDatePourRetour);
//                stmt.executeUpdate();
//
//                if(reserveur == ab) {
//                    String updateQuery = "DELETE FROM reservation WHERE numAbo = ? AND numDoc = ?";
//                    stmt = (PreparedStatement) connection.prepareStatement(updateQuery);
//                    stmt.setInt(1, ab.getNum());
//                    stmt.setInt(2, numero());
//                    stmt.executeUpdate();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (stmt != null) stmt.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
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
   //FAIRE LES CEERTIF

    }




//        PreparedStatement stmt = null;
//        try {
//            String query = "UPDATE Document SET reserveur = NULL WHERE Numero = ?";
//            stmt = (PreparedStatement) connection.prepareStatement(query);
//            stmt.setInt(1, numero());
//            stmt.executeUpdate();
//
//            query = "UPDATE Document SET emprunteur = NULL WHERE Numero = ?";
//            stmt = (PreparedStatement) connection.prepareStatement(query);
//            stmt.setInt(1, numero());
//            stmt.executeUpdate();
//
//            String updateQuery = "DELETE FROM reservation WHERE numDoc = ?";
//            stmt = (PreparedStatement) connection.prepareStatement(updateQuery);
//            stmt.setInt(1, numero());
//            stmt.executeUpdate();
//
//            Abonne ab;
//            if (emprunteur() == null) {
//                ab = reserveur();
//            } else {
//                ab = emprunteur();
//            }
//
//            if(ab != null) {
//                if(degradé() || delais(ab.getNum()) ) {
//                    ab.startBan();
//                }
//            }
//
//            updateQuery = "DELETE FROM emprunt WHERE numDoc = ?";
//            stmt = (PreparedStatement) connection.prepareStatement(updateQuery);
//            stmt.setInt(1, numero());
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (stmt != null) stmt.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        mail();
//        emprunteur = null;
//        reserveur = null;



//    private boolean delais(int numAbo) {
//        boolean delaisDepasse = false;
//        try {
//            String selectQuery = "SELECT datePourRetour FROM emprunt WHERE numAbo = ?";
//            PreparedStatement stmt = (PreparedStatement) connection.prepareStatement(selectQuery);
//            stmt.setInt(1, numAbo);
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) {
//                LocalDateTime datePourRetour = rs.getObject("datePourRetour", LocalDateTime.class);
//                LocalDateTime now = LocalDateTime.now();
//                if (now.isAfter(datePourRetour)) {
//                    delaisDepasse = true;
//                }
//            }
//            rs.close();
//            stmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return delaisDepasse;
//    }
//
//    private Boolean degradé() {
//        Random random = new Random();
//        int randomNumber = random.nextInt(100);
//        return randomNumber <= 19;
//    }

//    private void mail() {
//        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
//            String query = "SELECT * FROM alerte WHERE numDoc = ?";
//            PreparedStatement stmt = (PreparedStatement) conn.prepareStatement(query);
//            stmt.setInt(1, numero());
//            ResultSet rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                props.put("mail.smtp.auth", "true");
//                props.put("mail.smtp.starttls.enable", "true");
//                props.put("mail.smtp.host", host);
//                props.put("mail.smtp.port", "587");
//
//                Session session = Session.getInstance(props,
//                        new javax.mail.Authenticator() {
//                            protected PasswordAuthentication getPasswordAuthentication() {
//                                return new PasswordAuthentication(username, password);
//                            }
//                        });
//                try {
//                    Message message = new MimeMessage(session);
//                    message.setFrom(new InternetAddress(from));
//                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
//                    message.setSubject("Votre DVD est de retour !");
//                    message.setText("Bonjour,\n\nLe DVD " + this.titre + " est de retour ! Vous pouvez désormais aller le réserver ou directement l'emprunter"
//                            + ".\n\nCordialement, l'équipe de la médiathèque Ça Croust'ill.\n\nTél : 01 76 53 47 00\nMail : service.mediatheque@gmail.com\nAdresse : 143 Avenue de Versailles, 75016 Paris");
//                    Transport.send(message);
//
//                    String deleteQuery = "DELETE FROM alerte WHERE numDoc = ?";
//                    PreparedStatement deleteStmt = (PreparedStatement) conn.prepareStatement(deleteQuery);
//                    deleteStmt.setInt(1, numero());
//                    deleteStmt.executeUpdate();
//                } catch (MessagingException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }



}

package Documents;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import static DB.Bd.connect;

public class Livres implements IDocument {
    private final String title;
    private final int numero;
    private final int nb_pages;
    private String from = "tribu.mediatheque@gmail.com";
    private String pour = "alyaayinde@gmail.com";
    //
    private String host = "smtp.gmail.com";
    private String username = "service.mediatheque@gmail.com";
    private String password = "chamanbullgeromino";
    private Properties props;
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

    public int emprunteur() {
        String query = "SELECT idabo FROM EMPRUNT WHERE iddoc = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, numero());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idabo");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur SQL : " + e.getMessage(), e);
        }
        return -1;
    }
    public int reserveur() {
        String query = "SELECT idabo FROM reservation WHERE iddoc = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, numero());
            ResultSet rs = stmt.executeQuery();

            return  rs.getInt("idabo");

        } catch (SQLException e) {
            throw new RuntimeException("Erreur SQL : " + e.getMessage(), e);
        }
    }

    @Override
    public void reserver(Abonne ab) throws ReservationException {
        String insertQuery = "INSERT INTO Reservation (idabo, iddoc, date_resa, date_fin_resa) VALUES (?, ?, ?, ?)";

        PreparedStatement insertStmt = null;

        try{


            insertStmt = connection.prepareStatement(insertQuery);
            insertStmt.setInt(1, ab.getNum());
            insertStmt.setInt(2, numero());

            LocalDateTime debut = LocalDateTime.now();
            LocalDateTime fin = debut.plusHours(1); // Expiration après 1 heure

            insertStmt.setTimestamp(3, Timestamp.valueOf(debut));
            insertStmt.setTimestamp(4, Timestamp.valueOf(fin));
            insertStmt.executeUpdate();

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
        String insertEmpruntQuery = "INSERT INTO Emprunt (idabo, iddoc,date_retour) VALUES (?, ?,?)";
        String updateResaQuery = "DELETE FROM reservation WHERE idabo = ? AND iddoc = ?";

        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updaterStmt = null;

        try
        {

            updateStmt = connection.prepareStatement(updateEmpruntQuery);
            insertStmt = connection.prepareStatement(insertEmpruntQuery);
            updaterStmt = connection.prepareStatement(updateResaQuery);
            updateStmt.setInt(1, numero());
            updateStmt.executeUpdate();

            insertStmt.setInt(1, ab.getNum());
            insertStmt.setInt(2, numero());
            LocalDateTime debut = LocalDateTime.now();
            LocalDateTime fin = debut.plusMonths(2); //Deux mois pour rendre le document
            insertStmt.setTimestamp(3, Timestamp.valueOf(fin));
            insertStmt.executeUpdate();


            if(this.reserveur() == ab.getNum()) {
                updaterStmt.setInt(1, ab.getNum());
                updaterStmt.setInt(2, numero());
                updaterStmt.executeUpdate();
            }



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
    public synchronized void retourner(Abonne ab) {


        String deleteQuery = "DELETE FROM Emprunt WHERE iddoc = ?";
        String updateQuery = "UPDATE Document SET disponible = 1 WHERE id_doc = ?";


        PreparedStatement deleteStmt = null;
        PreparedStatement updateStmt =null;
        try {

            deleteStmt = connection.prepareStatement(deleteQuery);
            updateStmt = connection.prepareStatement(updateQuery);
            deleteStmt.setInt(1,numero() );
            deleteStmt.executeUpdate();
            updateStmt.setInt(1, numero());
            updateStmt.executeUpdate();


            if(emprunteur() == ab.getNum()) {
                if(degrade() || delais(emprunteur()) ) {
                    ab.ban();
                }
            }
            mail();


        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if( updateStmt != null || deleteStmt != null) {
                    updateStmt.close();
                    deleteStmt.close();
                }
                if (connection != null) connection.close();
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void mail() {
        try  {
            String query = "SELECT * FROM alerte WHERE numDoc = ?";
            PreparedStatement stmt = (PreparedStatement) connection.prepareStatement(query);
            stmt.setInt(1, numero());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props,
                        new Authenticator() {
                            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(from));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(pour));
                    message.setSubject("Votre DVD est de retour !");
                    message.setText("Bonjour,\n\nLe livre " + this.title + " est de retour ! Vous pouvez désormais aller le réserver ou directement l'emprunter"
                            + ".\n\nLa tribue vous salue.\n\nTél : 01 02 03 04 05\nMail : tribu.mediatheque@gmail.com\nAdresse : 143 Avenue de Versailles, 75016 Paris");

                    Transport.send(message);

                    String deleteQuery = "DELETE FROM alerte WHERE numDoc = ?";
                    PreparedStatement deleteStmt = (PreparedStatement) connection.prepareStatement(deleteQuery);
                    deleteStmt.setInt(1, numero());
                    deleteStmt.executeUpdate();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private boolean delais(int numAbo) {
        boolean delaisDepasse = false;
        try {Connection conn = connect();
            String selectQuery = "SELECT date_retour FROM emprunt WHERE idabo = ?";
            PreparedStatement stmt = (PreparedStatement) conn.prepareStatement(selectQuery);
            stmt.setInt(1, numAbo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDateTime date_retour = rs.getObject("date_retour", LocalDateTime.class);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime delaisplus = date_retour.plusWeeks(2);//Deux semaines apres la date de retour off

                if (now.isAfter(delaisplus)) {

                    delaisDepasse = true;
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return delaisDepasse;
    }


    private Boolean degrade() {
        Random random = new Random();
        int randomNumber = random.nextInt(100);
        return randomNumber <= 19;
    }


}


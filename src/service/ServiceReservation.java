package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import DB.*;

import static DB.Bd.reservation;

public class ServiceReservation extends Service {



    public ServiceReservation(Socket socket){

        super(socket);
    }

    @Override
    public void run() {
        System.out.println("********* Connexion démarrée au service Reservation: " + this.socket.getInetAddress() + " *********");
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Envoi du message initial
            out.println("Donne nous le numéro d'abonné  :");
            int nbAbo= Integer.parseInt(in.readLine());
            out.println("Donne nous le numéro de document que tu souhaiites réservé :");
            int nbDoc = Integer.parseInt(in.readLine());

            reserverDocument(nbAbo,nbDoc);
//            // Lecture des données du client
//            String numeroAbonne = in.readLine();
//            String numeroDocument = in.readLine();

            // Vérification des données
//            if (nbAbo != null && nbDoc != null) {
//                boolean success = reserverDocument(numeroAbonne, numeroDocument);

                // Envoi de la réponse au client
//            if (success) {
//                    out.println("Réservation confirmée !");
//                } else {
//                    out.println("Échec de la réservation !");
//                }
//            } else {
//                out.println("Données invalides !");

        } catch (IOException e) {
            System.err.println("Erreur réseau : " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
    }


    private synchronized void reserverDocument(int numeroAbonne, int numeroDocument) {
        reservation(numeroAbonne,numeroDocument);
        System.out.println("Réservation pour l'abonné " + numeroAbonne + " du document " + numeroDocument);

    }

}

package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import DB.*;
import Documents.EmpruntException;
import Documents.IDocument;
import Documents.ReservationException;

public class ServiceReservation extends Service {

    public ServiceReservation(Socket socket){

        super(socket);
    }

    @Override
    public void run() {
        System.out.println("********* Connexion demarree au service Reservation: " + this.socket.getInetAddress() + " *********");
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Envoi du message initial
            out.println("Donne nous le numero d'abonne  :");
            int nAbo= Integer.parseInt(in.readLine());
            out.println("Donne nous le numero de document que tu souhaites reserver :");
            int nDoc = Integer.parseInt(in.readLine());
            IDocument d=DB.getDocuments().get(nDoc);
            if (!DB.getAbonnes().containsKey(nAbo)) {
                out.println("Ce numéro d'abonnée n'existe pas !");
            }
            else if (!DB.getDocuments().containsKey(nDoc)) {
                out.println("Ce numéro de documents  n'existe pas");
            }
            else if (DB.getEmprunt().get(d)!= null) {
                out.println( "Le document est déjà emprunté ");
            } else if (DB.getReservation() != null) {
                int temps = DB.tempsRestant(nDoc);
                if (temps > 30) {
                    out.println( "Ce document est réservé encore " + DB.tempsRestantHeure(nDoc));}

            } else if (DB.getAbonnes().get(nAbo).getAge() < 16 && DB.getAdulte(nDoc)) {
                out.println( "Vous n’avez pas l’âge pour reserver ce DVD.");}else{
                DB.getDocuments().get(nDoc).reserver(DB.getAbonnes().get(nAbo));
            }

        } catch (IOException e) {
            System.err.println("Erreur reseau : " + e.getMessage());
        } catch (ReservationException e) {
            throw new RuntimeException(e);
        } catch (EmpruntException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
    }


}

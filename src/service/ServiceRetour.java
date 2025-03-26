package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import DB.*;


public class ServiceRetour extends Service {

    public ServiceRetour(Socket socket) {
        super(socket);

    }

    @Override
    public void run() {
        System.out.println("********* Connexion demarree au service Retour: " + this.socket.getInetAddress() + " *********");
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {


            out.println("Donne nous le numero de document que tu souhaites retourner ");
            int numDoc = Integer.parseInt(in.readLine());

            if(!DB.getDocuments().containsKey(numDoc)){
                out.println("Document inexistant.");
                System.out.println("Echec du retour.");
            }
            else {
                if (DB.getEmprunt().get(numDoc) != null) {//si ya bien quelqun qui a reserve
                    int numAboReserveur = DB.getReservation().get(numDoc); //repub num abonnee
                    DB.suppReservations(numAboReserveur, numDoc);
                }
                DB.getDocuments().get(numDoc).retourner();
                out.println("Document retourner avec succees !");
                System.out.println("Le document n° "+ numDoc + " vient d'etre rendu.");
            }

        } catch (IOException e) {
            System.err.println("Erreur reseau : " + e.getMessage());
        } finally {
            System.out.println( "********* Connexion terminee *********" );
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
    }

//    private synchronized void retournerDocument(int numeroAbonne, int numeroDocument) {
//        retour(numeroAbonne, numeroDocument);
//        //System.out.println("Retour effectué pour l'abonné " + numeroAbonne + " du document " + numeroDocument);
//    }
}

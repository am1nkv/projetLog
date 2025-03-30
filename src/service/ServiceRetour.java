package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import DB.*;
import Documents.EmpruntException;


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
            boolean peutRetourner = true;
            int numAboReserveur =0;
            int numAboEmprunteur =0;
            //recup le reserveur
            for( int i :DB.getReservation().keySet() ) {
                if(DB.getReservation().get(i) == numDoc){
                    numAboReserveur=i;
                }
            }
            //recup lemprunteur
            for( int i :DB.getEmprunt().keySet() ) {
                if(DB.getEmprunt().get(i) == numDoc){
                    numAboEmprunteur =i;
                }
            }
            System.out.println(numAboEmprunteur);
            if (!DB.getAbonnes().containsKey(numAboEmprunteur)) {
                out.println("ERREUR: Abonné introuvable.");
                System.out.println("Echec du retour.");
                peutRetourner = false;
                return;
            }
            if(!DB.getDocuments().containsKey(numDoc)){
                out.println("ERREUR:Document inexistant.");
                System.out.println("Echec du retour.");
                peutRetourner = false;
                return;
            }
            if(DB.getEmprunt().isEmpty()) {
                out.println("ERREUR:Emprunt inexistant.");
                System.out.println("Echec du retour.");
                peutRetourner = false;
                return;
            }


            if(peutRetourner) {
                  DB.suppReservations(numAboReserveur, numDoc);
                  DB.getDocuments().get(numDoc).retourner(DB.getAbonnes().get(numAboEmprunteur));
                  out.println("OK:Document retourner avec succees !");
                  System.out.println("Le document n° "+ numDoc + " vient d'etre rendu.");
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("********* Connexion terminée *********");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }




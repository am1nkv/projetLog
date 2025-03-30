package service;
import Documents.EmpruntException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import static DB.Bd.*;

public class ServiceEmprunt extends Service {

    public ServiceEmprunt(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        System.out.println("********* Connexion demarree au service Emprunt: " + this.socket.getInetAddress() + " *********" );

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            boolean peutEmprunter = true;
            // Envoi du message initial
            out.println("Donnes-nous le numero d'abonne  :");
            int nbAbo= Integer.parseInt(in.readLine());
            if(!DB.getAbonnes().containsKey(nbAbo)) {
                out.println("ERREUR:Ce numero d'abonne est inexistant.");
                System.out.println("Echec de l'emprunt.");
                peutEmprunter = false;
                return;
            }

            out.println("Donnes-nous le numero du document que vous souhaitez emprunter :");
            int nbDoc = Integer.parseInt(in.readLine());
            if(!DB.getDocuments().containsKey(nbDoc)) {
                out.println("ERREUR:Le document que vous souhaitez n'existe pas.");
                System.out.println("Echec de l'emprunt.");
                peutEmprunter = false;
                return;
            }
            if(DB.getEmprunt().containsValue(nbDoc)) {
                out.println("ERREUR:Le document est deja emprunte...");
                System.out.println("Echec de l'emprunt.");
                peutEmprunter = false;
            }
            System.out.println(DB.getAbonnes().get(nbAbo).getBan());
            if (DB.getAbonnes().get(nbAbo).getBan()==true) {
                out.println("ERREUR:VOUS ETES BANNI DE LA TRIBU");
                System.out.println("Document emprunte !");
                peutEmprunter = false;
                return;
            }
            if(DB.getReservation().containsValue(nbDoc) && DB.getReservation().get(nbAbo)!= nbDoc) {
                out.println("ERREUR:Le document est deja reserve...");
                System.out.println("Echec de l'emprunt.");
                peutEmprunter = false;
            }
            if(DB.getAbonnes().get(nbAbo).getAge() < 16 && DB.getAdulte(nbDoc)){
                out.println("ERREUR:Vous n’avez pas l’age pour emprunter ce document.");
                System.out.println("Echec du l'emprunt.");
                peutEmprunter = false;
                return;
            }
           if (peutEmprunter) {
                out.println("OK:Document emprunter avec succes !");
                System.out.println("Le document " + nbDoc+ "a ete emprunter");
                DB.getDocuments().get(nbDoc).emprunter(DB.getAbonnes().get(nbAbo));
            }


        }
        catch (IOException | EmpruntException e) {
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


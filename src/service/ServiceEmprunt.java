package service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static DB.Bd.emprunt;
import static DB.Bd.reservation;


public class ServiceEmprunt extends Service {

    public ServiceEmprunt(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        System.out.println("********* Connexion démarrée au service Emprunt: " + this.socket.getInetAddress());

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Envoi du message initial
            out.println("Donne nous le numero d'abonne  :");
            int nbAbo= Integer.parseInt(in.readLine());
            out.println("Donne nous le numero de document que tu souhaites emprunter :");
            int nbDoc = Integer.parseInt(in.readLine());
            emprunterDocument(nbAbo, nbDoc);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("********* Connexion terminée");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private synchronized void emprunterDocument(int numeroAbonne, int numeroDocument) {
        emprunt(numeroAbonne,numeroDocument);
        System.out.println("Emprunt pour l'abonné " + numeroAbonne + " du document " + numeroDocument);

    }
}
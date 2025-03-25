package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import DB.*;
import static DB.Bd.retour;

public class ServiceRetour extends Service {

    public ServiceRetour(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        System.out.println("********* Connexion démarrée au service Retour: " + this.socket.getInetAddress() + " *********");
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Demande des informations au client
            out.println("Donne nous le numero d'abonne :");
            int nbAbo = Integer.parseInt(in.readLine());
            out.println("Donne nous le numero de document que tu souhaites retourner :");
            int nbDoc = Integer.parseInt(in.readLine());

            retournerDocument(nbAbo, nbDoc);

        } catch (IOException e) {
            System.err.println("Erreur reseau : " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
    }

    private synchronized void retournerDocument(int numeroAbonne, int numeroDocument) {
        retour(numeroAbonne, numeroDocument);
        System.out.println("Retour effectué pour l'abonné " + numeroAbonne + " du document " + numeroDocument);
    }
}

package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import service.Service;

public class ServiceReservation extends Service {

    protected Socket socket;

    public ServiceReservation(Socket socket){
        super(socket);
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            // Lecture des données du client
            String numeroAbonne = in.readLine();
            String numeroDocument = in.readLine();

            // Vérification des données
            if (numeroAbonne != null && numeroDocument != null) {
                boolean success = reserverDocument(numeroAbonne, numeroDocument);

                // Envoi de la réponse au client
                if (success) {
                    out.println("Réservation confirmée !");
                } else {
                    out.println("Échec de la réservation !");
                }
            } else {
                out.println("Données invalides !");
            }
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

    // Simule la réservation (à adapter selon ton système)
    private boolean reserverDocument(String numeroAbonne, String numeroDocument) {
        // Ici, tu peux ajouter une interaction avec une base de données ou une liste en mémoire
        System.out.println("Réservation pour l'abonné " + numeroAbonne + " du document " + numeroDocument);
        return true; // Supposons que la réservation est toujours acceptée pour l'instant
    }

}

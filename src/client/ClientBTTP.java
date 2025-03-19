package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientBTTP {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Demande ? L'utilisateur d'entrer l'URL BTTP
        System.out.print("Entrez l'URL du serveur (ex: BTTP:localhost:1234) : ");
        String url = scanner.nextLine();

        // Parsing de l'URL
        if (!url.startsWith("BTTP:")) {
            System.out.println("Erreur : URL invalide. Utilisez le format BTTP:host:port");
            scanner.close();
            return;
        }

        String[] parts = url.substring(5).split(":");
        if (parts.length != 2) {
            System.out.println("Erreur : Format incorrect. Utilisez BTTP:host:port");
            scanner.close();
            return;
        }

        String host = parts[0];
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Erreur : Le port doit ?tre un nombre.");
            scanner.close();
            return;
        }

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connecté au serveur BTTP sur " + host + ":" + port);

            // Lire le message initial du serveur
            String serverMessage = in.readLine();
            System.out.println("Serveur: " + serverMessage);

            while (true) {
                System.out.print("Vous: ");
                String userInput = scanner.nextLine();
                out.println(userInput); // Envoyer le message au serveur

                if (userInput.equalsIgnoreCase("QUIT")) {
                    break; // Quitter le programme
                }

                // Lire la réponse du serveur
                String response = in.readLine();
                if (response == null) {
                    break; // Le serveur a fermé la connexion
                }

                System.out.println("Serveur: " + response);
            }

        } catch (IOException e) {
            System.err.println("Erreur: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}

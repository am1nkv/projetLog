package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServiceEmprunt extends Service {

    public ServiceEmprunt(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        System.out.println("********* Connexion démarrée : " + this.socket.getInetAddress());

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Envoi du message initial
            out.println("Donne nous le numéro d'abonné  :");
            int nbAbo= Integer.parseInt(in.readLine());
            out.println("Donne nous le numéro de document que tu souhaiites réservé :");
            int nbDoc = Integer.parseInt(in.readLine());
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
}
package Serveur;


import service.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur implements Runnable {
    private ServerSocket listen_socket;
    private int port;
    private Class<? extends Service> serviceClass; // La classe du service, limitée aux sous-classes de Service

    public Serveur(Class<? extends Service> serviceClass, int port) throws IOException {
        this.port = port;
        this.listen_socket = new ServerSocket(this.port);
        this.serviceClass = serviceClass;
    }

    @Override
    public void run() {
        try {
            System.err.println("Lancement du serveur au port " + this.listen_socket.getLocalPort());
            while (true) {
                Socket socket = listen_socket.accept();
                try {
                    Service service = serviceClass.getDeclaredConstructor(Socket.class).newInstance(socket);
                    new Thread(service).start();
                } catch (InstantiationException e) {
                    // En cas d'erreur lors de l'instanciation, imprime un message d'erreur
                    System.err.println("Erreur lors de l'instanciation du service : " + e.getMessage());
                } catch (IllegalAccessException e) {
                    // En cas d'acc?s interdit, imprime un message d'erreur
                    System.err.println("Acc?s interdit lors de l'instanciation du service : " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    // En cas d'argument non valide, imprime un message d'erreur
                    System.err.println("Argument non valide lors de l'instanciation du service : " + e.getMessage());
                } catch (InvocationTargetException e) {
                    // En cas d'exception levée par la méthode invoquée, imprime un message d'erreur
                    System.err.println("Exception levée lors de l'invocation de la méthode : " + e.getCause().getMessage());
                } catch (NoSuchMethodException e) {
                    // En cas de méthode non trouvée, imprime un message d'erreur
                    System.err.println("Méthode non trouvée lors de l'instanciation du service : " + e.getMessage());
                } catch (SecurityException e) {
                    // En cas de violation de la sécurité, imprime un message d'erreur
                    System.err.println("Violation de la sécurité lors de l'instanciation du service : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            try {
                this.listen_socket.close();
            } catch (IOException e1) {
                // Ignorer l'exception
            }
            System.err.println("Arr?t du serveur au port " + this.listen_socket.getLocalPort());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (listen_socket != null && !listen_socket.isClosed()) {
                listen_socket.close();
            }
        } catch (IOException e1) {
            // Ignorer l'exception
        }
    }
}
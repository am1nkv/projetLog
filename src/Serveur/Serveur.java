package Serveur;
import DB.Bd;
import service.Service;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Provider;

public class Serveur implements Runnable {
    private final ServerSocket listen_socket;
    private final int port;
    private final Class<? extends Service> serviceClass; // La classe du service, limitée aux sous-classes de Service
    private Bd b;
    public Serveur(Class<? extends Service> serviceClass, int port,Bd bdd) throws IOException {
        this.port = port;
        this.listen_socket = new ServerSocket(this.port);
        this.serviceClass = serviceClass;
        this.b = bdd;
    }

    @Override
    public void run() {
        try {
            System.err.println("Lancement du serveur au port " + this.listen_socket.getLocalPort());
            while (true) {
                Socket socket = listen_socket.accept();

                try {
                   Service service = serviceClass.getDeclaredConstructor(Socket.class).newInstance(socket);
                    service.setSocket(socket);
                    service.setBD(new Bd());
                    new Thread(service).start();

                } catch (InstantiationException e) {
                    // En cas d'erreur lors de l'instanciation, imprime un message d'erreur
                    System.err.println("Erreur lors de l'instanciation du service : " + e.getMessage());
                } catch (IllegalAccessException e) {
                    // En cas d'acc?s interdit, imprime un message d'erreur
                    System.err.println("Acces interdit lors de l'instanciation du service : " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    // En cas d'argument non valide, imprime un message d'erreur
                    System.err.println("Argument non valide lors de l'instanciation du service : " + e.getMessage());
                } catch (InvocationTargetException e) {
                    // En cas d'exception levée par la méthode invoquée, imprime un message d'erreur
                    System.err.println("Exception levee lors de l'invocation de la méthode : " + e.getCause().getMessage());
                } catch (NoSuchMethodException e) {
                    // En cas de méthode non trouvée, imprime un message d'erreur
                    System.err.println("Methode non trouvee lors de l'instanciation du service : " + e.getMessage());
                } catch (SecurityException e) {
                    // En cas de violation de la sécurité, imprime un message d'erreur
                    System.err.println("Violation de la securite lors de l'instanciation du service : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            try {
                this.listen_socket.close();
            } catch (IOException e1) {
                // Ignorer l'exception
            }
            System.err.println("Arret du serveur au port " + this.listen_socket.getLocalPort());
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
package service;
import service.Service;
import java.net.Socket;

public class ServiceRetour extends Service {
    protected Socket socket;

    public ServiceRetour(Socket socket){

        super(socket);
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        System.out.println("********* Connexion démarrée au service Retour: " + this.socket.getInetAddress());

    }
}

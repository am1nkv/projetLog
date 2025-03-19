package service;

import java.net.*;

public abstract class Service implements Runnable {
    protected Socket socket;

    public Service(Socket socket) {
        this.socket = socket;
    }

    // Setter pour la socket, a appeler apres instanciation via newInstance()
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    // Getter pour la socket (optionnel)
    public Socket getSocket() {
        return this.socket;
    }

    // La méthode run() reste abstraite pour etre implémentée dans les classes concretes
}
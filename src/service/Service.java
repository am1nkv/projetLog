package service;

import DB.Bd;

import java.net.*;

public abstract class Service implements Runnable {
    protected Socket socket;
    protected Bd DB;
    public Service(Socket socket ) {
        this.socket = socket;

    }
    public Service() {
        // Constructeur vide obligatoire
    }
    public void setBD(Bd b) {
        this.DB = b;
    }
    // Setter pour la socket, a appeler apres instanciation via newInstance()
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    // Getter pour la socket (optionnel)
    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public abstract void run();

    // La méthode run() reste abstraite pour etre implémentée dans les classes concretes
}
package Serveur;

import service.ServiceEmprunt;
import service.ServiceReservation;
import service.ServiceRetour;

public class AppliServeur {
    public static void main(String[] args) {
        try {
            new Thread(new Serveur(ServiceRetour.class, 4000)).start();
            new Thread(new Serveur(ServiceEmprunt.class, 3000)).start();
            new Thread(new Serveur(ServiceReservation.class, 2000)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

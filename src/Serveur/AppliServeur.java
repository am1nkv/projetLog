package Serveur;

import DB.Bd;
import service.Service;
import service.ServiceEmprunt;
import service.ServiceReservation;
import service.ServiceRetour;

public class AppliServeur {
    public static void main(String[] args) {
        Bd b = new Bd();

        try {
            new Thread(new Serveur(ServiceRetour.class, 4000,b)).start();
            new Thread(new Serveur(ServiceEmprunt.class, 3000,b)).start();
            new Thread(new Serveur(ServiceReservation.class, 2000,b)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

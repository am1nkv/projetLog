package Documents;

public interface IDocument {
    int numero();
    // exception si déjà réservé ou emprunté
    void reserver (Abonne ab) throws ReservationException, EmpruntException;

    // exception si réservé pour une autre abonné ou déjà emprunté
    void emprunter(Abonne ab) throws EmpruntException;

    // sert au retour d’un document ou à l’annulation d‘une réservation
    //Modifiacation de l'interface pour la certif geremino desoles on a essayé sans mais on a pas pu
    void retourner(Abonne ab);
}

package Documents;

public class Livres implements IDocument {
    private final String title;
    private final int numero;
    private final int nb_pages;

    public Livres(String title, int numero, int nb_pages) {
        this.title = title;
        this.numero = numero;
        this.nb_pages = nb_pages;
    }
    @Override
    public int numero() {
        return numero;
    }

    @Override
    public void reserver(Abonne ab) throws ReservationException {

    }

    @Override
    public void emprunter(Abonne ab) throws EmpruntException {

    }

    @Override
    public void retourner() {

    }
}

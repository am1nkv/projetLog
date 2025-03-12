public class DVD implements IDocument{
    private String title;
    private int numero;
    private boolean adulte;

    public DVD(String title, int numero, boolean adulte){
        this.title = title;
        this.numero = numero;
        this.adulte = adulte;

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

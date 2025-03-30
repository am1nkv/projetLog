package Documents;
public class Banni implements Runnable {
    private Abonne abonne;
    private long tempsbanni;
    private long debutbanni;

    public Banni(long debut, Abonne abonne) {
        this.abonne = abonne;
        this.debutbanni = debut;
        this.tempsbanni = 2628000;   // 1mois converviti en seconde
    }

    @Override
    public void run() {
        while(System.currentTimeMillis() - debutbanni < tempsbanni) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        abonne.unban();
    }

}
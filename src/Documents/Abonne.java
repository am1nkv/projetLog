package Documents;

import java.util.Date;

public class Abonne {
    private final int num;
    private final String nom;
    private final Date date ;

    public Abonne(int num, String nom, Date date) {
        this.num = num;
        this.nom = nom;
        this.date = date;
    }

    public int getNum() {
        return num;
    }
}

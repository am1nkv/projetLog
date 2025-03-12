import java.util.Date;

public class Abonne {
    private int num;
    private String nom;
    private Date date ;

    public Abonne(int num, String nom, Date date) {
        this.num = num;
        this.nom = nom;
        this.date = date;
    }

    public int getNum() {
        return num;
    }
}

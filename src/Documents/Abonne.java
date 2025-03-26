package Documents;

import java.time.LocalDate;
import java.time.Period;

public class Abonne {
    private final int num;
    private final String nom;
    private final LocalDate date_de_naissance;
    private Boolean ban;

    public Abonne(int num, String nom, LocalDate date) {
        this.num = num;
        this.nom = nom;
        this.date_de_naissance = date;
    }

    public int getNum() {
        return num;
    }

    public String getNom() {
        return nom;
    }

    public boolean getBan() {
        return ban;
    }
    public LocalDate getDateNaissance() {
            return date_de_naissance;
        }

    public void endBan() {
        ban = false;
    }
//
//        public void startBan() {
//            ban = true;
//            new Thread(new Ban(System.currentTimeMillis(), this));
//        }
    public int getAge() {
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(date_de_naissance, currentDate);
        return period.getYears();
    }
}



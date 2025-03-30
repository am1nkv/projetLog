package Documents;

import java.time.LocalDate;
import java.time.Period;

public class Abonne {
    private final int num;
    private final String nom;
    private final LocalDate date_de_naissance;
    private Boolean ban;
    private boolean averti;

    public Abonne(int num, String nom, LocalDate date) {
        this.num = num;
        this.nom = nom;
        this.date_de_naissance = date;
        this.ban = false;
        this.averti = false;
    }
    public Abonne getAbonne(int num) {
        if (num == this.num){
        return this;
        }
        else {
            return null;
        }
    }
    public boolean getAverti() {
        return averti;
    }
    public void setAverti(boolean averti) {
        this.averti = averti;
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


    public int getAge() {
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(date_de_naissance, currentDate);
        return period.getYears();
    }
    public void ban() {
        ban = true;
        new Thread(new Banni(System.currentTimeMillis(), this));
    }
    public void unban() {
        ban = false;
    }
}



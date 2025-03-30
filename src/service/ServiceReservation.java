package service;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import DB.*;
import Documents.EmpruntException;
import Documents.IDocument;
import Documents.ReservationException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class ServiceReservation extends Service {

    private static final String MUSIC_FILE = "musique_attente.wav";
    private Clip clip;
        public ServiceReservation(Socket socket) {
            super(socket);
        }

        @Override
        public void run() {
            System.out.println("********* Connexion demarree au service Reservation: " + this.socket.getInetAddress() + " *********");
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // Envoi du message initial
                out.println("Donne nous le numero d'abonne :");
                int nAbo = Integer.parseInt(in.readLine());
                out.println("Donne nous le numero de document que tu souhaites reserver :");
                int nDoc = Integer.parseInt(in.readLine());

                // Variable pour controler si la reservation est possible
                boolean peutReserver = true;


                if (!DB.getAbonnes().containsKey(nAbo)) {
                    out.println("ERREUR:Ce numero d'abonne n'existe pas !");
                    System.out.println("Abonne introuvable !");
                    peutReserver = false;
                    in.close();
                    return;
                }


                if (!DB.getDocuments().containsKey(nDoc)) {
                    out.println("ERREUR:Ce numero de document n'existe pas !");
                    System.out.println("Document introuvable !");
                    peutReserver = false;
                    return;
                }


                if (DB.getAbonnes().get(nAbo).getBan()==true) {
                    out.println("ERREUR:VOUS ETES BANNI DE LA TRIBU");
                    System.out.println("Document emprunte !");
                    peutReserver = false;
                    return;
                }
                if (DB.getEmprunt().containsValue(nDoc)) {
                    out.println("ERREUR:Le document est deja emprunte !");
                    System.out.println("Document emprunte !");
                    peutReserver = false;
                    return;
                }


                if (DB.getAbonnes().get(nAbo).getAge() < 16 && DB.getAdulte(nDoc)) {
                    out.println("ERREUR:Vous n'avez pas l'age pour reserver ce DVD.");
                    System.out.println("Age insuffisant !");
                    peutReserver = false;
                    return;
                }
                int temps = DB.tempsRestant(nDoc);
                if (DB.getReservation().containsValue(nDoc) && !DB.getAbonnes().get(nAbo).getAverti()) {
                    out.println("Ce document est reserve encore " + DB.tempsRestantHeure(nDoc) + ". Souhaitez-vous être averti lors du retour du document ? Oui ou Non.");
                    String res = in.readLine();
                    if (res.equalsIgnoreCase("Oui") ) {
                       DB.addAlerte(nAbo, nDoc);
                        DB.getAbonnes().get(nAbo).setAverti(true);

                      out.println("cool chaman");
                    } else if (res.equalsIgnoreCase("Non")) {
                        out.println(" c'est vous qui voyez chef");
                    }else {
                        out.println("ERREUR:Nous n'avons pas pu interpréter votre demande");
                    }
                    peutReserver = false;
                }
                if (DB.getReservation().containsValue(nDoc) && temps < 30) {
                    out.println("Ce document est reserve juste pour encore " + DB.tempsRestantHeure(nDoc) + ". Souhaitez-vous attendre ? Oui ou Non.");
                    String res = in.readLine();
                    if (res.equalsIgnoreCase("Oui")) {
                        playMusic(MUSIC_FILE);
                        out.println(" Patientez avec une musique d'attente et, des que les augures le permettront, on validera votre reservation");
                        long startTime = System.currentTimeMillis();
                        while (true) {
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            if (elapsedTime >= temps * 1000) {
                                break;
                            }
                            if (DB.getEmprunt().containsValue(nDoc) ) {
                                stopMusic();
                                out.println( "ERREUR:Le document vient d'être récupéré." );
                                System.out.println("Echec de la reservation.");
                                peutReserver = false;
                                return;
                            }
                            Thread.sleep(1000);
                        }
                        stopMusic();
                       peutReserver = true;
                    }
                }


                if (peutReserver) {
                    DB.getDocuments().get(nDoc).reserver(DB.getAbonnes().get(nAbo));
                    out.println("OK:Reservation effectuee avec succes !");
                    System.out.println("Reservation effectuee");
                } else {
                    out.println("Reservation impossible.");
                }

            } catch (IOException e) {
                System.err.println("Erreur reseau : " + e.getMessage());
            } catch (ReservationException | EmpruntException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
                }
            }
        }
    private void playMusic(String musicFile) {
        try {
            File file = new File(musicFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la musique : " + e);
        }
    }

    private void stopMusic() {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'arrêt de la musique : " + e);
        }
    }
    }

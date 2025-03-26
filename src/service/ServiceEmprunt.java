package service;
import Documents.EmpruntException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import static DB.Bd.*;

public class ServiceEmprunt extends Service {

    public ServiceEmprunt(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        System.out.println("********* Connexion demarree au service Emprunt: " + this.socket.getInetAddress() + " *********" );

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Envoi du message initial
            out.println("Donnes-nous le numero d'abonne  :");
            int nbAbo= Integer.parseInt(in.readLine());
            if(!DB.getAbonnes().containsKey(nbAbo)) {
                out.println("Ce numero d'abonne est inexistant.");
                System.out.println("Echec de l'emprunt.");
                return;
            }

            out.println("Donnes-nous le numero du document que vous souhaitez emprunter :");
            int nbDoc = Integer.parseInt(in.readLine());
            if(!DB.getDocuments().containsKey(nbDoc)) {
                out.println("Le document que vous souhaitez n'existe pas.");
                System.out.println("Echec de l'emprunt.");
            }
            if(DB.getEmprunt().containsKey(nbDoc)) {
                out.println("Le document est deja emprunte...");
                System.out.println("Echec de l'emprunt.");
            }
            if(DB.getReservation().containsKey(nbDoc)) {
                out.println("Le document est deja reserve...");
                System.out.println("Echec de l'emprunt.");
            }
            if(DB.getAbonnes().get(nbAbo).getAge() < 16 && DB.getAdulte(nbDoc)){
                out.println("Vous n’avez pas l’age pour emprunter ce document.");
                System.out.println("Echec du l'emprunt.");
            }
            else {
                DB.getDocuments().get(nbDoc).emprunter(DB.getAbonnes().get(nbAbo));
            }


        }
        catch (IOException | EmpruntException e) {
            e.printStackTrace();
        }

        System.out.println("********* Connexion terminée *********");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
package Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import BD.bd;
import Documents.DVD;
import Documents.EmpruntException;
import Documents.Livre;
import Serveur.Abonne;
import Serveur.Document;
import libServeur.Service;

public class ServiceEmpruntRetour extends Service {

	 private Socket client;
	 private bd BD;

	 public ServiceEmpruntRetour() {
	     this.client = new Socket();
	 }

	 public void setSock(Socket s) {
	     this.client = s;
	 }

	 public void setBD(bd b) {
	        this.BD = b;
	    }

	 public void run() {
		 	String greenColor = "\u001B[32m";
	        String resetColor = "\u001B[0m";
	    	System.out.println(greenColor + "*********Connexion démarre" + resetColor);
	        try {
	            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

	            String ANSI_RED = "\u001B[91m";
	            String ANSI_RESET = "\u001B[0m";

	            LocalDateTime futureDateTime;
	        	DateTimeFormatter formatter;
	        	String formattedFutureDateTime;

	            String serv;
                int numAbo;
                int numDoc;

	            out.println("Souhaitez-vous réaliser un emprunt ou un retour :");
                serv = in.readLine();

	                	if(serv.equals("retour")) {
	                		out.println("Tapez le numéro du document que vous souhaitez retourner :");
	                        numDoc = Integer.parseInt(in.readLine());

	                		if(!BD.getDocuments().containsKey(numDoc)){
	                			 out.println(ANSI_RED + "Document inexistant." + ANSI_RESET);
	                			 System.out.println("Echec du retour.");
	                		}

	                		else {
	                			if (BD.getDocuments().get(numDoc).reserveur() != null) {
	                			    int numAboReserveur = BD.getDocuments().get(numDoc).reserveur().getNum();
	                			    BD.supprimeRes(numAboReserveur, numDoc);
	                			}
	                			BD.getDocuments().get(numDoc).retour();
				                out.println(greenColor + "Document retourné avec succées !" + resetColor);
				                System.out.println("Le document n° "+ numDoc + " vient d'etre rendu.");
	                		}

			            }else if(serv.equals("emprunt")) {
			            	out.println("Tapez votre numéro d'abonné :");
	                        numAbo = Integer.parseInt(in.readLine());
	                        if(!BD.getAbonnes().containsKey(numAbo)) {
				                out.println(ANSI_RED + "Ce numéro d'abonnée est inexistant." + ANSI_RESET);
				                System.out.println("Echec du l'emprunt.");
				                return;
				            }
	                        if (BD.getAbonnes().get(numAbo).getBan()) {
	                            out.println(ANSI_RED + "Vous etes encore banni." + ANSI_RESET);
	                            System.out.println("Echec de la reservation.");
	                            return;
	                        }

	                        out.println("Tapez le numéro du document que vous souhaitez emprunter :");
	                        numDoc = Integer.parseInt(in.readLine());

			            	if(!BD.getDocuments().containsKey(numDoc)) {
				                out.println(ANSI_RED + "Ce numéro document est inexistant."+ ANSI_RESET);
				                System.out.println("Echec du l'emprunt.");
				                return;
				            }
				            if(BD.getDocuments().get(numDoc).emprunteur() != null){
				            	out.println(ANSI_RED + "Ce document est déja emprunté par un autre abonnée." + ANSI_RESET);
				            	System.out.println("Echec du l'emprunt.");
				            }
				            else if(BD.getDocuments().get(numDoc).reserveur() != null && BD.getDocuments().get(numDoc).reserveur() != BD.getAbonnes().get(numAbo)){
				            		out.println(ANSI_RED + "Ce document est réservé encore "+ BD.tempsRestantHeure(numDoc) + ANSI_RESET);
				            		System.out.println("Echec de la reservation.");

				            }
				            else if(BD.getAbonnes().get(numAbo).getAge() < 16 && BD.getAdulte(numDoc)){
		   		            	 out.println(ANSI_RED + "Vous n’avez pas l’âge pour emprunter ce DVD." + ANSI_RESET);
		   		            	 System.out.println("Echec du l'emprunt.");
		                	}
				            else if(BD.getAbonnes().containsKey(numAbo) && BD.getDocuments().containsKey(numDoc)) {
				            	BD.getDocuments().get(numDoc).empruntPar(BD.getAbonnes().get(numAbo));
				                out.println(greenColor + "Document emprunté avec succées !" + resetColor);
				                if(BD.getAbonnes().get(numAbo) == BD.getDocuments().get(numDoc).reserveur()) {
				                	BD.supprimeRes(numAbo, numDoc);
				                }
				                System.out.println("Le document n° "+ numDoc + " vient d'etre emprunté par l'abonnée n° " + numAbo + ".");
				            }
			            } else{
			            	out.println(ANSI_RED + "Veuillez saisir un service correct." + ANSI_RESET);
			            	System.out.println("Echec du service.");
			            }


	        } catch (IOException e) {
	            System.err.println("Erreur lors du traitement de la connexion : " + e);
	        } catch (EmpruntException e) {
				e.printStackTrace();

			}finally {
	        	System.out.println(greenColor + "*********Connexion terminee" + resetColor);

	            try {
	                client.close();
	            } catch (IOException e) {
	                System.err.println("Erreur lors de la fermeture de la connexion: " + e);
	            }
	        }
		}
}

 */
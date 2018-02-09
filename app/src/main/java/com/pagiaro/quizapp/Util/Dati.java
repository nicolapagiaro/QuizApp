package com.pagiaro.quizapp.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe per organizzare i dati che servono all'applicazione
 * Created by nicola on 18/05/17.
 */

public class Dati {
    private ArrayList<HashMap<String, String>> utente;
    private ArrayList<HashMap<String, String>> categorie;
    private ArrayList<HashMap<String, String>> classifica;

    /**
     * Costruttore non parametrico
     */
    public Dati(){
    }

    /**
     * Costruttore parametrico
     * @param utente array sull'utente
     * @param categorie array sulle categorie
     * @param classifica array sulla classifica
     */
    public Dati(ArrayList<HashMap<String, String>> utente, ArrayList<HashMap<String, String>> categorie, ArrayList<HashMap<String, String>> classifica) {
        this.utente = utente;
        this.categorie = categorie;
        this.classifica = classifica;
    }

    public ArrayList<HashMap<String, String>> getUtente() {
        return utente;
    }

    public void setUtente(ArrayList<HashMap<String, String>> utente) {
        this.utente = utente;
    }

    public ArrayList<HashMap<String, String>> getCategorie() {
        return categorie;
    }

    public void setCategorie(ArrayList<HashMap<String, String>> categorie) {
        this.categorie = categorie;
    }

    public ArrayList<HashMap<String, String>> getClassifica() {
        return classifica;
    }

    public void setClassifica(ArrayList<HashMap<String, String>> classifica) {
        this.classifica = classifica;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "Dati{" +
                "utente=" + utente.toString() +
                ", categorie=" + categorie.toString() +
                ", classifica=" + classifica.toString() +
                '}';
    }
}

package com.pagiaro.quizapp.Util;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Classe per gestire le domande del quiz
 * Created by Nicola on 20/05/2016.
 */
public class Domande implements Serializable {
    private ArrayList<Domanda> list;
    private int[] risposte;

    /**
     * SQL version
     * @param a
     */
    public Domande(ArrayList<HashMap<String, String>>  a) {
        list = new ArrayList<>();
        risposte = new int[a.size()];
        for(int i=0; i<a.size(); i++) {
            risposte[i] = -1;
            HashMap<String, String> temp = a.get(i);
            String[] s = new String[5];
            s[0] = temp.get("domanda");
            s[1] = temp.get("rG");
            s[2] = temp.get("rS1");
            s[3] = temp.get("rS2");
            s[4] = temp.get("rS3");
            list.add(new Domanda(s, Integer.parseInt(temp.get("id")), temp.get("immagine")));
            list.get(i).mischia();
        }
    }

    /**
     * Metodo che restituisce una domanda
     * @param i indice della domanda
     * @return l'oggetto Domanda dell'indice indicato
     */
    public Domanda getDomanda(int i) {
        return list.get(i);
    }

    /**
     * Metodo che aggiunge la risposta alla corrispondente domanda
     * @param i indice corrispondente della domanda
     * @param selected numero dell'opzione selezionata (Da 1 a 4, 0 se non risposto)
     */
    public void putRisposta(int i, int selected) {
        risposte[i] = selected;
    }

    /**
     * Metodo che restituisce il punteggio totalizzato
     * @return il punteggio ottenuto
     */
    public int getRisultato() {
        int count = 0;
        for(int i=0; i<list.size(); i++) {
            if(list.get(i).isCorrect(risposte[i]))
                count++;
        }
        return count;
    }

    /**
     * Metodo che restituisce un array degli id delle domande divisi da un punto "."
     * @return la stringa con gli id
     */
    public String getIds() {
        String s = "";
        for(int i=0; i<list.size(); i++) {
            if (i != list.size() - 1) s += list.get(i).getId() + ".";
            else s += list.get(i).getId();
        }
        return  s;
    }

    /**
     * Metodo che resituisce l'array di risposte
     * @return l'array di risposte
     */
    public int[] getRisposte() {
        return risposte;
    }

    /**
     * Metodo che restituisce la risposta data all'indice della domanda fornito
     * @param indice indice della domanda
     * @return la stringa contenente la risposta se essa è stata data se no NULL
     */
    public String getRisposta(int indice) {
        int rispostaData = risposte[indice];
        if (rispostaData != -1)
            return list.get(indice).risposte.get(rispostaData);
        return null;
    }

    /**
     * Metodo che restituisce il numero di domande
     * @return il numero di domande
     */
    public int getCount() {
        return list.size();
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "Domande{" + list.get(0).toString();
    }


    /**
     * Classe che gestisce una singola domanda
     *
     *
     *
     */
    public class Domanda implements Serializable {
        private String immagine;
        private int id;
        private String domanda;
        private ArrayList<String> risposte;
        private int i_giusta;

        /**
         * Costruttore parametrico
         * @param total array di stringhe che comprendono la domanda e le 4 risposte
         * @param id id della domanda
         */
        public Domanda(String[] total, int id, String img) {
            this.id = id;
            immagine = img;
            risposte = new ArrayList<>();
            i_giusta = 0;
            domanda = total[0];
            for(int i=1; i<total.length; i++)
                risposte.add(total[i]);
        }

        /**
         * Metodo che restituisce la domanda
         * @return la stringa contenente la domanda
         */
        public String getDomanda() {
            return domanda;
        }

        /**
         * Metodo che restituisce la domanda indicata
         * @param n numero della domanda (0-3 compresi)
         * @return la stringa contenente la risposta o null se non ci fosse la risposta indicata
         */
        public String getRisposta(int n) {
            if (n < 0 || n > 3) {
                return null;
            }
            return risposte.get(n);
        }

        /**
         * Metodo che verifica se la risposta data corrisponde a quella giusta
         * @param id indice della risposta (0-3 compresi)
         * @return vero se la risposta dataè quella giusta, falso se non lo è
         */
        public boolean isCorrect(int id) {
           if(id < 0 || id > 3)
               return false;
            return i_giusta == id;
        }

        /**
         *
         * @return
         */
        public int getId() {
            return id;
        }

        /**
         *
         * @param id
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return l'indice della risposta giusta
         */
        public int getIGiusta() {
            return  i_giusta;
        }

        /**
         *
         * @return
         */
        public String getImmagine() {
            return immagine;
        }

        /**
         *
         * @param immagine
         */
        public void setImmagine(String immagine) {
            this.immagine = immagine;
        }

        /**
         * Metodo che mischia l'ordine delle risposte
         */
        public void mischia() {
            String g = risposte.get(0);
            Collections.shuffle(risposte);
            for(int i=0; i<risposte.size(); i++)
                if(risposte.get(i).equals(g))
                    i_giusta = i;
        }
    }
}

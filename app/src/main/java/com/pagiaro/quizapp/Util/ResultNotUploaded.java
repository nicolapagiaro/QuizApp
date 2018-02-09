package com.pagiaro.quizapp.Util;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pagiaro.quizapp.Util.Util;

import org.json.JSONException;

import java.io.Serializable;

/**
 * Classe che memorizza il risultato ed i dati di una sessione di gioco
 * Created by nicola on 29/03/17.
 */

public class ResultNotUploaded implements Serializable {
    private int id;
    private int punteggio;
    private String tempo;
    private String data;
    private String categoria;
    private boolean isSaved = false;

    /**
     * Costruttore vuoto
     */
    public ResultNotUploaded() {}

    /**
     * Costruttore completo
     * @param id        id del giocatore
     * @param punteggio della sessione
     * @param tempo     tempo impiegato per il quiz
     * @param data      data dello svolgimento del quiz
     */
    public ResultNotUploaded(int id, int punteggio, String tempo, String data, String categoria) {
        this.id = id;
        this.punteggio = punteggio;
        this.tempo = tempo;
        this.data = data;
        this.categoria = categoria;
    }


    // getter e setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPunteggio() {
        return punteggio;
    }

    public void setPunteggio(int punteggio) {
        this.punteggio = punteggio;
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    /**
     * Metodo che salva il risultato se esiste
     */
    public void salva(Context c) {
        if(!isSaved) return;

        // uploado il risultato
        Ion.with(c)
                .load(Util.UPLOAD_RESULT)
                .setBodyParameter("id", String.valueOf(id))
                .setBodyParameter("punteggio", String.valueOf(punteggio))
                .setBodyParameter("tempo", tempo)
                .setBodyParameter("data", data)
                .setBodyParameter("categoria", categoria)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        try {
                            JsonObject obj = result.getAsJsonArray(Util.JSON_RESULT).get(0).getAsJsonObject();
                            if (obj.get(Util.JSON_SUCCESS_LABEL).getAsInt() != 1) {
                                throw new JSONException("General erro");
                            }
                        } catch (JSONException ex) {}
                    }
                });
    }
}


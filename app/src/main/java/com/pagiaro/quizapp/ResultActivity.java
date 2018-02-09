package com.pagiaro.quizapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.makeramen.roundedimageview.RoundedImageView;
import com.pagiaro.quizapp.Util.Domande;
import com.pagiaro.quizapp.Util.ResultNotUploaded;
import com.pagiaro.quizapp.Util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.pagiaro.quizapp.Util.Util.DARK_THEME_LABEL;
import static com.pagiaro.quizapp.Util.Util.EXTRA_CATEGORIA;
import static com.pagiaro.quizapp.Util.Util.EXTRA_DOMANDE;
import static com.pagiaro.quizapp.Util.Util.EXTRA_NOME_CATEORIA;
import static com.pagiaro.quizapp.Util.Util.EXTRA_TEMPO;
import static com.pagiaro.quizapp.Util.Util.JSON_RESULT;
import static com.pagiaro.quizapp.Util.Util.JSON_SUCCESS_LABEL;
import static com.pagiaro.quizapp.Util.Util.QUIZ_CATEGORIA_LABEL;
import static com.pagiaro.quizapp.Util.Util.QUIZ_PUNTEGGIO_LABEL;
import static com.pagiaro.quizapp.Util.Util.QUIZ_TEMPO_LABEL;
import static com.pagiaro.quizapp.Util.Util.UTENTE_ID_LABEL;


public class ResultActivity extends AppCompatActivity {
    private static final int QUIZ_PASSED_RESULT = 6;
    private static AlertDialog dialog;
    private Domande domande;
    private String tempo;
    private int ris;
    private boolean isUploading = true;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // controllo il tema dell'app
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPrefs.getBoolean(DARK_THEME_LABEL, false))
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // prendo i riferimenti alle view
        TextView punteggio = (TextView) findViewById(R.id.punteggio);
        TextView tv_tempo = (TextView) findViewById(R.id.tempo);
        TextView categoria = (TextView) findViewById(R.id.categoriaResult);
        RoundedImageView roundedImageView = (RoundedImageView) findViewById(R.id.result_image);


        // prendo gli extra
        Bundle b = getIntent().getExtras();
        tempo = b.getString(EXTRA_TEMPO);
        domande = (Domande) b.getSerializable(EXTRA_DOMANDE);
        ris = domande.getRisultato();
        String nomeCategoria = b.getString(EXTRA_NOME_CATEORIA);
        String testoTempo = convertiTempo(tempo, getApplicationContext());

        // mostro le stringhe
        punteggio.setText(getString(R.string.ris) + " " + ris);
        categoria.setText(nomeCategoria);
        tv_tempo.setText(getString(R.string.tempo) + " " + testoTempo);

        // carico il quadro delle risposte errate/giuste
        loadRes();

        // Cambio l'immagine
        if (ris >= QUIZ_PASSED_RESULT) {
            roundedImageView.setImageResource(R.drawable.banner_test_passed);
        } else {
            roundedImageView.setImageResource(R.drawable.banner_test_not_passed);
        }

    }

    /**
     * Metodo per caricare le view
     */
    private void loadRes() {
        ImageView[] listaImg = new ImageView[10];
        TextView[] listaNumeri = new TextView[10];

        // immagini
        listaImg[0] = (ImageView) findViewById(R.id.imgErrori1);
        listaImg[1] = (ImageView) findViewById(R.id.imgErrori2);
        listaImg[2] = (ImageView) findViewById(R.id.imgErrori3);
        listaImg[3] = (ImageView) findViewById(R.id.imgErrori4);
        listaImg[4] = (ImageView) findViewById(R.id.imgErrori5);
        listaImg[5] = (ImageView) findViewById(R.id.imgErrori6);
        listaImg[6] = (ImageView) findViewById(R.id.imgErrori7);
        listaImg[7] = (ImageView) findViewById(R.id.imgErrori8);
        listaImg[8] = (ImageView) findViewById(R.id.imgErrori9);
        listaImg[9] = (ImageView) findViewById(R.id.imgErrori10);

        // numeri
        listaNumeri[0] = (TextView) findViewById(R.id.numeroD1);
        listaNumeri[1] = (TextView) findViewById(R.id.numeroD2);
        listaNumeri[2] = (TextView) findViewById(R.id.numeroD3);
        listaNumeri[3] = (TextView) findViewById(R.id.numeroD4);
        listaNumeri[4] = (TextView) findViewById(R.id.numeroD5);
        listaNumeri[5] = (TextView) findViewById(R.id.numeroD6);
        listaNumeri[6] = (TextView) findViewById(R.id.numeroD7);
        listaNumeri[7] = (TextView) findViewById(R.id.numeroD8);
        listaNumeri[8] = (TextView) findViewById(R.id.numeroD9);
        listaNumeri[9] = (TextView) findViewById(R.id.numeroD10);

        // riempo le view
        for (int i = 0; i < domande.getCount(); i++) {
            listaNumeri[i].setText(String.valueOf(i + 1));
            boolean isGiusta;
            if (isGiusta = domande.getDomanda(i).isCorrect(domande.getRisposte()[i]))
                listaImg[i].setImageResource(R.drawable.risposta_giusta);
            else
                listaImg[i].setImageResource(R.drawable.risposta_sbagliata);
            addOnClickListener(i, listaImg[i], isGiusta);
        }
    }

    /**
     * Metodo per verificare la connessione internet
     *
     * @return
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Metodo che aggiunge un listener alle domande cosi da aprire il dialog
     *
     * @param position posizione della domanda
     * @param itemView oggetto a cui aggiungere il listener
     */
    private void addOnClickListener(final int position, View itemView, final boolean isGiusta) {
        itemView.setOnClickListener(new View.OnClickListener() {
            // costruisco il dialog e lo faccio vedere
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(ResultActivity.this);
                View dialogView = inflater.inflate(R.layout.question_result_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this,
                        (sharedPrefs.getBoolean(DARK_THEME_LABEL, false) ? R.style.DialogDark_Alert : R.style.DialogLight_Alert));
                builder.setView(dialogView);
                builder.setTitle(getString(R.string.title_dialogDomanda) + " " + String.valueOf(position + 1));
                builder.setPositiveButton(getString(R.string.chiudi_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                TextView txt = (TextView) dialogView.findViewById(R.id.domandaDialog);
                txt.setText(domande.getDomanda(position).getDomanda());
                txt = (TextView) dialogView.findViewById(R.id.rispostaDialog);
                if (isGiusta) {
                    txt.setTextColor(getResources().getColor(R.color.rispostaCorretta));
                    txt.setText(getString(R.string.positiva_dialogDomanda));
                } else {
                    txt.setTextColor(getResources().getColor(R.color.rispostaSbagliata));
                    txt.setText(getString(R.string.negativa_dialogDomanda));
                }
                dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!isUploading) {
            super.onBackPressed();
            Util.isResultActivity =  true;
        } else
            Toast.makeText(getApplicationContext(), getString(R.string.backHome_err), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        final String idCat = getIntent().getStringExtra(EXTRA_CATEGORIA);
        super.onPostCreate(savedInstanceState);
        if (!isNetworkAvailable()) {
            isUploading = false;
            new AlertDialog.Builder(new ContextThemeWrapper(this,
                    (sharedPrefs.getBoolean(DARK_THEME_LABEL, false) ? R.style.DialogDark_Alert : R.style.DialogLight_Alert)))
                    .setTitle(R.string.dialog_nointernet_header)
                    .setMessage(R.string.dialog_nointernetresult)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setCancelable(false)
                    .show();
            // salvo il risultato
            Util.salvaRisultato(getApplicationContext(), sharedPrefs, ris, tempo, idCat);
        } else {
            isUploading = true;

            // uploado il risultato
            Ion.with(getApplicationContext())
                    .load(Util.UPLOAD_RESULT)
                    .setBodyParameter(UTENTE_ID_LABEL, String.valueOf(sharedPrefs.getInt("id", 0)))
                    .setBodyParameter(QUIZ_PUNTEGGIO_LABEL, String.valueOf(ris))
                    .setBodyParameter(QUIZ_TEMPO_LABEL, tempo)
                    .setBodyParameter(QUIZ_CATEGORIA_LABEL, getIntent().getStringExtra(EXTRA_CATEGORIA))
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            JsonObject obj = result.getAsJsonArray(JSON_RESULT).get(0).getAsJsonObject();
                            if (obj == null) {
                                Toast.makeText(getApplicationContext(), getString(R.string.salvataggioErr), Toast.LENGTH_SHORT).show();
                                Util.salvaRisultato(getApplicationContext(), sharedPrefs, ris, tempo, idCat);
                            }
                            else if(obj.get(JSON_SUCCESS_LABEL).getAsInt() != 1) {
                                Toast.makeText(getApplicationContext(), getString(R.string.salvataggioErr), Toast.LENGTH_SHORT).show();
                                Util.salvaRisultato(getApplicationContext(), sharedPrefs, ris, tempo, idCat);
                            }
                            isUploading = false;
                        }
                    });
        }
    }

    /**
     * Metodo per convertire la stringa del tempo impiegato
     *
     * @param t stringa di partenza
     * @return stringa scritta bene
     */
    public String convertiTempo(String t, Context c) {
        String finall = "";
        String[] temp = t.split(":");
        String minTemp = temp[0].replaceAll("0", "");
        minTemp = minTemp.trim();
        if (!minTemp.equals("")) {
            finall += minTemp;
            if (minTemp.equals("1"))
                finall += " " + c.getString(R.string.minuto);
            else
                finall += " " + c.getString(R.string.minuti);
            finall += " e ";
        }
        minTemp = temp[1].replaceAll("0", "");
        minTemp = minTemp.trim();
        if (!minTemp.equals("")) {
            finall += minTemp;
            if (minTemp.equals("1"))
                finall += " " + c.getString(R.string.secondo);
            else
                finall += " " + c.getString(R.string.secondi);
        }

        return finall;
    }
}

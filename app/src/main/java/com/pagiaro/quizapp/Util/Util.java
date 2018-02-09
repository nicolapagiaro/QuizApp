package com.pagiaro.quizapp.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.pagiaro.quizapp.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Classe per organizzare i link per le operazioni e la comunicazione con il database
 * Created by nicola on 27/01/17.
 */

public class Util {
    public static final String ARGS_BUNDLE_FRAGMENT = "data";
    public static final String DARK_THEME_LABEL = "darkTheme";
    public static final String NULL_LABEL = "null";
    public static final String SHARED_LOGGATO_LABEL = "loggato";
    public static boolean isResultActivity = false;

    /**
     * Costanti scambio di EXTRAS tra:
     * - Homepage e QuizActivity
     * - QuizActivity e ResultActivity
     */
    public static final String EXTRA_DOMANDE = "domande";
    public static final String EXTRA_NOME_CATEORIA = "nomeCategoria";
    public static final String EXTRA_CATEGORIA = "categoria";
    public static final String EXTRA_TEMPO = "tempo";

    /**
     * Costanti scambia di EXTRAS tra:
     * - Classifica e MostraImmaginiActivity
     */
    public static final String EXTRA_USERNAME_GIOCATORE = "username";
    public static final String EXTRA_IMMAGINE_GIOCATORE = "immagine";

    /**
     * Links alle risorse del server
     */
    public static final String DOWNLOAD_DATA = "http://quizapp.000webhostapp.com/scaricaTutto.php";
    public static final String UPLOADIMAGE = "http://quizapp.000webhostapp.com/upload_image.php";
    public static final String USERIMAGEPATH = "http://quizapp.000webhostapp.com/imagesUtenti/";
    public static final String REGISTER_URL = "http://quizapp.000webhostapp.com/registerApp.php";
    public static final String UPLOAD_RESULT = "http://quizapp.000webhostapp.com/uploadResult.php";
    public static final String LOGIN_URL = "http://quizapp.000webhostapp.com/loginApp.php";
    public static final String URL_IMG = "http://quizapp.000webhostapp.com/imagesApp/";
    public static final String CATIMAGEPATH = "http://quizapp.000webhostapp.com/imagesCategorie/";
    public static final String DOWNLOAD_DOMANDE = "http://quizapp.000webhostapp.com/scaricaDomande.php";
    public static final String FILE_NAME = "salvataggi.bin";

    /**
     * Costanti riguardo i dati nella fase di registrazione dell'utente
     */
    public static final String REGISTER_USERNAME_LABEL = "username";
    public static final String REGISTER_PASSWORD_LABEL = "pssw";
    public static final String REGISTER_NOME_LABEL = "nome";
    public static final String REGISTER_COGNOME_LABEL = "cognome";
    public static final String REGISTER_DATA_LABEL = "data";

    /**
     * Costanti per prendere gli array/oggetti dal JSON
     */
    public static final String JSON_RESULT = "result";
    public static final String JSON_SUCCESS_LABEL = "success";
    public static final String JSON_INFO_UTENTE_LABEL = "infoUtente";
    public static final String JSON_RISULTATI_LABEL = "risultati";
    public static final String JSON_ARRAY_DOMANDE_LABEL = "domande";
    public static final String JSON_CLASSIFICA_ARRAY_LABEL = "classifica";

    /**
     * Costanti riguardo l'utente
     */
    public static final String UTENTE_ID_LABEL = "id";
    public static final String UTENTE_USERNAME_LABEL = "username";
    public static final String UTENTE_NOME_LABEL = "nome";
    public static final String UTENTE_COGNOME_LABEL = "cognome";
    public static final String UTENTE_DATAN_LABEL = "dataN";
    public static final String UTENTE_IMAGE_LABEL = "image";
    public static final String UTENTE_ISCRITTODAL_LABEL = "iscrittoDal";
    public static final String UTENTE_PUNTEGGIO_LABEL = "punteggio";

    /**
     * Costanti riguardo le giocate recenti dell'utente
     */
    public static final String UTENTE_IDSESSIONE_LABEL = "idSessione";
    public static final String UTENTE_PUNTEGGIO_SESSIONE_LABEL = "punteggio";
    public static final String UTENTE_TEMPO_LABEL = "tempoImpiegato";
    public static final String UTENTE_DATA_LABEL = "dataSessione";
    public static final String UTENTE_CATEGORIASESSIONE_LABEL = "categoria";

    /**
     * Costanti riguardo le categorie
     */
    public static final String CAT_ID_LABEL = "id";
    public static final String CAT_NOME_LABEL = "nomeCategoria";
    public static final String CAT_NUMEROTOT_LABEL = "numeroTot";
    public static final String CAT_IMAGE_LABEL = "image";

    /**
     * Costanti riguardo la classifica
     */
    public static final String CHART_ID_LABEL = "id";
    public static final String CHART_NOME_LABEL = "nome";
    public static final String CHART_COGNOME_LABEL = "cognome";
    public static final String CHART_IMAGE_LABEL = "image";
    public static final String CHART_ISCRITTO_LABEL = "iscrittoDal";
    public static final String CHART_PUNTEGGIO_LABEL = "punteggio";
    public static final String CHART_USERNAME_LABEL = "username";

    /**
     * Costantanti riguardo le domande
     */
    public static final String DOMANDE_ID_LABEL = "id";
    public static final String DOMANDE_DOMANDA_LABEL = "domanda";
    public static final String DOMANDE_RG_LABEL = "rG";   // risposta giusta
    public static final String DOMANDE_RS1_LABEL = "rS1"; // risposta sbagliata 1
    public static final String DOMANDE_RS2_LABEL = "rS2"; // risposta sbagliata 2
    public static final String DOMANDE_RS3_LABEL = "rS3"; // risposta sbagliata 3
    public static final String DOMANDE_IMMAGINE_LABEL = "immagine";

    /**
     * Costanti riguardo il caricamento del risultato del quiz
     */
    public static final String QUIZ_PUNTEGGIO_LABEL = "punteggio";
    public static final String QUIZ_TEMPO_LABEL = "tempo";
    public static final String QUIZ_CATEGORIA_LABEL = "categoria";



    /**
     * Metodo che salva il risultato localmente
     * @param c context
     * @param sharedPreferences shared preferences
     * @param risultato risultato (int)
     * @param tempo tempo impiegato (string)
     * @param categora id della categoria (i
     */
    public static void salvaRisultato(Context c, SharedPreferences sharedPreferences, int risultato,
                                String tempo, String categora) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
        Date date = new Date();
        try {
            FileOutputStream fos = c.openFileOutput(Util.FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream streamOut = new ObjectOutputStream(fos);
            streamOut.writeObject(
                    new ResultNotUploaded(sharedPreferences.getInt(UTENTE_ID_LABEL, 0), risultato, tempo, dateFormat.format(date), categora)
            );
            streamOut.close();
        } catch (IOException e) {}
    }

}

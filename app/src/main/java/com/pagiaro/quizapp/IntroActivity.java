package com.pagiaro.quizapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pagiaro.quizapp.Util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import static com.pagiaro.quizapp.Util.Util.*;


public class IntroActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private TextInputEditText username, pssw, nome, cognome, data;
    private TextInputLayout layout_username, layout_pssw, layout_data, layout_nome, layout_cognome;
    private Button register;
    private TextView login;
    private ProgressDialog dialog;
    private String dataTxt = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // controllo se l'utente non è loggato
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean(SHARED_LOGGATO_LABEL, false)) {
            Intent splash = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(splash);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        username = (TextInputEditText) findViewById(R.id.nomeUtente);
        nome = (TextInputEditText) findViewById(R.id.nome);
        cognome = (TextInputEditText) findViewById(R.id.cognome);
        data = (TextInputEditText) findViewById(R.id.data);
        pssw = (TextInputEditText) findViewById(R.id.password);
        login = (TextView) findViewById(R.id.login_label);
        register = (Button) findViewById(R.id.register);
        layout_username = (TextInputLayout) findViewById(R.id.input_username_label);
        layout_pssw = (TextInputLayout) findViewById(R.id.input_password_label);
        layout_nome = (TextInputLayout) findViewById(R.id.input_nome_label);
        layout_cognome = (TextInputLayout) findViewById(R.id.input_cognome_label);
        layout_data = (TextInputLayout) findViewById(R.id.input_data_label);

        data.setFocusableInTouchMode(false);
        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd;
                String d;
                if((d = dataTxt).equals(""))   {
                    dpd = DatePickerDialog.newInstance(
                            IntroActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                }
                else {
                    String[] d2 = d.split("\\-");
                    dpd = DatePickerDialog.newInstance(
                            IntroActivity.this,
                            Integer.parseInt(d2[0]),
                            Integer.parseInt(d2[1])-1,
                            Integer.parseInt(d2[2])
                    );
                }
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getApplicationContext(), register.getWindowToken());
                initLogin();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getApplicationContext(), login.getWindowToken());
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                finish();
            }
        });

        if(!isNetworkAvailable()) {
            new AlertDialog.Builder(new ContextThemeWrapper(this,
                    (sharedPrefs.getBoolean("darkTheme", false) ? R.style.DialogDark_Alert : R.style.DialogLight_Alert)))
                    .setTitle(R.string.dialog_nointernet_header)
                    .setMessage(R.string.dialog_nointernet)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * Metodo per chiudere la tastiera
     *
     * @param c           context
     * @param windowToken window token
     */
    public void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    /**
     * Validate Login form and authenticate.
     */
    public void initLogin() {
        String str_username = username.getText().toString().trim();
        String str_pssw = pssw.getText().toString().trim();
        String str_nome = nome.getText().toString().trim();
        String str_cognome = cognome.getText().toString().trim();
        String str_data = data.getText().toString();

        boolean cancelLogin = false;

        // controllo la password
        if (!isPasswordValid(str_pssw)) {
            layout_pssw.setError(getString(R.string.errore1));
            cancelLogin = true;
        } else
            layout_pssw.setErrorEnabled(false);

        // controllo l'username
        if (!isUsernameValid(str_username)) {
            layout_username.setError(getString(R.string.errore4));
            cancelLogin = true;
        } else {
            layout_username.setErrorEnabled(false);
        }

        // controllo il nome, se c'è
        if (!isValid(str_nome)) {
            layout_nome.setError(getString(R.string.errore2));
            cancelLogin = true;
        } else
            layout_username.setErrorEnabled(false);

        // controllo il cognome, se c'è
        if (!isValid(str_cognome)) {
            layout_cognome.setError(getString(R.string.errore3));
            cancelLogin = true;
        } else
            layout_cognome.setErrorEnabled(false);

        // controllo la data, se c'è
        if (TextUtils.isEmpty(str_data)) {
            layout_data.setError(getString(R.string.errore5));
            cancelLogin = true;
        } else
            layout_data.setErrorEnabled(false);

        // se è tutto valido lancio il register
        if (!cancelLogin) {
            dialog = ProgressDialog.show(IntroActivity.this, getString(R.string.dialog_content_register),
                    getString(R.string.dialog_waiting), true);

            // register
            Ion.with(getApplicationContext())
                    .load(Util.REGISTER_URL)
                    .setBodyParameter(REGISTER_USERNAME_LABEL, str_username)
                    .setBodyParameter(REGISTER_PASSWORD_LABEL, str_pssw)
                    .setBodyParameter(REGISTER_NOME_LABEL, str_nome)
                    .setBodyParameter(REGISTER_COGNOME_LABEL, str_cognome)
                    .setBodyParameter(REGISTER_DATA_LABEL, dataTxt)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            JsonObject obj = result.getAsJsonObject(JSON_RESULT);
                            if(obj != null) {
                                if (obj.get(JSON_SUCCESS_LABEL).getAsInt() == 1) {
                                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor edit = sharedPrefs.edit();
                                    edit.putBoolean(SHARED_LOGGATO_LABEL, true);
                                    edit.putInt(UTENTE_ID_LABEL, obj.get(UTENTE_ID_LABEL).getAsInt());
                                    edit.putString(UTENTE_USERNAME_LABEL, username.getText().toString());
                                    edit.putString(UTENTE_NOME_LABEL, nome.getText().toString());
                                    edit.putString(UTENTE_COGNOME_LABEL, cognome.getText().toString());
                                    edit.putString(UTENTE_DATAN_LABEL, dataTxt);
                                    edit.putString(UTENTE_IMAGE_LABEL, "");
                                    edit.putInt(UTENTE_PUNTEGGIO_LABEL, 0);
                                    edit.apply();
                                    dialog.dismiss();
                                    Intent main = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(main);
                                    finish();
                                } else {
                                    dialog.dismiss();
                                    layout_username.setError(getString(R.string.errore_register));
                                }
                            } else {
                                dialog.dismiss();
                            }
                        }
                    });
        }
    }

    /**
     * Metodo per verificare la connessione internet
     * @return
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     *
     */
    private boolean isUsernameValid(String username) {
        //add your own logic
        return username.length() > 4;
    }

    /**
     *
     */
    private boolean isPasswordValid(String password) {
        //add your own logic
        return password.length() >= 6;
    }

    /**
     *
     */
    private boolean isValid(String strr) {
        //add your own logic
        return strr.length() >= 2;
    }

    /**
     * Listener del dialog date picker
     * @param view
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        data.setText(dayOfMonth + "/" + (monthOfYear+1) + "/" + year);
        if(monthOfYear < 9)
            dataTxt = year + "-0" + (monthOfYear+1) + "-" + dayOfMonth;
        else
            dataTxt = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
        System.out.println(dataTxt);
    }
}


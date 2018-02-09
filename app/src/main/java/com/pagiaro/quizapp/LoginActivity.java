package com.pagiaro.quizapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pagiaro.quizapp.Util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import static com.pagiaro.quizapp.Util.Util.*;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText username, pssw;
    private TextInputLayout layout_username, layout_pssw;
    private Button login;
    private ProgressDialog dialog;;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (TextInputEditText) findViewById(R.id.login_nomeUtente);
        pssw = (TextInputEditText) findViewById(R.id.login_password);
        login = (Button) findViewById(R.id.login);
        layout_username = (TextInputLayout) findViewById(R.id.login_username_label);
        layout_pssw = (TextInputLayout) findViewById(R.id.login_password_label);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getApplicationContext(), login.getWindowToken());
                initLogin();
            }
        });

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent register = new Intent(getApplicationContext(), IntroActivity.class);
        startActivity(register);
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        finish();
    }

    /**
     * Validate Login form and authenticate.
     */
    public void initLogin() {
        String str_username = username.getText().toString().trim();
        String str_pssw = pssw.getText().toString().trim();

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
        } else
            layout_username.setErrorEnabled(false);

        // se è tutto valido lancio il login
        if (!cancelLogin) {
            dialog = ProgressDialog.show(LoginActivity.this, getString(R.string.dialog_content_login),
                    getString(R.string.dialog_waiting), true);

            // login
            Ion.with(getApplicationContext())
                    .load(Util.LOGIN_URL)
                    .setBodyParameter(REGISTER_USERNAME_LABEL, str_username)
                    .setBodyParameter(REGISTER_PASSWORD_LABEL, str_pssw)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            try {
                                JsonObject obj = result.getAsJsonObject(JSON_RESULT);
                                if (obj.get(JSON_SUCCESS_LABEL).getAsInt() == 1) {
                                    // login avvenuto con successo
                                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor edit = sharedPrefs.edit();
                                    edit.putBoolean(SHARED_LOGGATO_LABEL, true);
                                    edit.putInt(UTENTE_ID_LABEL, obj.get(UTENTE_ID_LABEL).getAsInt());
                                    edit.putString(UTENTE_USERNAME_LABEL, username.getText().toString());
                                    edit.putString(UTENTE_NOME_LABEL, obj.get(UTENTE_NOME_LABEL).getAsString());
                                    edit.putString(UTENTE_COGNOME_LABEL, obj.get(UTENTE_COGNOME_LABEL).getAsString());
                                    edit.putString(UTENTE_DATAN_LABEL, obj.get(UTENTE_DATAN_LABEL).getAsString());
                                    edit.putString(UTENTE_IMAGE_LABEL, obj.get(UTENTE_IMAGE_LABEL).getAsString());
                                    String p = obj.get(UTENTE_PUNTEGGIO_LABEL).getAsString();
                                    if(p.equalsIgnoreCase(NULL_LABEL))
                                        edit.putInt(UTENTE_PUNTEGGIO_LABEL, 0);
                                    else
                                        edit.putInt(UTENTE_PUNTEGGIO_LABEL, Integer.parseInt(p));
                                    edit.apply();

                                    dialog.dismiss();
                                    Intent splash = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(splash);
                                    finish();
                                } else {
                                    dialog.dismiss();
                                    layout_username.setError(getString(R.string.errore_login));
                                    throw new JSONException("General erro");
                                }
                            }catch (JSONException ex) {
                                dialog.dismiss();
                            }
                        }
                    });
        }
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
}

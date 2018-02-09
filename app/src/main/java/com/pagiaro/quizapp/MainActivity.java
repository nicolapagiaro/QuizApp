package com.pagiaro.quizapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.mvc.imagepicker.ImagePicker;
import com.pagiaro.quizapp.Fragment.Classifica;
import com.pagiaro.quizapp.Util.Dati;
import com.pagiaro.quizapp.Fragment.Homepage;
import com.pagiaro.quizapp.Fragment.ProfiloUtente;
import com.pagiaro.quizapp.Util.ResultNotUploaded;
import com.pagiaro.quizapp.Util.Util;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.pagiaro.quizapp.R.id.username;
import static com.pagiaro.quizapp.Util.Util.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final int REQUEST_CODE = 20;
    private final int MAX_LOAD_RETRIES = 3;
    private SharedPreferences shared;
    private Dati data = new Dati();
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private CircleImageView userImage;
    private ProgressBar pbMain;
    private Switch sw;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean isLoading = false;
    private int loadCount;
    private int fragmentActiveId = R.id.nav_homepage; //default


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // imposto il tema
        shared = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (shared.getBoolean(DARK_THEME_LABEL, false))
            setTheme(R.style.AppThemeDark_NoActionBar);
        else
            setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // riferimenti delle view
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pbMain = (ProgressBar) findViewById(R.id.progBar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        sw = (Switch) navigationView.getMenu()
                .findItem(R.id.nav_theme).getActionView().findViewById(R.id.switch_theme);


        // imposto il drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_homepage);

        // controllo lo switch del dark theme
        sw.setChecked(shared.getBoolean(DARK_THEME_LABEL, false));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                shared.edit().putBoolean(DARK_THEME_LABEL, isChecked).apply();
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                finish();
            }
        });

        if (!isNetworkAvailable()) {
            loadCount = 4;
            setLoadingFailed();
        } else {
            loadCount = 0;
            // guardo se c'è un risultato salvato da caricare
            leggiCarica();
            // carico tutto
            caricaHome(shared);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(Util.isResultActivity) {
            // guardo se c'è un risultato salvato da caricare
            leggiCarica();
            // carico tutto
            caricaHome(shared);
            Util.isResultActivity = false;
        }
    }

    /**
     * Metodo per caricare tutti i dati
     * @param shared
     */
    private void caricaHome(final SharedPreferences shared) {
        setHomeLoading();
        Ion.with(getApplicationContext())
                .load(Util.DOWNLOAD_DATA)
                .setBodyParameter(UTENTE_ID_LABEL, String.valueOf(shared.getInt(UTENTE_ID_LABEL, 0)))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {

                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (result != null) {
                            JsonObject JSONlista = result.getAsJsonObject(JSON_RESULT);

                            if (JSONlista.get(JSON_SUCCESS_LABEL).getAsInt() == 1) {
                                JsonArray utente = JSONlista.getAsJsonArray(JSON_INFO_UTENTE_LABEL);
                                JsonArray risultatiUtenti = JSONlista.getAsJsonArray(JSON_RISULTATI_LABEL);
                                JsonArray domande = JSONlista.getAsJsonArray(JSON_ARRAY_DOMANDE_LABEL);
                                JsonArray classifica = JSONlista.getAsJsonArray(JSON_CLASSIFICA_ARRAY_LABEL);

                                // informazioni utente salvate nelle prefecereces
                                ArrayList<HashMap<String, String>> u = new ArrayList<>();
                                for (int i = 0; i < utente.size(); i++) {
                                    JsonObject obj = utente.get(i).getAsJsonObject();
                                    SharedPreferences.Editor edit = shared.edit();
                                    edit.putString(UTENTE_USERNAME_LABEL, obj.get(UTENTE_USERNAME_LABEL).getAsString());
                                    edit.putString(UTENTE_NOME_LABEL, obj.get(UTENTE_NOME_LABEL).getAsString());
                                    edit.putString(UTENTE_COGNOME_LABEL, obj.get(UTENTE_COGNOME_LABEL).getAsString());
                                    edit.putString(UTENTE_DATAN_LABEL, obj.get(UTENTE_DATAN_LABEL).getAsString());
                                    edit.putString(UTENTE_IMAGE_LABEL, obj.get(UTENTE_IMAGE_LABEL).getAsString());
                                    edit.putString(UTENTE_ISCRITTODAL_LABEL, obj.get(UTENTE_ISCRITTODAL_LABEL).getAsString());
                                    String p = obj.get(UTENTE_PUNTEGGIO_LABEL).getAsString();
                                    if (p.equalsIgnoreCase(NULL_LABEL))
                                        edit.putInt(UTENTE_PUNTEGGIO_LABEL, 0);
                                    else
                                        edit.putInt(UTENTE_PUNTEGGIO_LABEL, Integer.parseInt(p));
                                    edit.apply();

                                }
                                // risultati recenti dell'utente
                                for (int i = 0; i < risultatiUtenti.size(); i++) {
                                    HashMap<String, String> temp = new HashMap<>();
                                    JsonObject obj = risultatiUtenti.get(i).getAsJsonObject();
                                    temp.put(UTENTE_IDSESSIONE_LABEL, obj.get(UTENTE_IDSESSIONE_LABEL).getAsString());
                                    temp.put(UTENTE_PUNTEGGIO_SESSIONE_LABEL, obj.get(UTENTE_PUNTEGGIO_SESSIONE_LABEL).getAsString());
                                    temp.put(UTENTE_TEMPO_LABEL, obj.get(UTENTE_TEMPO_LABEL).getAsString());
                                    temp.put(UTENTE_DATA_LABEL, obj.get(UTENTE_DATA_LABEL).getAsString());
                                    temp.put(UTENTE_CATEGORIASESSIONE_LABEL, obj.get(UTENTE_CATEGORIASESSIONE_LABEL).getAsString());
                                    u.add(temp);

                                }
                                data.setUtente(u);

                                // categorie
                                ArrayList<HashMap<String, String>> d = new ArrayList<>();
                                for (int i = 0; i < domande.size(); i++) {
                                    JsonObject obj = domande.get(i).getAsJsonObject();
                                    HashMap<String, String> temp = new HashMap<>();
                                    temp.put(CAT_ID_LABEL, obj.get(CAT_ID_LABEL).getAsString());
                                    temp.put(CAT_NOME_LABEL, obj.get(CAT_NOME_LABEL).getAsString());
                                    temp.put(CAT_NUMEROTOT_LABEL, obj.get(CAT_NUMEROTOT_LABEL).getAsString());
                                    temp.put(CAT_IMAGE_LABEL, obj.get(CAT_IMAGE_LABEL).getAsString());
                                    d.add(temp);
                                }
                                data.setCategorie(d);

                                // classifica
                                ArrayList<HashMap<String, String>> c = new ArrayList<>();
                                for (int i = 0; i < classifica.size(); i++) {
                                    JsonObject obj = classifica.get(i).getAsJsonObject();
                                    HashMap<String, String> temp = new HashMap<>();
                                    temp.put(CHART_ID_LABEL, obj.get(CHART_ID_LABEL).getAsString());
                                    temp.put(CHART_NOME_LABEL, obj.get(CHART_NOME_LABEL).getAsString());
                                    temp.put(CHART_COGNOME_LABEL, obj.get(CHART_COGNOME_LABEL).getAsString());
                                    temp.put(CHART_IMAGE_LABEL, obj.get(CHART_IMAGE_LABEL).getAsString());
                                    temp.put(CHART_ISCRITTO_LABEL, obj.get(CHART_ISCRITTO_LABEL).getAsString());
                                    temp.put(CHART_PUNTEGGIO_LABEL, obj.get(CHART_PUNTEGGIO_LABEL).getAsString());
                                    temp.put(CHART_USERNAME_LABEL, obj.get(CHART_USERNAME_LABEL).getAsString());
                                    c.add(temp);
                                }
                                data.setClassifica(c);
                                // carico l'header
                                loadHeader();
                                setHomeLoaded();
                                return;
                            }
                        }

                        loadCount++;
                        if(loadCount <= MAX_LOAD_RETRIES) {
                            caricaHome(shared);
                            if(loadCount == 1)
                                Toast.makeText(getApplicationContext(), getString(R.string.slowLoading), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            setLoadingFailed();
                        }
                    }
                });
    }

    /**
     * La home si sta caricando
     */
    private void setHomeLoading() {
        // nascondo le view di errore
        findViewById(R.id.tv_error).setVisibility(View.GONE);
        findViewById(R.id.imageErr).setVisibility(View.GONE);
        findViewById(R.id.btn_retry).setVisibility(View.GONE);


        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        pbMain.setVisibility(View.VISIBLE);
    }

    /**
     * La home è caricata
     */
    private void setHomeLoaded() {
        // nascondo la progressbar
        pbMain.setVisibility(View.INVISIBLE);

        // imposto il fragment
        Fragment fragment = Homepage.newInstance(data.getCategorie());
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // unlocko il drawer
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        // non ta più caricando
        setLoading(false);
    }

    /**
     * Errore nel caricamento
     */
    private void setLoadingFailed() {
        // cambio l'immagine a seconda del tema
        if(shared.getBoolean(DARK_THEME_LABEL, false)) {
            ImageView imageView = (ImageView) findViewById(R.id.imageErr);
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_error_outline_white_48dp));
        }

        // nascondo la progressbar
        pbMain.setVisibility(View.GONE);

        // mostro le altre view
        findViewById(R.id.tv_error).setVisibility(View.VISIBLE);
        findViewById(R.id.imageErr).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_retry).setVisibility(View.VISIBLE);


        // non ta più caricando
        setLoading(false);
    }

    /**
     * Metodo che legge il risultato salvati
     */
    private void leggiCarica() {
        try {
            // lettura
            FileInputStream fis = openFileInput(Util.FILE_NAME);
            ObjectInputStream stream = new ObjectInputStream(fis);
            ResultNotUploaded r = (ResultNotUploaded) stream.readObject();
            if (r != null)
                r.salva(getApplicationContext());
            stream.close();
            fis.close();

            // scrittura
            FileOutputStream fos = openFileOutput(Util.FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream streamOut = new ObjectOutputStream(fos);
            streamOut.writeObject(null);
            streamOut.close();
        } catch (IOException | ClassNotFoundException e) {}
    }

    /**
     * Loading the navigationview and the homepage
     */
    private void loadHeader() {
        View holder = navigationView.getHeaderView(0);
        userImage = (CircleImageView) holder.findViewById(R.id.userImage);
        TextView userNick = (TextView) holder.findViewById(R.id.userNickname);
        TextView userName = (TextView) holder.findViewById(username);
        String nick, name;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        nick = sharedPrefs.getString(UTENTE_USERNAME_LABEL, "");
        name = sharedPrefs.getString(UTENTE_NOME_LABEL, "") + " " + sharedPrefs.getString(UTENTE_COGNOME_LABEL, "");
        String s = sharedPrefs.getString(UTENTE_IMAGE_LABEL, "");
        if (s.equals(""))
            Picasso.with(getApplicationContext())
                    .load(R.drawable.default_user)
                    .into(userImage);
        else {
            Picasso.with(getApplicationContext())
                    .load(Util.USERIMAGEPATH + s)
                    .into(userImage);
        }

        userNick.setText(nick);
        userName.setText(name);
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!isLoading()) {
            FragmentManager fm = getSupportFragmentManager();
            if (!(fragmentActiveId == R.id.nav_homepage)) {
                fm.beginTransaction().replace(R.id.flContent, Homepage.newInstance(data.getCategorie())).commit();
                navigationView.setCheckedItem(R.id.nav_homepage);
            } else {
                if (doubleBackToExitPressedOnce) super.onBackPressed();

                doubleBackToExitPressedOnce = true;
                Toast.makeText(this, getString(R.string.doubleClickExit_msg), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        } else
            super.onBackPressed();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        boolean isFrag;

        if (id == R.id.nav_homepage) {
            try {
                fragment = Homepage.newInstance(data.getCategorie());
                fragmentActiveId = R.id.nav_homepage;
            } catch (Exception e) {
                e.printStackTrace();
            }
            isFrag = true;
        } else if (id == R.id.nav_profilo) {
            try {
                fragment = ProfiloUtente.newInstance(data.getUtente());
                fragmentActiveId = R.id.nav_profilo;
            } catch (Exception e) {
                e.printStackTrace();
            }
            isFrag = true;
        } else if (id == R.id.nav_stats) {
            try {
                fragment = Classifica.newInstance(data.getClassifica());
                fragmentActiveId = R.id.nav_stats;
            } catch (Exception e) {
                e.printStackTrace();
            }
            isFrag = true;
        } else if (id == R.id.nav_about) {
            Intent about = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(about);
            isFrag = false;
        } else if (id == R.id.nav_theme) {
            sw.toggle();
            isFrag = false;
        } else if (id == R.id.nav_esci) {
            // esci come account
            logout();
            isFrag = false;
        } else {
            isFrag = false;
        }

        if (isFrag) {
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    /**
     * Metodo per effettuare il logout
     */
    private void logout() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.clear();
        edit.putBoolean(SHARED_LOGGATO_LABEL, false);
        edit.apply();
        Intent main = new Intent(getApplicationContext(), IntroActivity.class);
        startActivity(main);
        finish();
    }

    /**
     * Metodo per verificare la connessione internet
     * @return true o false
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String fp = getPath(data.getData());

            // genere un nome del file col nome utente ed un numero random
            String oldPhotoName = sharedPrefs.getString(UTENTE_IMAGE_LABEL, "");
            String newPhotoName;
            do {
                newPhotoName = sharedPrefs.getString(UTENTE_USERNAME_LABEL, "") + new Random().nextInt(100);
            } while (oldPhotoName.split("\\.")[0].equals(newPhotoName));

            sharedPrefs.edit().putString(UTENTE_IMAGE_LABEL, newPhotoName + ".jpg").apply(); // aggiorno il nome dell'immagine

            final ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.dialog_uploadPhoto),
                    getString(R.string.dialog_waiting), true);
            final String t = newPhotoName + ".jpg";

            // carico l'immagine
            Ion.with(getApplicationContext())
                    .load(Util.UPLOADIMAGE)
                    .setMultipartParameter("name", newPhotoName)
                    .setMultipartParameter("oldPhoto", oldPhotoName)
                    .setMultipartParameter("id", String.valueOf(sharedPrefs.getInt("id", 0)))
                    .setMultipartFile("image", "application/zip", new File(fp))
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null)
                                // errore
                                Toast.makeText(getApplicationContext(), getString(R.string.errorUploadPhoto), Toast.LENGTH_SHORT).show();
                            else {
                                // tutto apposto, ricarico
                                updateImage(dialog, t);
                            }
                        }
                    });
        }
    }

    /**
     * Metodo per aggiornare l'immagine appena caricata
     *
     * @param dialog
     * @param s
     */
    private void updateImage(ProgressDialog dialog, String s) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, ProfiloUtente.newInstance(data.getUtente())).commit();
        Picasso.with(getApplicationContext())
                .load(Util.USERIMAGEPATH + s)
                .into(userImage);
        dialog.dismiss();
    }

    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();
        cursor = getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    ImagePicker.pickImage(this, getString(R.string.image_picker));
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.noAccessGallery), Toast.LENGTH_SHORT).show();

                break;
        }
    }

    /**
     * @return
     */
    private synchronized boolean isLoading() {
        return isLoading;
    }

    /**
     * @param l
     */
    private synchronized void setLoading(boolean l) {
        isLoading = l;
    }

    /**
     * Metodo per onClick del bottone nella schermata di errore
     * @param v
     */
    public void btnRiprovaCliccato(View v) {
        if(v.getId() == R.id.btn_retry) {
            loadCount = 0;
            // guardo se c'è un risultato salvato da caricare
            leggiCarica();
            // carico tutto
            caricaHome(shared);
        }
    }
}
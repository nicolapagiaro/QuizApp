package com.pagiaro.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pagiaro.quizapp.R;

import static com.pagiaro.quizapp.Util.Util.DARK_THEME_LABEL;

public class AboutActivity extends AppCompatActivity {
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(shared.getBoolean(DARK_THEME_LABEL, false))
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        fab = (FloatingActionButton) findViewById(R.id.fab_share);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "QuizApp");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Gioca su QuizApp! \nhttps://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
    }

}
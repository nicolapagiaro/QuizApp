package com.pagiaro.quizapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import com.pagiaro.quizapp.Util.Util;
import com.squareup.picasso.Picasso;

import static com.pagiaro.quizapp.Util.Util.*;

public class MostraImmaginiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostra_immagini);

        String img = getIntent().getStringExtra(EXTRA_IMMAGINE_GIOCATORE);
        String user = getIntent().getStringExtra(EXTRA_USERNAME_GIOCATORE);
        setTitle(user);
        ImageView imgv = (ImageView) findViewById(R.id.imageViewProfilo);
        Picasso.with(getApplicationContext())
                .load(Util.USERIMAGEPATH + img)
                .into(imgv);

    }
}

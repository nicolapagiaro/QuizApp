package com.pagiaro.quizapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.pagiaro.quizapp.Util.Domande;
import com.pagiaro.quizapp.Util.Util;
import com.squareup.picasso.Picasso;
import static com.pagiaro.quizapp.Util.Util.*;


public class QuizActivity extends AppCompatActivity {
    private final int N_DOMANDE = 10;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static Domande d;
    private TextView domandaCorrente;
    private Button indietro, avanti;
    private ViewPager mViewPager;   //The {@link ViewPager} that will host the section contents.
    private Chronometer chrono;
    private long mLastStopTime;

    /**
     * OnCreate metodo
     * @param savedInstanceState saved
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(shared.getBoolean(DARK_THEME_LABEL, false))
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        //riferimenti
        indietro = (Button) findViewById(R.id.indietro);
        avanti = (Button) findViewById(R.id.avanti);
        mViewPager = (ViewPager) findViewById(R.id.container);
        domandaCorrente = (TextView) findViewById(R.id.n_domanda);
        chrono = (Chronometer) findViewById(R.id.chronometer);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // prendo l'oggetto domande dall'home activity
        d = (Domande) getIntent().getSerializableExtra(EXTRA_DOMANDE);

        //imposto il numero della pagina per la prima volta
        domandaCorrente.setText(mSectionsPagerAdapter.getPageTitle(mViewPager.getCurrentItem()));
        chrono.start();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // selezione quali bottoni mostrare ed il testo
                if (position != 0) {
                    indietro.setVisibility(View.VISIBLE);
                    if (position == N_DOMANDE - 1)
                        avanti.setText(getString(R.string.fine));
                    else
                        avanti.setText(getString(R.string.avanti));
                } else
                    indietro.setVisibility(View.INVISIBLE);

                //modifico il numero di domanda
                domandaCorrente.setText(mSectionsPagerAdapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //listener per il bottone AVANTI/FINE
        avanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() != N_DOMANDE - 1)
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                else {
                    Intent result = new Intent(getApplicationContext(), ResultActivity.class);
                    result.putExtra(EXTRA_CATEGORIA, getIntent().getStringExtra(EXTRA_CATEGORIA));
                    result.putExtra(EXTRA_NOME_CATEORIA, getIntent().getStringExtra(EXTRA_NOME_CATEORIA));
                    result.putExtra(EXTRA_DOMANDE, d);
                    result.putExtra(EXTRA_TEMPO, chrono.getText().toString());
                    startActivity(result);
                    finish();
                }
            }
        });

        //listener per il bottone INDIETRO
        indietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            }
        });
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        new AlertDialog.Builder(new ContextThemeWrapper(this,
                (sharedPrefs.getBoolean(DARK_THEME_LABEL, false) ? R.style.DialogDark_Alert : R.style.DialogLight_Alert)))
                .setMessage(R.string.dialog_content_back_home)
                .setPositiveButton(R.string.dialog_action_back_home, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        QuizActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // fermo il cronometro e salvo l'ultimo orario
        chrono.stop();
        mLastStopTime = SystemClock.elapsedRealtime();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // faccio riparire il cronometro da dove era rimasto
        long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
        chrono.setBase(chrono.getBase() + intervalOnPause);
        chrono.start();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private PlaceholderFragment placeH;

        /**
         * Costruttore parametrico
         *
         * @param fm il manager dei frammenti
         */
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            return placeH.newInstance(position);
        }

        @Override
        public int getCount() {
            return N_DOMANDE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(R.string.n_domanda) + " " + (position + 1) + "/" + N_DOMANDE;
        }
    }

    /**
     * Classe usata per modificare il frammento gestito dal'adapter
     * Created by nicola on 04/10/16.
     */
    public static class PlaceholderFragment extends android.support.v4.app.Fragment {
        private int num;

        /**
         * Costruttore non parametrico
         */
        public PlaceholderFragment() {
            super();
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            num = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * Metodo usato per creare il layout
         * @return
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.progBarQuiz);
            final RoundedImageView img = (RoundedImageView) rootView.findViewById(R.id.immagine);
            TextView domanda = (TextView) rootView.findViewById(R.id.domandaErr);
            RadioButton r1 = (RadioButton) rootView.findViewById(R.id.radio1);
            RadioButton r2 = (RadioButton) rootView.findViewById(R.id.radio2);
            RadioButton r3 = (RadioButton) rootView.findViewById(R.id.radio3);
            RadioButton r4 = (RadioButton) rootView.findViewById(R.id.radio4);
            final RadioGroup r_grp = (RadioGroup) rootView.findViewById(R.id.radio_groupe);

            Domande.Domanda dd = d.getDomanda(num);
            Picasso.with(getContext())
                    .load(Util.URL_IMG + dd.getImmagine())
                    .into(img, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (bar != null) {
                                bar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
            domanda.setText(dd.getDomanda());
            r1.setText(dd.getRisposta(0));
            r2.setText(dd.getRisposta(1));
            r3.setText(dd.getRisposta(2));
            r4.setText(dd.getRisposta(3));

            //listener per i bottoni radio
            r_grp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    int temp = i - R.id.radio1;
                    // aggiungo la risposta
                    d.putRisposta(num, temp);
                }
            });

            return rootView;
        }
    }
}

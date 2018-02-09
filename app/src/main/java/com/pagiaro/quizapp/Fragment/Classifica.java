package com.pagiaro.quizapp.Fragment;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pagiaro.quizapp.MostraImmaginiActivity;
import com.pagiaro.quizapp.R;
import com.pagiaro.quizapp.Util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.pagiaro.quizapp.Util.Util.*;

public class Classifica extends Fragment {
    public static final int PODIO_COUNT = 3;
    private static AlertDialog dialog;
    SharedPreferences sharedPrefs;
    private ArrayList<HashMap<String, String>> dati;


    public Classifica() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment Homepage.
     */
    public static Classifica newInstance(ArrayList<HashMap<String, String>> dati) {
        Classifica f = new Classifica();
        Bundle args = new Bundle();
        args.putSerializable(ARGS_BUNDLE_FRAGMENT, dati);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppBarLayout apbl = (AppBarLayout) getActivity().findViewById(R.id.appBarLayout);
            apbl.setElevation(4);
        }
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        dati = (ArrayList<HashMap<String, String>>) getArguments().getSerializable(ARGS_BUNDLE_FRAGMENT);
        getActivity().setTitle(getString(R.string.menu_classifiche));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_classifica, container, false);

        // i primi tre della classifica
        ArrayList<HashMap<String, String>> t1 = new ArrayList<>();
        for(int i=0; i < PODIO_COUNT; i++) { t1.add(dati.get(i)); }
        RecyclerView recyclerView1 = (RecyclerView) v.findViewById(R.id.recycleViewClassifica1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView1.setAdapter(new Informazioni(t1, Informazioni.PODIO));
        recyclerView1.setNestedScrollingEnabled(false);
        t1 = null;

        // i primi tre della classifica
        ArrayList<HashMap<String, String>> t2 = new ArrayList<>();
        for(int i=PODIO_COUNT; i < dati.size(); i++) { t2.add(dati.get(i)); }
        RecyclerView recyclerView2 = (RecyclerView) v.findViewById(R.id.recycleViewClassifica2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView2.setAdapter(new Informazioni(t2, Informazioni.DOPO_PODIO));
        recyclerView2.setNestedScrollingEnabled(false);
        t2 = null;

        dati = null;
        return  v;
    }

    /**
     * Classe per gestire la recyclerview
     * (Adapter)
     */
    class Informazioni extends RecyclerView.Adapter<InformazioniViewHolder> {
        public static final int PODIO = 1;
        public static final int DOPO_PODIO = 2;
        private List<HashMap<String, String>> ris;
        private int numeroLista;

        /**
         * @param ris arraylist di hashmap
         */
        public Informazioni(List<HashMap<String, String>> ris, int numeroLista) {
            this.ris = ris;
            this.numeroLista = numeroLista;
        }

        @Override
        public InformazioniViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.classifica_list, parent, false);
            return new InformazioniViewHolder(view);
        }

        @Override
        public void onBindViewHolder(InformazioniViewHolder holder, int position) {
            HashMap<String, String> temp = ris.get(position);

            if (temp.get(CHART_USERNAME_LABEL).equals(sharedPrefs.getString(UTENTE_USERNAME_LABEL, ""))) {
                holder.nome.setText(getString(R.string.scritta_tu));
                holder.nome.setTypeface(null, Typeface.BOLD);
            }
            else {
                holder.addOnClickListener(temp);
                holder.nome.setText(temp.get(CHART_NOME_LABEL) + " " + temp.get(CHART_COGNOME_LABEL));
            }

            holder.punteggio.setText(getString(R.string.point) + " " + temp.get(CHART_PUNTEGGIO_LABEL));
            holder.username.setText(temp.get(CHART_USERNAME_LABEL));

            // cambio il colore di sfondo/l'immagine
            switch (numeroLista) {
                case PODIO:
                    //scelgo il colore per colorare il cerchio in base alla posizione
                    if (position == 0)
                        holder.img.setImageResource(R.drawable.medal_1);
                    else if (position == 1)
                        holder.img.setImageResource(R.drawable.medal_2);
                    else if (position == 2)
                        holder.img.setImageResource(R.drawable.medal_3);
                    break;

                case DOPO_PODIO:
                    holder.number.setText(String.valueOf(PODIO_COUNT + position + 1));
                    break;

                default:
                    Log.e(Classifica.this.getClass().getName(), "Error");
            }
        }

        @Override
        public int getItemCount() {
            return ris.size();
        }
    }

    /**
     * Classe per le informazioni principali
     */
    class InformazioniViewHolder extends RecyclerView.ViewHolder {
        private TextView nome, punteggio, username, number;
        private ImageView img;

        private InformazioniViewHolder(View itemView) {
            super(itemView);
            nome = (TextView) itemView.findViewById(R.id.nomeGiocatore);
            punteggio = (TextView) itemView.findViewById(R.id.punteggioGiocatore);
            username = (TextView) itemView.findViewById(R.id.usernameGiocatore);
            number = (TextView) itemView.findViewById(R.id.numero);
            img = (ImageView) itemView.findViewById(R.id.circle_number);
        }

        private void addOnClickListener(final HashMap<String, String> temp) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    View dialogView = inflater.inflate(R.layout.dati_giocatore_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                            (sharedPrefs.getBoolean(DARK_THEME_LABEL, false) ? R.style.DialogDark_Alert : R.style.DialogLight_Alert));

                    // nome e cognome
                    TextView t = (TextView) dialogView.findViewById(R.id.userDialogName);
                    t.setText(temp.get(CHART_NOME_LABEL) + " " + temp.get(CHART_COGNOME_LABEL));

                    // punteggio
                    t = (TextView) dialogView.findViewById(R.id.userDialogPunteggio);
                    t.setText(getString(R.string.point) + " " + temp.get(CHART_PUNTEGGIO_LABEL));


                    // iscritto dal
                    t = (TextView) dialogView.findViewById(R.id.userDialogIscritto);
                    t.setText(getString(R.string.dialog_infoUtente_iscritto) + " " + temp.get(CHART_ISCRITTO_LABEL));

                    // immagine utente
                    final CircleImageView img = (CircleImageView) dialogView.findViewById(R.id.userDialogImage);
                    final String i = temp.get(CHART_IMAGE_LABEL);
                    if(i.trim().equals("")) {
                        Picasso.with(getContext())
                                .load(R.drawable.default_user)
                                .into(img);
                    }
                    else {
                        // carico la foto dell'utente
                        Picasso.with(getContext())
                                .load(Util.USERIMAGEPATH + i)
                                .into(img);

                        // la rendo cliccabile per aprire la foto intera
                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent show = new Intent(getActivity().getApplicationContext(), MostraImmaginiActivity.class);
                                show.putExtra(EXTRA_IMMAGINE_GIOCATORE , i);
                                show.putExtra(EXTRA_USERNAME_GIOCATORE, username.getText().toString());

                                ActivityOptions transitionActivityOptions = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    String transitionName = getString(R.string.transName);
                                    transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), img, transitionName);
                                    startActivity(show, transitionActivityOptions.toBundle());
                                }
                                else
                                    startActivity(show);
                            }
                        });
                    }

                    builder.setView(dialogView);
                    builder.setTitle(getString(R.string.dialog_infoUtente_title));
                    builder.setPositiveButton(getString(R.string.chiudi_dialog), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }
}


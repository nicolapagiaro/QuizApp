package com.pagiaro.quizapp.Fragment;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mvc.imagepicker.ImagePicker;
import com.pagiaro.quizapp.R;
import com.pagiaro.quizapp.Util.Util;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import at.grabner.circleprogress.CircleProgressView;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.pagiaro.quizapp.MainActivity.REQUEST_CODE;
import static com.pagiaro.quizapp.Util.Util.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ProfiloUtente#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfiloUtente extends Fragment {
    private static final int SECTION_RED_HEIGHT = 65;
    private SharedPreferences sharedPrefs;
    private AppBarLayout apbl;
    private ArrayList<HashMap<String, String>> dati;

    public ProfiloUtente() {
        // Required empty public constructor
    }

    /**
     * Nuova istanza del fragment
     *
     * @return A new instance of fragment ProfiloUtente.
     */
    public static ProfiloUtente newInstance(ArrayList<HashMap<String, String>> dati) {
        ProfiloUtente f = new ProfiloUtente();
        Bundle args = new Bundle();
        args.putSerializable(ARGS_BUNDLE_FRAGMENT, dati);
        f.setArguments(args);
        return f;
    }

    /**
     * @param ele
     */
    private void setElevation(int ele) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
            apbl = (AppBarLayout) getActivity().findViewById(R.id.appBarLayout);
            apbl.setElevation(ele);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActivity().setTitle(getString(R.string.menu_profilo));
        setElevation(0);
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        dati = (ArrayList<HashMap<String, String>>) getArguments().getSerializable(ARGS_BUNDLE_FRAGMENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profiloutente, container, false);

        // per l'ombra sotto l'actionbar (elevation=4)
        final ScrollView scroll = (ScrollView) v.findViewById(R.id.scroll_layout);
        scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scroll.getScrollY() >= SECTION_RED_HEIGHT)
                    setElevation(4);
                else
                    setElevation(0);

            }
        });

        // TextView e ImageView varie
        final CircleImageView img = (CircleImageView) v.findViewById(R.id.profile_image);
        FloatingActionButton fab_image = (FloatingActionButton) v.findViewById(R.id.fab_editImage);
        TextView nome = (TextView) v.findViewById(R.id.nomeUtente);
        TextView user = (TextView) v.findViewById(R.id.usernameUtente);
        TextView data = (TextView) v.findViewById(R.id.dataNascita);
        TextView iscrittoDal = (TextView) v.findViewById(R.id.dataIscrizione);
        ImageView usernameIco = (ImageView) v.findViewById(R.id.usernameIco);
        ImageView nascitaIco = (ImageView) v.findViewById(R.id.nascitaIco);
        ImageView iscrittoIco = (ImageView) v.findViewById(R.id.iscrizioneIco);
        View cardBest = v.findViewById(R.id.cardBest);
        View cardRecents = v.findViewById(R.id.cardRecents);
        TextView txtBest = (TextView) v.findViewById(R.id.bestRis);
        TextView txtRecents = (TextView) v.findViewById(R.id.allRis);

        String dataN = sharedPrefs.getString(UTENTE_DATAN_LABEL, null);
        String dataI = sharedPrefs.getString(UTENTE_ISCRITTODAL_LABEL, null);

        // data di nascita e data di iscrizione
        data.setText(convertiData(dataN));
        iscrittoDal.setText(convertiData(dataI));

        // nome, cognome e username
        nome.setText(sharedPrefs.getString(UTENTE_NOME_LABEL, "") + " " + sharedPrefs.getString(UTENTE_COGNOME_LABEL, ""));
        user.setText(sharedPrefs.getString(UTENTE_USERNAME_LABEL, ""));

        // imposto l'immagine di profilo
        final String s = sharedPrefs.getString(UTENTE_IMAGE_LABEL, "");
        if (s.equals(""))
            Picasso.with(getActivity().getApplicationContext())
                    .load(R.drawable.default_user)
                    .into(img);
        else {
            Picasso.with(getActivity().getApplicationContext())
                    .load(Util.USERIMAGEPATH + s)
                    .into(img);
        }

        // click listener al fab per cambiare l'immagine
        fab_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                    }
                } else {
                    ImagePicker.pickImage(getActivity(), getString(R.string.image_picker));
                }
            }
        });

        // icone bianche per il tema scuro
        if(sharedPrefs.getBoolean(DARK_THEME_LABEL, false)) {
            usernameIco.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_account_white));
            nascitaIco.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_gift_white));
            iscrittoIco.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_account_plus_white));
        }

        // punteggio
        CircleProgressView punteggio = (CircleProgressView) v.findViewById(R.id.punteggio);
        punteggio.setTextColor(getResources().getColor(R.color.primary_textLight));
        punteggio.setInnerContourSize(0);
        punteggio.setOuterContourSize(0);
        punteggio.setValueAnimated(sharedPrefs.getInt(UTENTE_PUNTEGGIO_LABEL, 0));

        if (dati.size() != 0) {
            cardBest.setVisibility(View.VISIBLE);
            cardRecents.setVisibility(View.VISIBLE);
            txtBest.setVisibility(View.VISIBLE);
            txtRecents.setVisibility(View.VISIBLE);

            // recycler view 1
            ArrayList<HashMap<String, String>> t1 = new ArrayList<>();
            t1.add(dati.get(0));
            RecyclerView bestRis = (RecyclerView) v.findViewById(R.id.bestRisRe);
            bestRis.setLayoutManager(new LinearLayoutManager(getContext()));
            bestRis.setAdapter(new RisultatiHolder(t1));
            bestRis.setNestedScrollingEnabled(false);
            t1 = null;

            // recycler view 2
            ArrayList<HashMap<String, String>> t2 = new ArrayList<>();
            for (int i = 1; i < dati.size(); i++) t2.add(dati.get(i));

            RecyclerView allRis = (RecyclerView) v.findViewById(R.id.allRisRe);
            allRis.setLayoutManager(new LinearLayoutManager(getContext()));
            allRis.setAdapter(new RisultatiHolder(t2));
            allRis.setNestedScrollingEnabled(false);
            t2 = null;
        }

        dati = null;
        return v;
    }

    /**
     * Classe per gestire la recyclerview
     * (Adapter)
     */
    public class RisultatiHolder extends RecyclerView.Adapter<RisultatoViewHolder>{
        ArrayList<HashMap<String, String>> lista;

        /**
         *
         * @param lista
         */
        public RisultatiHolder(ArrayList<HashMap<String, String>> lista) {
            this.lista = lista;
        }

        @Override
        public RisultatoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.risultati_giocatore_list, parent, false);
            return new RisultatoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RisultatoViewHolder holder, int position) {
            HashMap<String, String> temp = lista.get(position);
            holder.punteggioRis.setText(getString(R.string.ris) + " "+ temp.get(UTENTE_PUNTEGGIO_SESSIONE_LABEL));
            holder.tempoRis.setText(getString(R.string.tempo) + " " + temp.get(UTENTE_TEMPO_LABEL));
            holder.categoriaRis.setText(temp.get(UTENTE_CATEGORIASESSIONE_LABEL));
            holder.dataRis.setText(convertiData(temp.get(UTENTE_DATA_LABEL)));
            holder.numero.setText("-");
        }

        @Override
        public int getItemCount() {
            return lista.size();
        }
    }

    /**
     * Classe per le informazioni principali
     */
    public class RisultatoViewHolder extends RecyclerView.ViewHolder{
        protected TextView punteggioRis, dataRis, tempoRis, categoriaRis, numero;

        public RisultatoViewHolder(View itemView) {
            super(itemView);
            punteggioRis = (TextView) itemView.findViewById(R.id.punteggioRis);
            dataRis = (TextView) itemView.findViewById(R.id.dataRis);
            categoriaRis = (TextView) itemView.findViewById(R.id.categoriaRis);
            tempoRis = (TextView) itemView.findViewById(R.id.tempoRis);
            numero = (TextView) itemView.findViewById(R.id.numeroRis);
        }
    }

    /**
     * Converte la data da numeri con il mese scritto a parola (formato dd-MMMM-yyyy)
     * @param data data in stringa (col formato yyyy-mm-dd)
     * @return
     */
    private String convertiData(String data) {
        if (data != null) {
            String[] data1 = data.split("-");
            Calendar cal1 = Calendar.getInstance();
            cal1.set(Integer.parseInt(data1[0]), Integer.parseInt(data1[1]) - 1, Integer.parseInt(data1[2]));
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.ITALIAN);
            return formatter.format(cal1.getTime());
        }
        return "null";
    }

}

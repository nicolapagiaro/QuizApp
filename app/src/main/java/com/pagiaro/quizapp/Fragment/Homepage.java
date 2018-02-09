package com.pagiaro.quizapp.Fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.makeramen.roundedimageview.RoundedImageView;
import com.pagiaro.quizapp.QuizActivity;
import com.pagiaro.quizapp.Util.Domande;
import com.pagiaro.quizapp.R;
import com.pagiaro.quizapp.Util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import static com.pagiaro.quizapp.Util.Util.*;

public class Homepage extends Fragment {
    ArrayList<HashMap<String, String>> dati;
    private boolean isDownloading = false;

    public Homepage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment Homepage.
     */
    public static Homepage newInstance(ArrayList<HashMap<String, String>> dati) {
        Homepage f = new Homepage();
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
        dati = (ArrayList<HashMap<String, String>>) getArguments().getSerializable(ARGS_BUNDLE_FRAGMENT);
        getActivity().setTitle(getString(R.string.menu_home));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_homepage, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycleViewHone);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new CategorieHolder(dati));
        return  v;
    }

    /**
     * Classe per gestire la recyclerview
     * (Adapter)
     */
    public class CategorieHolder extends RecyclerView.Adapter<CategoriaViewHolder>{
        ArrayList<HashMap<String, String>> lista;

        /**
         *
         * @param lista
         */
        public CategorieHolder(ArrayList<HashMap<String, String>> lista) {
            this.lista = lista;
        }

        @Override
        public CategoriaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categorie_list, parent, false);
            return new CategoriaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CategoriaViewHolder holder, int position) {
            HashMap<String, String> temp = lista.get(position);
            holder.cat.setText(temp.get(CAT_NOME_LABEL));
            holder.tot.setText(temp.get(CAT_NUMEROTOT_LABEL) + " " + getString(R.string.domandeTot));
            Picasso.with(getActivity().getApplicationContext())
                    .load(Util.CATIMAGEPATH + temp.get(CAT_IMAGE_LABEL))
                    .into(holder.roundedImageView,  new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (holder.progress != null) {
                                holder.progress .setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
            holder.addOnClickListener(position);
        }

        @Override
        public int getItemCount() {
            return lista.size();
        }
    }

    /**
     * Classe per le informazioni principali
     */
    class CategoriaViewHolder extends RecyclerView.ViewHolder{
        private TextView cat, tot;
        private RoundedImageView roundedImageView;
        private ProgressBar progress, loadingS;
        private Button btnInizia;


        private CategoriaViewHolder(View itemView) {
            super(itemView);
            cat = (TextView) itemView.findViewById(R.id.categoriaResult);
            tot = (TextView) itemView.findViewById(R.id.totale);
            roundedImageView = (RoundedImageView) itemView.findViewById(R.id.immagineCategoria);
            loadingS = (ProgressBar) itemView.findViewById(R.id.loadingS);
            progress = (ProgressBar) itemView.findViewById(R.id.progBar1);
            btnInizia = (Button) itemView.findViewById(R.id.quizInizia);
        }

        private void addOnClickListener(final int position) {
            btnInizia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isDownloading) {
                        isDownloading = true;
                        btnInizia.setVisibility(View.INVISIBLE);
                        loadingS.setVisibility(View.VISIBLE);
                        scaricaDomande(dati.get(position).get(CAT_ID_LABEL), btnInizia,
                                loadingS, cat.getText().toString());
                    }
                    else
                        Toast.makeText(getContext(), getString(R.string.alreadyDownloading), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Metodo per lo scaricamento delle domande
     * @param id
     */
    private void scaricaDomande(final String id, final Button btn, final ProgressBar p, final String catNome) {
        Ion.with(getActivity().getApplicationContext())
                .load(Util.DOWNLOAD_DOMANDE)
                .setBodyParameter(CAT_ID_LABEL, id)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {

                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(result != null) {
                            ArrayList<HashMap<String, String>> lista = new ArrayList<>();
                            JsonArray domande = result.getAsJsonArray(JSON_RESULT);

                            for(int i = 0; i < domande.size(); i++) {
                                HashMap<String, String> temp = new HashMap<>();
                                JsonObject obj = domande.get(i).getAsJsonObject();
                                temp.put(DOMANDE_ID_LABEL, obj.get(DOMANDE_ID_LABEL).getAsString());
                                temp.put(DOMANDE_DOMANDA_LABEL, obj.get(DOMANDE_DOMANDA_LABEL).getAsString());
                                temp.put(DOMANDE_RG_LABEL, obj.get(DOMANDE_RG_LABEL).getAsString());
                                temp.put(DOMANDE_RS1_LABEL, obj.get(DOMANDE_RS1_LABEL).getAsString());
                                temp.put(DOMANDE_RS2_LABEL, obj.get(DOMANDE_RS2_LABEL).getAsString());
                                temp.put(DOMANDE_RS3_LABEL, obj.get(DOMANDE_RS3_LABEL).getAsString());
                                temp.put(DOMANDE_IMMAGINE_LABEL, obj.get(DOMANDE_IMMAGINE_LABEL).getAsString());
                                lista.add(temp);
                            }

                            Domande d = new Domande(lista);
                           // dialog.dismiss();
                            Intent quiz = new Intent(getActivity(), QuizActivity.class);
                            quiz.putExtra(EXTRA_DOMANDE, d);
                            quiz.putExtra(EXTRA_NOME_CATEORIA, catNome);
                            quiz.putExtra(EXTRA_CATEGORIA, id);
                            startActivity(quiz);
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    getString(R.string.errorDownloadDomande), Toast.LENGTH_SHORT).show();

                        }

                        // ripristino le cose
                        btn.setVisibility(View.VISIBLE);
                        p.setVisibility(View.GONE);
                        isDownloading = false;
                    }
                });
    }
}

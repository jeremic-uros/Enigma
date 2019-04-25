package com.jeremic.uros.enigma.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jeremic.uros.enigma.model.GameModel;
import com.jeremic.uros.enigma.view.AgentActivity;
import com.jeremic.uros.enigma.R;
import com.jeremic.uros.enigma.controler.ApplicationController;
import com.jeremic.uros.enigma.view.MasterActivity;


public class CardFragment extends Fragment {

    private static final int NUM_OF_COLUMNS = 5;
    private static final int NUM_OF_WORDS = 391;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ApplicationController gameControler;

    private OnCardFragmentInteractionListener mListener;

    public CardFragment() {
        // Required empty public constructor
    }

    public static CardFragment newInstance() {
        CardFragment fragment = new CardFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_card, container, false);
        view.post(new Runnable() {
            @Override
            public void run() {
                recyclerView = view.findViewById(R.id.cardRecyclerView);
                recyclerView.setHasFixedSize(true);
                layoutManager = new GridLayoutManager(view.getContext(), NUM_OF_COLUMNS);
                recyclerView.setLayoutManager(layoutManager);

                if(savedInstanceState == null) {
                    String[] words = new String[25];

                    SharedPreferences flags = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    if(flags.getBoolean("gameStarted",false)){
                        SharedPreferences.Editor editor = flags.edit();
                        editor.putBoolean("gameStarted",false);
                        editor.apply();
                        boolean isMaster = false;
                        if(mListener instanceof MasterActivity) isMaster = true;
                        mAdapter = new CardAdapter(mListener.restoreGameModel(),getResources(),view.getMeasuredHeight(),isMaster);
                    }
                    else mAdapter = new CardAdapter(words, getResources(), view.getMeasuredHeight());

                } else {
                    ((CardAdapter) mAdapter).setRootHeight(view.getMeasuredHeight());
                }

                if (getActivity() instanceof AgentActivity) {
                    ((CardAdapter) mAdapter).setCardClickListener(new CardAdapter.CardClickListener() {
                        @Override
                        public void onCardClick(int pos, View v) {
                            onButtonPressed(pos);
                        }
                    });
                }
                recyclerView.setAdapter(mAdapter);

            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        CardAdapter cardAdapter = (CardAdapter) mAdapter;
        outState.putSerializable("adapter",cardAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            CardAdapter cardAdapter = (CardAdapter) savedInstanceState.getSerializable("adapter");
            cardAdapter.setResources(getResources());
            mAdapter = cardAdapter;
        }
    }

    public void onButtonPressed(int pos) {
        if (mListener != null) {
            mListener.onCardPressed(pos);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCardFragmentInteractionListener) {
            mListener = (OnCardFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCardFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnCardFragmentInteractionListener {
        void onCardPressed(int pos);
        GameModel restoreGameModel();
    }

    //Update words
    public void updateWords(String[] words) {
        ((CardAdapter)mAdapter).setWords(words);
        mAdapter.notifyDataSetChanged();
    }

    //if is clicked will not change color
    public void updateCard(int pos, int color,boolean clicked) {
        ((CardAdapter)mAdapter).updateItem(pos,color,clicked);
        mAdapter.notifyItemChanged(pos);
    }

}

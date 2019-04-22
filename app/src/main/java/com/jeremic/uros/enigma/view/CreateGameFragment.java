package com.jeremic.uros.enigma.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jeremic.uros.enigma.R;


public class CreateGameFragment extends Fragment {


    private OnCreateGameFragmentInteractionListener mListener;

    public CreateGameFragment() {
        // Required empty public constructor
    }


    public static CreateGameFragment newInstance() {
        CreateGameFragment fragment = new CreateGameFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);
        view.findViewById(R.id.startGameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed(view);
            }
        });
        view.findViewById(R.id.newWordsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed(view);
            }
        });
        view.findViewById(R.id.newArrangmentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed(view);
            }
        });
        return view;
    }

    public void onButtonPressed(View view) {
        if (mListener != null) {
            switch (view.getId()) {
                case R.id.startGameButton:
                    mListener.onStartGamePressed();
                    break;
                case R.id.newWordsButton:
                    mListener.onNewWordsPressed();
                    break;
                case R.id.newArrangmentButton:
                    mListener.onNewLayoutPressed();
                    break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateGameFragmentInteractionListener) {
            mListener = (OnCreateGameFragmentInteractionListener) context;
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

    public interface OnCreateGameFragmentInteractionListener {
        void onStartGamePressed();
        void onNewWordsPressed();
        void onNewLayoutPressed();
    }
}

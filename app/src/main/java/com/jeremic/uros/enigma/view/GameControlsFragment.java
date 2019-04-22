package com.jeremic.uros.enigma.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.jeremic.uros.enigma.R;


public class GameControlsFragment extends Fragment {

    private OnGameControlsInteractionListener mListener;

    public GameControlsFragment() {
        // Required empty public constructor
    }



    public static GameControlsFragment newInstance() {
        GameControlsFragment fragment = new GameControlsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_controls, container, false);
        view.findViewById(R.id.sendCodeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed();
            }
        });
        return  view;
    }


    public void onButtonPressed() {
        if (mListener != null) {
            /*
                get code and number
                and send to activity to handle
             */
            EditText codeEditText = getActivity().findViewById(R.id.inputCodeEditText);
            EditText numEditText = getActivity().findViewById(R.id.inputNumEditText);
            mListener.onSendCode(codeEditText.getText().toString(),numEditText.getText().toString());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGameControlsInteractionListener) {
            mListener = (OnGameControlsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCodeFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnGameControlsInteractionListener {
        void onSendCode(String code,String num);
    }
}

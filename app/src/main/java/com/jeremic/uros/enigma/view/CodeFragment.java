package com.jeremic.uros.enigma.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jeremic.uros.enigma.R;


public class CodeFragment extends Fragment {

    private OnCodeInteractionListener mListener;

    public CodeFragment() {
        // Required empty public constructor
    }

    public static CodeFragment newInstance() {
        CodeFragment fragment = new CodeFragment();
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
        View view = inflater.inflate(R.layout.fragment_code, container, false);
        view.findViewById(R.id.connectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConnect();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if( context instanceof OnCodeInteractionListener){
            mListener = (OnCodeInteractionListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCodeInteractionListener");
        }
    }

    public void onConnect(){
        if(mListener != null){
            mListener.onConnectButtonPressed();
        }
    }


    public interface OnCodeInteractionListener {
        void onConnectButtonPressed();
    }



}

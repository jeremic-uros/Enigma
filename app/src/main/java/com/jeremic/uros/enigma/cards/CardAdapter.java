package com.jeremic.uros.enigma.cards;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.jeremic.uros.enigma.R;
import com.jeremic.uros.enigma.model.GameModel;

import java.io.Serializable;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> implements Serializable {
    private static CardClickListener cardClickListener;
    private String[] words;
    private int[] layout;
    private transient Resources resources;
    private int rootHeight;

    public CardAdapter(String[] words,Resources res,int height) {
        this.words = words;
        rootHeight = height;
        this.resources = res;
        layout = new int[25];
        for(int i = 0 ; i < 25 ; i++) layout[i] = R.color.colorNeutral;
    }

    // Used when loading from file
    public CardAdapter(GameModel gameModel,Resources res, int height,boolean isMaster){
        this(gameModel.getWords(),res,height);
        GameModel.CardType[] types = gameModel.getLayout();
        boolean[] isPressed = gameModel.getIsPressed();
        for(int i = 0 ; i < 25 ; i++){
            if(isMaster || isPressed[i]) {
                switch (types[i]) {
                    case RED:
                        layout[i] = R.color.colorRedTeam;
                        break;
                    case BLUE:
                        layout[i] = R.color.colorBlueTeam;
                        break;
                    case BLANK:
                        layout[i] = R.color.colorBlank;
                        break;
                    case ASSASSIN:
                        layout[i] = R.color.colorAssassin;
                        break;
                }
            }
            if(isPressed[i]) words[i] = "";
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.card_button,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final String word = words[i];
        viewHolder.cardButton.setText(word);
        viewHolder.cardButton.setHeight(rootHeight/5);
        viewHolder.cardButton.setTextColor(resources.getColor(R.color.colorAccent));
        viewHolder.cardButton.setBackgroundColor(resources.getColor(layout[i]));
    }

    @Override
    public int getItemCount() {
        if(words == null) return 0;
        return words.length;
    }

    public void updateItem(int pos,int color,boolean isClicked){
        try {
            if(isClicked) words[pos] = "";
            else layout[pos] = color;
        } catch (IndexOutOfBoundsException i) {
            Log.e("WRONG_POSITION","No element at " + pos);
        }
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    public void setResources(Resources res) { this.resources = res;}

    public void setRootHeight(int height) { rootHeight = height;}

    public interface CardClickListener {
        void onCardClick(int pos,View v);
    }

    //Set by card fragment, used notify the controller which card is pressed
    public void setCardClickListener(CardClickListener cardClickListener) { CardAdapter.cardClickListener = cardClickListener;}

    public class ViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener{
        public Button cardButton;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            cardButton = v.findViewById(R.id.cardButton);
            cardButton.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if(cardClickListener != null) {
                cardClickListener.onCardClick(getAdapterPosition(), view);
            }
        }
    }
}

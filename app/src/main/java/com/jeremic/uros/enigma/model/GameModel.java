package com.jeremic.uros.enigma.model;


import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class GameModel implements Serializable {


    public static final String filename = "GameModel.ser";

    public enum CardType {
        BLUE,
        RED,
        BLANK,
        ASSASSIN
    }

    public enum Turn {
        RED,
        BLUE
    }

    private String[] words;
    private boolean assassinPressed;
    private CardType[] layout;
    private boolean[] isPressed;
    private Turn turn;
    private int cardsLeftRed,CardsLeftBlue;
    private transient Context context;

    public GameModel(Context context){
        this.context = context;
    }

    public String[] getWords() {
        return words;
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    public CardType[] getLayout() {
        return layout;
    }

    public void setLayout(CardType[] layout) {
        this.layout = layout;
    }

    public CardType getType(int pos) { return layout[pos];}

    public boolean[] getIsPressed() {
        return isPressed;
    }

    public void setIsPressed(boolean[] isPressed) {
        this.isPressed = isPressed;
    }

    //Updates pressed cards and returns the type of the pressed card, in case of error returns null
    public CardType updateOnCardPressed(int pos){
        if(words != null && isPressed != null && layout != null && pos < words.length) {
            isPressed[pos] = true;
            if(layout[pos] == CardType.ASSASSIN) assassinPressed = true;
            return layout[pos];
        }
        return null;
    }

    public boolean getIsAssassinPressed() { return  assassinPressed;}

    public Turn getTurn() {
        return turn;
    }

    public void setTurn(Turn turn) {
        this.turn = turn;
    }

    public int getCardsLeftRed() {
        return cardsLeftRed;
    }

    public void setCardsLeftRed(int cardsLeftRed) {
        this.cardsLeftRed = cardsLeftRed;
    }

    public int getCardsLeftBlue() {
        return CardsLeftBlue;
    }

    public void setCardsLeftBlue(int cardsLeftBlue) {
        CardsLeftBlue = cardsLeftBlue;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void saveGameModel(){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(filename,Context.MODE_PRIVATE);
            if(fos != null) {
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(this);
                os.close();
                fos.close();
                Log.i("UROS","wrote game model");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("error","failed to open to file");

        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("error","failed to save to file");
        }
    }

    public GameModel restoreGameModel(){
        FileInputStream fis = null;
        GameModel gameModel = null;
        try{
           fis = context.openFileInput(filename);
            ObjectInputStream is = new ObjectInputStream(fis);
            gameModel = (GameModel) is.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return gameModel;
    }

}


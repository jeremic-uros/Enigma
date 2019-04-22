package com.jeremic.uros.enigma.model;


import java.io.Serializable;


public class GameModel implements Serializable {


    public static final String WORDS_KEY = "words";
    public static final String LAYOUT_KEY = "layout";
    public static final String PRESSED_KEY = "pressed";
    public static final String TURN_KEY = "turn";
    public static final String RED_CARDS_LEFT_KEY = "red";
    public static final String BLUE_CARDS_LEFT_KEY = "blue";

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

}


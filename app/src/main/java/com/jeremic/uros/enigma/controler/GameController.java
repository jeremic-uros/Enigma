package com.jeremic.uros.enigma.controler;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.Log;

import com.jeremic.uros.enigma.view.GameView;
import com.jeremic.uros.enigma.R;
import com.jeremic.uros.enigma.model.GameModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameController implements ApplicationController {
    private static final int NUM_OF_WORDS = 693;
    private GameModel gameModel;
    private transient GameView gameView;
    private transient BluetoothSocket bluetoothSocket;
    private transient PrintWriter pw;
    private transient BufferedReader bf;
    private int numOfPresses;     // number of cards left to press in turn
    private boolean gameStarted;

    public GameController(Activity context){
        gameModel = new GameModel();
        if ( context instanceof GameView){
            gameView = (GameView) context;
        }
    }

    @Override
    public String[] generateWords() {
        TypedArray words = ((Context)gameView).getResources().obtainTypedArray(R.array.words);
        Random rand= new Random();
        String[] newWords = new String[25];
        for(int i = 0 ; i < 25 ; i++) {
            int index = -1;
            try {
                String word = "";
                boolean check = true;
                while(check) {
                    index = rand.nextInt(NUM_OF_WORDS);
                    word = words.getString(index);
                    check = false;
                    for(int j = 0 ; j < i ; j++){
                        if( newWords[j].equals(word)) check = true;
                    }
                }
                newWords[i] = word;
            }
            catch (Exception e) {
                Log.e("array","NO ELEMENT WITH" + index);
            }
        }
        words.recycle();
        gameModel.setWords(newWords);
        return newWords;
    }


    @Override
    public GameModel.CardType[] generateLayout() {
        GameModel.CardType[] layout = new GameModel.CardType[25];
        //init state
        layout[0] = GameModel.CardType.ASSASSIN;
        for(int i = 1 ; i < 9 ; i++) layout[i] = GameModel.CardType.BLUE;
        for(int i = 9 ; i < 17 ; i++) layout[i] = GameModel.CardType.RED;
        for(int i = 17 ; i < 25 ; i++) layout[i] = GameModel.CardType.BLANK;
        List<GameModel.CardType> temp = Arrays.asList(layout);
        Collections.shuffle(temp);
        layout = (GameModel.CardType[]) temp.toArray();
        gameModel.setLayout(layout);
        return layout;
    }

    @Override
    public void gameLoopAgent(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
        try {
            bf = new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream()));
            pw = new PrintWriter(bluetoothSocket.getOutputStream());
            // get the words and layout
            if(!gameStarted) {
                String[] words = new String[25];
                GameModel.CardType[] layout = new GameModel.CardType[25];
                Log.i("AGENT", "waiting on words");
                receiveWordsAndLayoutPhase(words, layout);
                Log.i("AGENT", "got on words");

                // Set up game model
                gameModel.setWords(words);
                gameModel.setLayout(layout);
                init(); // initializes gameModel with default values
                gameView.refreshCards(words);
                Log.i("AGENT", "refreshed words");
            }

            // save flag that the game has started
            gameView.setGameStart(true);

            /*
                Wait for the code and numbber of words associate with the code
                Enable cards to be clicked
                The click is send from another thread to the connected device
                repeat until gameover
             */
            gameStarted = true;
            waitForCodeLoopPhase();

        } catch (IOException e) {
            //e.printStackTrace();
            Log.i("AGENT","bluetooth error" );
        }
        gameover();
    }


    private void receiveWordsAndLayoutPhase(String[] words, GameModel.CardType[] layout) {
        try {
            for (int i = 0; i < 25; i++) {
                String word = bf.readLine();
                String type = bf.readLine();
                Log.i("AGENT", "recieved word and type " + i);
                words[i] = word;
                switch (type) {
                    case "red":
                        layout[i] = GameModel.CardType.RED;
                        break;
                    case "blue":
                        layout[i] = GameModel.CardType.BLUE;
                        break;
                    case "assassin":
                        layout[i] = GameModel.CardType.ASSASSIN;
                        break;
                    case "blank":
                        layout[i] = GameModel.CardType.BLANK;
                        break;
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.e("BLUETOOTH", "socket error on receiving words and layout");
        }
    }

    private void waitForCodeLoopPhase(){
        try {
            while(gameModel.getCardsLeftBlue()>0 && gameModel.getCardsLeftRed()>0){
                Log.i("AGENT","wait for code" );
                String code = bf.readLine();
                String num = bf.readLine();
                gameView.setCode(code,num);
                try {
                    numOfPresses = Integer.parseInt(num);
                } catch (NumberFormatException e){
                    numOfPresses = 0;
                }
                gameView.setClickable(true);
                Log.i("AGENT","code received" );
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.e("BLUETOOTH","Error while waiting for code");
        }
    }

    @Override
    public GameModel.CardType cardPressed(final int pos){

        if(!gameModel.getIsPressed()[pos]) {
            numOfPresses--;
            if(numOfPresses == 0 || wrongCardPressed(gameModel.getTurn(),gameModel.getType(pos))) {
                gameView.setClickable(false);
                gameView.refreshTurnDisplay(changeTurn());
            }
            gameModel.updateOnCardPressed(pos);
            updateCardsLeft(pos);
            new Thread() {
                @Override
                public void run() {

                    pw.println(pos);
                    pw.flush();
                    Log.i("AGENT", "card" + pos + "pressed");
                }
            }.start();
            if(gameModel.getType(pos) == GameModel.CardType.ASSASSIN) close();
        }
        return gameModel.getType(pos);
    }

    private boolean wrongCardPressed(GameModel.Turn turn, GameModel.CardType cardType) {
        switch (turn){
            case BLUE:
                if(cardType == GameModel.CardType.BLUE) return false;
                break;
            case RED:
                if(cardType == GameModel.CardType.RED) return false;
                break;
        }
        return true;
    }

    @Override
    public void gameLoopMaster(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;

        try {
            bf = new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream()));
            pw = new PrintWriter(bluetoothSocket.getOutputStream());
            if(!gameStarted) {
                init();
                String[] words = gameModel.getWords();
                GameModel.CardType[] layout = gameModel.getLayout();
                Log.i("MASTER", "send words");
                sendWordsAndLayoutPhase(words,layout);
                Log.i("MASTER", "sent words");
            }

            // save flag that the game has started
            gameView.setGameStart(true);

            gameStarted = true;
            waitForCardPressPhase();

        } catch (IOException e) {
            Log.i("MASTER","bluetooth error");
        }
        gameover();
    }

    private void sendWordsAndLayoutPhase(String[] words,GameModel.CardType[] layout){
            for (int i = 0; i < 25; i++) {
                pw.println(words[i]);
                pw.flush();
                switch (layout[i]) {
                    case BLUE:
                        String blue = "blue";
                        pw.println(blue);
                        break;
                    case RED:
                        String red = "red";
                        pw.println(red);
                        break;
                    case ASSASSIN:
                        String assassin = "assassin";
                        pw.println(assassin);
                        break;
                    case BLANK:
                        String blank = "blank";
                        pw.println(blank);
                        break;
                }
                pw.flush();
            }
    }

    private void waitForCardPressPhase() {
        try {
            while(gameModel.getCardsLeftBlue()>0 && gameModel.getCardsLeftRed()>0){
                Log.i("MASTER","waiting for click");
                numOfPresses = 1;
                while(numOfPresses > 0) {
                    int pos = Integer.parseInt(bf.readLine());
                    gameView.refreshOnCardClick(pos);
                    gameModel.updateOnCardPressed(pos);
                    updateCardsLeft(pos);
                    numOfPresses--;

                    // if wrong card end turn, if assassin end game
                    if(wrongCardPressed(gameModel.getTurn(),gameModel.getType(pos))) {
                        if(gameModel.getType(pos) == GameModel.CardType.ASSASSIN) close();
                        break;
                    }

                }
                gameView.refreshTurnDisplay(changeTurn());
                gameView.setClickable(true);
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.e("BLUETOOTH","socket error while waiting for card press");
        }
    }


    @Override
    public void sendCode(final String code,final String num) {
        gameView.setClickable(false);
        new Thread() {
            @Override
            public void run() {
                    pw.println(code);
                    pw.flush();
                    try{ numOfPresses = Integer.parseInt(num); }
                    catch (NumberFormatException e) { numOfPresses = 0; }
                    pw.println(num);
                    pw.flush();
                    Log.i("MASTER","send code");
            }
        }.start();
    }

    @Override
    public void close(){
        // Close all used resources
        try {
            if(bf != null && pw != null) {
                bf.close();
                pw.close();
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("connection","Unable to close socket");
        }
    }

    @Override
    public void setGameView(GameView activity) {
        if(gameView == null) gameView = activity;
    }

    private void gameover(){
            gameView.setGameStart(false);
            Log.i("GAMEOVER","YAAAS");
            // Close all used resources
            close();
            if( gameModel.getCardsLeftRed() == 0) gameView.showWinner("red");
            else if(gameModel.getCardsLeftBlue() == 0) gameView.showWinner("blue");
            else if(gameModel.getIsAssassinPressed()){
                String msg = "";
                switch (gameModel.getTurn()){
                    case RED:
                        msg = "red";
                        break;
                    case BLUE:
                        msg = "blue";
                        break;
                }
                gameView.showWinner(msg);
            }
            else {
                gameView.showError("Bluetooth je prestao sa radom");
                gameView.reconnect();
            }
    }

    private GameModel.Turn changeTurn(){
        switch (gameModel.getTurn()){
            case BLUE:
                gameModel.setTurn(GameModel.Turn.RED);
                return GameModel.Turn.RED;
            case RED:
                gameModel.setTurn(GameModel.Turn.BLUE);
                return GameModel.Turn.BLUE;
        }
        return null; // should not fall here
    }

    private void updateCardsLeft(int pos){
        switch (gameModel.getType(pos)){
            case RED:
                gameModel.setCardsLeftRed(gameModel.getCardsLeftRed() - 1);
                break;
            case BLUE:
                gameModel.setCardsLeftBlue(gameModel.getCardsLeftBlue() - 1);
                break;
        }
    }

    // Initilaize the gameModel
    private void init() {
        gameModel.setTurn(GameModel.Turn.BLUE);
        gameModel.setCardsLeftRed(8);
        gameModel.setCardsLeftBlue(8);
        boolean[] pressed = new boolean[25];
        gameModel.setIsPressed(pressed);
    }
}

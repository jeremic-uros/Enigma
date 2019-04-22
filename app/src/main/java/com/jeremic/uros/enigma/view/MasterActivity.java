package com.jeremic.uros.enigma.view;

import android.bluetooth.BluetoothSocket;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremic.uros.enigma.R;
import com.jeremic.uros.enigma.bluetooth.BluetoothHandler;
import com.jeremic.uros.enigma.cards.CardFragment;
import com.jeremic.uros.enigma.controler.ApplicationController;
import com.jeremic.uros.enigma.controler.GameController;
import com.jeremic.uros.enigma.model.GameModel;

public class MasterActivity extends GameActivity implements CreateGameFragment.OnCreateGameFragmentInteractionListener, CardFragment.OnCardFragmentInteractionListener, GameControlsFragment.OnGameControlsInteractionListener, GameView,
        BluetoothHandler.BluetoothCommunication {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            Fragment creatFrag = CreateGameFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.controlsFragment, creatFrag).commit();
        }
        clickable = true;
    }



    //////////////////////////////////////////////
    /*
        These methods are called from createGameFragment
     */
    @Override
    public void onStartGamePressed() {
        if(bluetoothHandler.isEnabled()) {
            bluetoothHandler.setDiscoverability();
            bluetoothHandler.startBluetoothServer();
            Toast.makeText(this, "Čeka se drugi uredjaj", Toast.LENGTH_SHORT).show();
        }
        else {
            bluetoothHandler.turnBluetoothOn();
        }
    }

    @Override
    public void onNewWordsPressed() {
        String[] words = gameController.generateWords();
        ((CardFragment)getSupportFragmentManager().findFragmentById(R.id.cardFragment)).updateWords(words);
    }

    @Override
    public void onNewLayoutPressed() {
        GameModel.CardType[] newLayout = gameController.generateLayout();
        CardFragment cardFragment = ((CardFragment)getSupportFragmentManager().findFragmentById(R.id.cardFragment));
        for(int i = 0 ; i < 25 ; i++) {
            switch(newLayout[i]){
                case RED:
                    cardFragment.updateCard(i,R.color.colorRedTeam,false);
                    break;
                case BLANK:
                    cardFragment.updateCard(i,R.color.colorBlank,false);
                    break;
                case BLUE:
                    cardFragment.updateCard(i,R.color.colorBlueTeam,false);
                    break;
                case ASSASSIN:
                    cardFragment.updateCard(i,R.color.colorAssassin,false);
                    break;
            }
        }
    }

    //////////////////////////////////////////////

    //Called by card fragment
    @Override
    public void onCardPressed(int pos) {} // Should never be called, required to be implemented

    //Called by game control fragment
    @Override
    public void onSendCode(String code,String number) {
        if(clickable){
            gameController.sendCode(code,number);
        }
    }

    ///////////////CALLED BY GAME CONTROLLER////////////

    @Override
    public void refreshOnCardClick(int pos) {
        ((CardFragment) getSupportFragmentManager().findFragmentById(R.id.cardFragment)).updateCard(pos,0,true);
    }

    @Override
    public void reconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onStartGamePressed(); // bluetooth is probably on if reconnecting
            }
        });
    }


    ///////////////called when a connection has been established/////////////////////
    @Override
    public void communicate(BluetoothSocket bluetoothSocket)  {
        //Swap control fragments

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Fragment controls = GameControlsFragment.newInstance();
                ft.replace(R.id.controlsFragment,controls);
                ft.commit();
            }
        });

        //go to loop
        gameController.gameLoopMaster(bluetoothSocket);
    }
    ///////////////////////////////////////////////////////////////////////////////

}


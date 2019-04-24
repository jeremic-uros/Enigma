package com.jeremic.uros.enigma.view;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremic.uros.enigma.R;
import com.jeremic.uros.enigma.bluetooth.BluetoothHandler;
import com.jeremic.uros.enigma.controler.ApplicationController;
import com.jeremic.uros.enigma.controler.GameController;
import com.jeremic.uros.enigma.model.GameModel;

public abstract class GameActivity extends AppCompatActivity implements GameView {
    protected boolean clickable;
    protected ApplicationController gameController;
    protected BluetoothHandler bluetoothHandler;
    private GameModel.Turn turn;
    protected boolean visibility;
    protected boolean toReconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        bluetoothHandler = new BluetoothHandler(this);
        if( savedInstanceState == null) {
            turn = GameModel.Turn.BLUE;
            TextView teamName = findViewById(R.id.teamNameTextView);
            teamName.setBackgroundColor(getResources().getColor(R.color.colorBlueTeam));
            gameController = new GameController(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        visibility = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        visibility = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("gameCntrl",gameController);
        switch (turn){
            case BLUE:
                outState.putInt("turn",0); // 0 = blue
                break;
            case RED:
                outState.putInt("turn",1); // 1 = red
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            gameController = (ApplicationController) savedInstanceState.getSerializable("gameCntrl");
            int trn = savedInstanceState.getInt("turn");
            switch (trn){
                case 0:
                    refreshTurnDisplay(GameModel.Turn.BLUE);
                    break;
                case 1:
                    refreshTurnDisplay(GameModel.Turn.RED);
            }
            gameController.setGameView(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gameController.close();
        bluetoothHandler.stopBluetoothServer();
    }

    @Override
    public void refreshTurnDisplay(final GameModel.Turn turn) {
        this.turn = turn;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int color = 0;
                String team = "";
                switch (turn){
                    case RED:
                        team = getString(R.string.redTeam);
                        color = R.color.colorRedTeam;
                        break;
                    case BLUE:
                        team = getString(R.string.blueTeam);
                        color = R.color.colorBlueTeam;
                }
                TextView teamName = findViewById(R.id.teamNameTextView);
                teamName.setBackgroundColor(getResources().getColor(color));
                teamName.setText(team);
            }
        });
    }

    @Override
    public void showError(final String errorMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),errorMsg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showWinner(final String winner) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(),winner + " team wins!!!",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });
    }

    @Override
    public void refreshCards(String[] words) {

    }

    @Override
    public void refreshOnCardClick(int pos) {

    }

    @Override
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    @Override
    public void setCode(String code, String num) {

    }

    @Override
    public void setGameStart(boolean gameStartedFlag ) {
        SharedPreferences flags = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = flags.edit();
        editor.putBoolean("gameStarted",gameStartedFlag);
        editor.apply();
    }
}

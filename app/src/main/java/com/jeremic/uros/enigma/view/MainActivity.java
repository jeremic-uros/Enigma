package com.jeremic.uros.enigma.view;


import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jeremic.uros.enigma.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onCreateGame(View view) {
        Intent startMasterActivity = new Intent(this, MasterActivity.class);
         startActivity(startMasterActivity);
    }


    public void onJoinGame(View view) {
        Intent startAgentActivity = new Intent(this, AgentActivity.class);
        startActivity(startAgentActivity);
    }


    public void onContinueGame(View view) {
        SharedPreferences flags = PreferenceManager.getDefaultSharedPreferences(this);
        if(flags.getBoolean("gameStarted",false)){
            String gameType = flags.getString("gameType","");
            if( gameType.equals("agent")){
                onJoinGame(view);
            }
            else {
                onCreateGame(view);
            }
        }
        else {
            Toast.makeText(this,"Ne postoji igra u toku",Toast.LENGTH_SHORT).show();
        }
    }
    public void onRules(View view) {

    }


}

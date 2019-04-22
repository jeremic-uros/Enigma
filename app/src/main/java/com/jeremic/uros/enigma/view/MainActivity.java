package com.jeremic.uros.enigma.view;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.jeremic.uros.enigma.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void createGame(View view) {
        Intent startMasterActivity = new Intent(this, MasterActivity.class);
         startActivity(startMasterActivity);
    }


    public void joinGame(View view) {
        Intent startAgentActivity = new Intent(this, AgentActivity.class);
        startActivity(startAgentActivity);
    }


    public void continueGame(View view) {

    }
    public void rules(View view) {

    }


}
package com.jeremic.uros.enigma.view;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremic.uros.enigma.R;
import com.jeremic.uros.enigma.bluetooth.BluetoothHandler;
import com.jeremic.uros.enigma.cards.CardFragment;
import com.jeremic.uros.enigma.controler.ApplicationController;
import com.jeremic.uros.enigma.controler.GameController;

public class AgentActivity extends GameActivity implements BluetoothHandler.BluetoothCommunication, GameView, CodeFragment.OnCodeInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clickable = false;
        Fragment codeFrag = CodeFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.controlsFragment, codeFrag).commit();
        bluetoothHandler.turnBluetoothOn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(toReconnect) {
            Toast.makeText(getApplication(), "Povezite se ponovo", Toast.LENGTH_SHORT).show();
            showPairedDevicesDialog();
            toReconnect = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                PairedDevicesDialogFragment pairedDevicesFrag = (PairedDevicesDialogFragment) getSupportFragmentManager().findFragmentByTag("dialog");
                if( pairedDevicesFrag != null) pairedDevicesFrag.updateDeviceList(device);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == BluetoothHandler.REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            showPairedDevicesDialog();
        }
    }

    private void showPairedDevicesDialog(){
        if (bluetoothHandler.isEnabled()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            DialogFragment newFragment = new PairedDevicesDialogFragment();
            newFragment.show(ft, "dialog");
        }
        else {
            bluetoothHandler.turnBluetoothOn();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    //Called by a CARD Fragment
    @Override
    public void onCardPressed(int pos) {
        //Update card
        Log.i("CARD","CARD PRESSED");
        if(clickable) {
            CardFragment cardFragment = ((CardFragment) getSupportFragmentManager().findFragmentById(R.id.cardFragment));
            cardFragment.updateCard(pos, 0, true);
            int color = R.color.colorNeutral;
            switch (gameController.cardPressed(pos)){
                case BLUE:
                    color = R.color.colorBlueTeam;
                    break;
                case RED:
                    color = R.color.colorRedTeam;
                    break;
                case ASSASSIN:
                    color = R.color.colorAssassin;
                    break;
                case BLANK:
                    color = R.color.colorBlank;
                    break;
            }
            cardFragment.updateCard(pos,color,false);
        }
    }


    //Ran in a separate thread after devices are connected
    @Override
    public void communicate(BluetoothSocket bluetoothSocket){
        gameController.gameLoopAgent(bluetoothSocket);
    }


    ///////////////////////CALLED BY CONTROLLER///////////////////
    @Override
    public void refreshCards(final String[] words) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((CardFragment)getSupportFragmentManager().findFragmentById(R.id.cardFragment)).updateWords(words); }
        });
    }

    @Override
    public void setCode(final String code,final String num) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView codeTextView = findViewById(R.id.codeTextView);
                codeTextView.setText(code);
                TextView numTextView = findViewById(R.id.numTextView);
                numTextView.setText(num);
            }
        });

    }

    @Override
    public void reconnect() {
        toReconnect = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(visibility) {
                    Toast.makeText(getApplication(), "Povezite se ponovo", Toast.LENGTH_SHORT).show();
                    showPairedDevicesDialog();
                    toReconnect = false;
                }
            }
        });
    }

    //////////CALLED BY CODE FRAGMENT///////////////////////
    @Override
    public void onConnectButtonPressed() {
        showPairedDevicesDialog();
    }



}

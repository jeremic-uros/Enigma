package com.jeremic.uros.enigma.controler;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import com.jeremic.uros.enigma.model.GameModel;
import com.jeremic.uros.enigma.view.GameView;

import java.io.Serializable;

public interface ApplicationController extends Serializable {

    String[] generateWords();
    GameModel.CardType[] generateLayout();
    void gameLoopAgent(BluetoothSocket bluetoothSocket);
    GameModel.CardType cardPressed(int pos);
    void gameLoopMaster(BluetoothSocket bluetoothSocket);
    void sendCode(String code,String num);
    void close();
    void setGameView(GameView activity);
}

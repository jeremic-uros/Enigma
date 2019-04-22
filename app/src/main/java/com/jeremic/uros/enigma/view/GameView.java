package com.jeremic.uros.enigma.view;

import com.jeremic.uros.enigma.model.GameModel;

public interface GameView {

    void refreshCards(String[] words);
    void refreshOnCardClick(int pos);
    void setClickable(boolean clickable);
    void setCode(String code,String num);
    void refreshTurnDisplay(GameModel.Turn turn);
    void showError(String errorMsg);
    void showWinner(String winner);
    void reconnect();
}

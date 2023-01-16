package com.calamus.smartconningsystempanel.Interfaces;

public interface BluetoothListener {
    void onConnected();
    void onDisconnect();
    void onStartSendingMessage();
    void onMessageSent();
    void onMessageReceived(String message);
    void onErrorSendingMessage(String error);
}

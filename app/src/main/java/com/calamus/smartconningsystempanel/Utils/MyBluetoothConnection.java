package com.calamus.smartconningsystempanel.Utils;

import static com.calamus.smartconningsystempanel.BluetoothActivity.BLUETOOTHADAPTER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.calamus.smartconningsystempanel.Interfaces.BluetoothListener;
import com.calamus.smartconningsystempanel.PanelActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

public class MyBluetoothConnection {
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothListener callbackListener;
    String address;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket = null;
    Activity c;
    Executor postExecutor;

    BufferedReader bufferedReader;


    public MyBluetoothConnection(Activity c,String address,  BluetoothListener callbackListener) {
        this.callbackListener = callbackListener;
        this.address = address;
        this.c=c;

        bluetoothDevice = BLUETOOTHADAPTER.getRemoteDevice(address);
        postExecutor= ContextCompat.getMainExecutor(c);
        reconnect();
    }


    @SuppressLint("MissingPermission")
    public void reconnect(){
        callbackListener.onMessageReceived("Start connecting");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(mUUID);
                    bluetoothSocket.connect();
                    if(bluetoothSocket.isConnected()){
                        callbackListener.onConnected();
                    }else{
                        callbackListener.onDisconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    callbackListener.onDisconnect();
                    callbackListener.onErrorSendingMessage("Connect Err : "+e.toString());
                }
            }
        }).start();
    }

    public void startListening(){
        if(bluetoothSocket.isConnected()){
            try{
                InputStream inputStream=bluetoothSocket.getInputStream();
                InputStreamReader reader=new InputStreamReader(inputStream);
                bufferedReader=new BufferedReader(reader);
                SignalReceiver signalReceiver=new SignalReceiver();
                signalReceiver.start();
            }catch (Exception e){
                callbackListener.onErrorSendingMessage("Msg Received Err : "+e.toString());
                if(bluetoothSocket.isConnected()){
                    callbackListener.onConnected();
                }else{
                    callbackListener.onDisconnect();
                }
            }
        }
    }

    public void sendCommand(final String command){

        callbackListener.onStartSendingMessage();
        callbackListener.onMessageReceived("Start Sending Command");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("Command Protocol ",command);
                try{
                    //sending data

                    OutputStream outputStream=bluetoothSocket.getOutputStream();
                    outputStream.write(command.getBytes());
                    callbackListener.onMessageSent();


                }catch (Exception e){
                    callbackListener.onErrorSendingMessage("Command Sending Err : "+e.toString());
                    if(bluetoothSocket.isConnected()){
                        callbackListener.onConnected();
                    }else{
                        callbackListener.onDisconnect();
                    }
                }
            }
        }).start();
    }

    class SignalReceiver extends Thread{
        @Override
        public void run() {
            super.run();
            while (bluetoothSocket.isConnected()){

                try {
                    if(bufferedReader.ready()){
                        String message=bufferedReader.readLine();
                        callbackListener.onMessageReceived(message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(500);
                }catch (Exception e){

                }
            }
        }
    }
}

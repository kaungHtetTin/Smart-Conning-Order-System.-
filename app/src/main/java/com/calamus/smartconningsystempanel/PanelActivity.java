package com.calamus.smartconningsystempanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.calamus.smartconningsystempanel.Interfaces.BluetoothListener;
import com.calamus.smartconningsystempanel.Utils.Dataset;
import com.calamus.smartconningsystempanel.Utils.MyBluetoothConnection;
import com.calamus.smartconningsystempanel.panels.GaugeView;
import com.calamus.smartconningsystempanel.panels.JoystickView;
import com.calamus.smartconningsystempanel.panels.SteeringGaugeView;
import com.calamus.smartconningsystempanel.panels.SteeringJoystickView;
import com.calamus.smartconningsystempanel.panels.TelegraphJoystickView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;

public class PanelActivity extends AppCompatActivity {

    //steering system
    SteeringGaugeView gv_steering;
    SteeringJoystickView jsv_steering;
    Executor postExecutor;
    int rudderPosition=30;

    TextView tv_cmd_box;
    TextView tv_bluetooth_device_name;
    TextView tv_bluetooth_status;
    ImageView iv_bluetooth_status;


    TextView tv_ship_status;
    ImageView iv_ship_status;

    //telegraph system
    GaugeView gv_portEngine;
    GaugeView gv_startBoardEngine;
    TelegraphJoystickView jsv_portEngine;
    TelegraphJoystickView jsv_startBoardEngine;
    ImageView iv_speed_up,iv_speed_down;

    ImageButton ibt_speak;

    //bluetooth connection
    static final UUID mUUID=UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothSocket bluetoothSocket=null;
    String deviceName;
    String deviceAddress;

    MyBluetoothConnection myBluetoothConnection;

    int portGear,starboardGear;

    int direction=0;

    public static boolean sendingMessage;

    int portGearState=0,stbGearState=0;
    boolean allEngine;

    ArrayList<Integer> rudderValues=new ArrayList<>();
    boolean sendingConningOrder=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        getSupportActionBar().hide();
        postExecutor= ContextCompat.getMainExecutor(this);
        deviceName=getIntent().getExtras().getString("deviceName");
        deviceAddress=getIntent().getExtras().getString("deviceAddress");

        setUpView();
    }

    private void setUpView(){
        gv_steering=findViewById(R.id.gauge_steering);
        jsv_steering=findViewById(R.id.jsv_steering);
        jsv_portEngine=findViewById(R.id.jsv_portEngine);
        jsv_startBoardEngine=findViewById(R.id.jsv_startboardEngine);
        jsv_startBoardEngine.setButtonColor(Color.GREEN);
        jsv_portEngine.setButtonColor(Color.RED);
        gv_portEngine=findViewById(R.id.gv_portEngine);
        gv_startBoardEngine=findViewById(R.id.gv_starBoardEngine);
        ibt_speak=findViewById(R.id.ibt_speak);
        tv_cmd_box=findViewById(R.id.tv_cmd_box);
        tv_bluetooth_device_name=findViewById(R.id.tv_device_name);
        tv_bluetooth_status=findViewById(R.id.tv_bluetooth_status);
        iv_bluetooth_status=findViewById(R.id.iv_bluetooth_status);
        tv_ship_status=findViewById(R.id.tv_ship_status);
        iv_ship_status=findViewById(R.id.iv_ship_direction);


        iv_speed_up=findViewById(R.id.iv_speed_up);
        iv_speed_down=findViewById(R.id.iv_speed_down);

        setSteeringControlView();
        setTelegraphControlView();
        setMic();
        takeRecordPermission();

        tv_bluetooth_device_name.setText(deviceName);

        myBluetoothConnection=new MyBluetoothConnection(this, deviceAddress, new BluetoothListener() {
            @Override
            public void onConnected() {
                myBluetoothConnection.startListening();
               postExecutor.execute(new Runnable() {
                   @Override
                   public void run() {
                       iv_bluetooth_status.setImageResource(R.drawable.ic_baseline_bluetooth_connected_24);
                       tv_bluetooth_status.setText("Connected");
                   }
               });
            }

            @Override
            public void onDisconnect() {
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        iv_bluetooth_status.setImageResource(R.drawable.ic_bluetooth_disconnect);
                        tv_bluetooth_status.setText("Disconnected");
                    }
                });
            }

            @Override
            public void onStartSendingMessage() {
                sendingMessage=true;
            }

            @Override
            public void onMessageSent() {

                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        allEngine=false;
                        tv_cmd_box.setText("Command sent");
                    }
                });
            }

            @Override
            public void onMessageReceived(String message) {
                sendingMessage=false;
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_cmd_box.setText(message);
                    }
                });
            }

            @Override
            public void onErrorSendingMessage(String error) {
                sendingMessage=false;
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_cmd_box.setText(error);
                    }
                });
            }
        });

        tv_bluetooth_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_bluetooth_status.setText("Reconnecting");
                iv_bluetooth_status.setImageResource(R.drawable.ic_bluetooth_connecting);
                myBluetoothConnection.reconnect();
            }
        });


        iv_speed_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(direction==0)setShipStatus(1);
                speedUp();
            }
        });

        iv_speed_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(direction==0)setShipStatus(-1);
                speedDown();
            }
        });

        setShipStatus(0);

    }

    private void takeRecordPermission() {
        if(!isPermitted()){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},101);
        }
    }

    private boolean isPermitted() {
        return  (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private void setMic(){
        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null)
                    Log.e("Command : ",matches.get(0)+"");
                    //tv_cmd_box.setText(matches.get(0)+"");
                    calculateVoiceCommand(matches.get(0));

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        ibt_speak.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        //  tv_speech.setHint("You will see input here");
                        tv_cmd_box.setText("Command Box");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        tv_cmd_box.setText("Listening...");
                        break;
                }
                return false;
            }
        });
    }

    private void setSteeringControlView(){
        gv_steering.setTargetValue(rudderPosition);
        jsv_steering.setOnJoystickMoveListener(new SteeringJoystickView.OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                // TODO Auto-generated method stub

                power=power/20;

                if(direction== JoystickView.LEFT){
                    if(rudderPosition<61){
                        rudderPosition=rudderPosition+power;
                        gv_steering.setTargetValue(rudderPosition);
                    }
                }else{
                    if(rudderPosition>0){
                        rudderPosition=rudderPosition-power;
                        gv_steering.setTargetValue(rudderPosition);
                    }
                }
                int value;
                boolean starboard=false;
                if(rudderPosition>30){
                    value=rudderPosition-30;

                    starboard=true;
                }else{
                    value=30-rudderPosition;
                }
                rudderValues.add(value);
                if(!sendingConningOrder)sendConningOrderFromJoystick(starboard);
            }
        }, SteeringJoystickView.DEFAULT_LOOP_INTERVAL);
    }

    private void setTelegraphControlView(){
        gv_portEngine.setTargetValue(0);
        gv_startBoardEngine.setTargetValue(0);
        final boolean[] portAhead = new boolean[1];
        final boolean[] stbAhead = {false};
        final int [] portPower={0};
        final int [] stbPower={0};

        jsv_portEngine.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {

                if(portPower[0]==0){
                    portAhead[0] =direction==0;
                }
                power=power/20;
                gv_portEngine.setTargetValue(power);
                portPower[0]=power;

                if(portGearState!=power){
                    portGearState=power;
                    portGear=power;
                    if(portAhead[0]){
                        if(allEngine) myBluetoothConnection.sendCommand("23"+power+"0");
                        else myBluetoothConnection.sendCommand("33"+power+"0");
                    }else{
                        if(allEngine) myBluetoothConnection.sendCommand("24"+power+"0");
                        else myBluetoothConnection.sendCommand("34"+power+"0");
                    }

                }

                if(portAhead[0]&&stbAhead[0]||portAhead[0]&&stbPower[0]==0){
                    setShipStatus(1);

                }else if(stbPower[0]==0&&portPower[0]==0){
                    setShipStatus(0);
                    myBluetoothConnection.sendCommand("3");
                }else{
                    setShipStatus(-1);
                }

            }
        },SteeringJoystickView.DEFAULT_LOOP_INTERVAL);

        jsv_startBoardEngine.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {

                if(stbPower[0]==0){
                    stbAhead[0] =direction==0;
                }

                power=power/20;
                gv_startBoardEngine.setTargetValue(power);

                stbPower[0]=power;

                if(stbGearState!=power){
                    stbGearState=power;
                    starboardGear=power;
                    if(stbAhead[0]){
                       if(!allEngine) myBluetoothConnection.sendCommand("43"+power+"0");
                    }else{
                       if(!allEngine)  myBluetoothConnection.sendCommand("44"+power+"0");
                    }
                }

                if(portAhead[0]==stbAhead[0]){
                    if(portAhead[0]&&stbAhead[0]||stbAhead[0]&&portPower[0]==0){
                        setShipStatus(1);
                        ;
                    }else if(stbPower[0]==0&&portPower[0]==0){
                        setShipStatus(0);
                    }else{
                        setShipStatus(-1);

                    }
                }else{
                    setShipStatus(2);
                }
                allEngine=false;
            }
        },SteeringJoystickView.DEFAULT_LOOP_INTERVAL);
    }


    private void calculateVoiceCommand(String voiceCommand){
        //turning left

        if(checkingVoiceCommand(Dataset.midShip,voiceCommand)){
            tv_cmd_box.setText("midship");
            gv_steering.setTargetValue(30);
            myBluetoothConnection.sendCommand("1100");
        }

       if(checkingVoiceCommand(Dataset.turningLeft,voiceCommand)){
           tv_cmd_box.setText("Turning left");
           turning(-5);
           return;
       }

        if(checkingVoiceCommand(Dataset.turningRight,voiceCommand)){
            tv_cmd_box.setText("Turning Right");
            turning(5);
            return;
        }

        if(checkingVoiceCommand(Dataset.forwarding,voiceCommand)){
            tv_cmd_box.setText("going forward");
            if(direction>=0){
                setShipStatus(1);
                speedUp();
            }else{
                tv_cmd_box.setText("Can't go forward now. Please make sure all engines Idle.");
            }
            return;
        }

        if(checkingVoiceCommand(Dataset.backward,voiceCommand)){
            tv_cmd_box.setText("going backward");
            if(direction<=0){
                setShipStatus(-1);
                speedUp();
            }else{
                tv_cmd_box.setText("Can't go back now. Please make sure all engines Idle.");
            }
            return;
        }

        if(checkingVoiceCommand(Dataset.speedUp,voiceCommand)){
            tv_cmd_box.setText("Speeding Up");
            speedUp();
            return;
        }

        if(checkingVoiceCommand(Dataset.speedDown,voiceCommand)){
            tv_cmd_box.setText("Speeding Down");
            speedDown();
            return;
        }


        if(checkingConningOrder(voiceCommand)!=null){
            tv_cmd_box.setText(checkingConningOrder(voiceCommand));
            return;
        }

        tv_cmd_box.setText(voiceCommand);


    }

    private boolean checkingVoiceCommand(String [] arr,String voiceCommand){
        for(int i=0;i<arr.length;i++){
            if(isSpeechCorrect(arr[i],voiceCommand)){
                return true;
            }
        }
        return false;
    }

    private String checkingConningOrder(String command){
        command=command.toLowerCase();

        boolean containDigit=false;
        char[] chars = command.toCharArray();
        int value=0;
        StringBuilder sb = new StringBuilder();
        for(char c : chars){
            if(Character.isDigit(c)){
                sb.append(c);
                containDigit=true;
            }
        }
        if(containDigit){
            value=Integer.parseInt(sb.toString());
            if(value>0 && value<35){
                if(command.charAt(0)=='p' ){
                    //dive the command here "11"+value
                    rudderPosition=30-value;
                    gv_steering.setTargetValue(rudderPosition);
                    myBluetoothConnection.sendCommand("11"+value);
                    return "Port "+value;
                }
                if(command.charAt(0)=='s' ||command.charAt(0)=='d' )
                {
                    //dive the command here "12"+value
                    rudderPosition=30+value;
                    gv_steering.setTargetValue(rudderPosition);
                    myBluetoothConnection.sendCommand("12"+value);
                    return "Starboard "+value;
                }

            }else{
                return null;
            }
        }else{
            return null;
        }

        return null;

    }

    private boolean isSpeechCorrect(String a,String b){
        a=a.toLowerCase();
        b=b.toLowerCase();
        a=a.replaceAll("\\p{Punct}","");
        b=b.replaceAll("\\p{Punct}","");

        return a.equals(b);
    }



    // handing the ship
    private void speedUp(){
        allEngine=true;
        if(direction==0){
            tv_cmd_box.setText("Please decide going forwarding or backward!");
            return;
        }
        if(portGear<5){
            portGear++;
            starboardGear++;
            jsv_portEngine.setJoystick(starboardGear*direction);
            jsv_startBoardEngine.setJoystick(starboardGear*direction);
        }else{
            tv_cmd_box.setText("Cannot speed up anymore");
        }
    }

    private void speedDown(){
        allEngine=true;
        if(direction==0){
            tv_cmd_box.setText("Please decide going forwarding or backward!");
            return;
        }

        if(portGear>0 && starboardGear>0){
            portGear--;
            starboardGear--;
            if(portGear==0&&starboardGear==0){
                setShipStatus(0);
            }
            jsv_portEngine.setJoystick(portGear*direction);
            jsv_startBoardEngine.setJoystick(starboardGear*direction);
        }else{
            tv_cmd_box.setText("Cannot speed down anymore");
            setShipStatus(0);
        }
    }

    private void turning(int i){
        if(rudderPosition>0&&i<0 || rudderPosition<61&&i>0){
            rudderPosition=rudderPosition+i;
            gv_steering.setTargetValue(rudderPosition);
        }

        int value;
        if(rudderPosition>30){
            value=rudderPosition-30;
            myBluetoothConnection.sendCommand("12"+value);
            Log.e("starboard",value+"");
        }else{
            value=30-rudderPosition;
            Log.e("Port",value+"");
            myBluetoothConnection.sendCommand("11"+value);
        }
    }



    private void setShipStatus(int dir){
        // 1 for forward
        //-1 for backward
        //0 for Idle
        // 2 for rotate

        if(dir==2){
            tv_ship_status.setText("Turning");
            iv_ship_status.setImageResource(R.drawable.ic_ship_rotate);
            return;
        }

        direction=dir;
        if(direction==1){
            tv_ship_status.setText("Forward");
            iv_ship_status.setImageResource(R.drawable.ic_ship_forward);
        }else if(direction==-1){
            tv_ship_status.setText("Backward");
            iv_ship_status.setImageResource(R.drawable.ic_ship_backward);
        }else if(direction==0){
            tv_ship_status.setText("Idle");
            iv_ship_status.setImageResource(R.drawable.ic_ship_idle);
        }
    }

    private void sendConningOrderFromJoystick(boolean starboard){
        sendingConningOrder=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(starboard) myBluetoothConnection.sendCommand("12"+rudderValues.get(rudderValues.size()-1)+" ");
                else myBluetoothConnection.sendCommand("11"+rudderValues.get(rudderValues.size()-1)+" ");

                Log.e("Conning order","Starboard "+starboard+ "and value - "+rudderValues.get(rudderValues.size()-1));
                rudderValues.clear();
                sendingConningOrder=false;
            }
        }).start();
    }
}
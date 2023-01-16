package com.calamus.smartconningsystempanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.calamus.smartconningsystempanel.Adapters.BluetoothDeviceAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    ArrayList<BluetoothDevice> boundedDevices =new ArrayList<>();
    RecyclerView recyclerView;
    BluetoothDeviceAdapter adapter;
    TextView tv;

    public static BluetoothAdapter BLUETOOTHADAPTER;

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        getSupportActionBar().hide();

        tv=findViewById(R.id.tv_status);
        recyclerView=findViewById(R.id.recyclerView);
        tv.setText("Paired Devices");

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new BluetoothDeviceAdapter(this,boundedDevices);
        recyclerView.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_CONNECT},101);
        }else{
            ActivityCompat.requestPermissions(this,getRequiredPermissions(),101);
        }

        BLUETOOTHADAPTER = BluetoothAdapter.getDefaultAdapter();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Devices ", String.valueOf(BLUETOOTHADAPTER.getBondedDevices()));
            Set<BluetoothDevice> devices=BLUETOOTHADAPTER.getBondedDevices();
            boundedDevices.addAll(devices);
            adapter.notifyDataSetChanged();
        }else{


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_CONNECT},101);
            }else{
                ActivityCompat.requestPermissions(this,getRequiredPermissions(),101);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==101){
            onRestart();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
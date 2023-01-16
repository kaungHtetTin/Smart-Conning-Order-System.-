package com.calamus.smartconningsystempanel.Adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.smartconningsystempanel.PanelActivity;
import com.calamus.smartconningsystempanel.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.Holder> {

    private final Activity c;
    ArrayList<BluetoothDevice> data;
    private final LayoutInflater mInflater;

    public BluetoothDeviceAdapter(Activity c, ArrayList<BluetoothDevice> data) {
        this.c = c;
        this.data = data;
        this.mInflater = LayoutInflater.from(c);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_bluetooth, parent, false);

        return new Holder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        BluetoothDevice bluetoothDevice = data.get(position);

        holder.tv_device_name.setText(bluetoothDevice.getName());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_device_name;
        public Holder(@NonNull View itemView) {
            super(itemView);
            tv_device_name=itemView.findViewById(R.id.tv_device_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onClick(View view) {
                    BluetoothDevice device=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, PanelActivity.class);
                    intent.putExtra("deviceName",device.getName());
                    intent.putExtra("deviceAddress",device.getAddress());
                    c.startActivity(intent);
                }
            });
        }
    }
}

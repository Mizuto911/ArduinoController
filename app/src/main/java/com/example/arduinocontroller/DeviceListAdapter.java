package com.example.arduinocontroller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceInfoHolder> {

    Context context;
    ArrayList<BluetoothDevice> devicesList;
    private final DeviceListInterface deviceListInterface;

    public DeviceListAdapter(Context context, ArrayList<BluetoothDevice> devicesList, DeviceListInterface deviceListInterface){
        this.context = context;
        this.devicesList = devicesList;
        this.deviceListInterface = deviceListInterface;
    }

    @NonNull
    @Override
    public DeviceListAdapter.DeviceInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.bluetooth_device_item, parent, false);
        return new DeviceListAdapter.DeviceInfoHolder(view, deviceListInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListAdapter.DeviceInfoHolder holder, int position) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String deviceName = devicesList.get(position).getName();
        String noName = "Unnamed Device";
        String deviceAddress = devicesList.get(position).getAddress();
        if(TextUtils.isEmpty(deviceName)){
            holder.deviceNameText.setText(noName);
        }else {
            holder.deviceNameText.setText(deviceName);
        }
        holder.deviceAddressText.setText(deviceAddress);
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }

    public static class DeviceInfoHolder extends RecyclerView.ViewHolder{
        TextView deviceNameText, deviceAddressText;
        public DeviceInfoHolder(@NonNull View itemView, DeviceListInterface deviceListInterface) {
            super(itemView);

            deviceNameText = itemView.findViewById(R.id.deviceName);
            deviceAddressText = itemView.findViewById(R.id.deviceAddress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(deviceListInterface != null){
                        int position = getAdapterPosition();

                        if(position != RecyclerView.NO_POSITION){
                            deviceListInterface.onDeviceClick(position);
                        }
                    }
                }
            });
        }
    }
}

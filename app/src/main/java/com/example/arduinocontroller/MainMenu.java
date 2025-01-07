package com.example.arduinocontroller;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainMenu extends AppCompatActivity implements DeviceListInterface{

    LinearLayout connectButton;
    BluetoothAdapter btAdapter;
    BroadcastReceiver deviceReceiver;
    ArrayList<BluetoothDevice> detectedDevices = new ArrayList<>();
    DeviceListAdapter deviceListAdapter;
    RecyclerView deviceList;
    ProgressBar dialogProgressBar;
    Dialog deviceSelectionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                ActivityCompat.requestPermissions(MainMenu.this,new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN
                , Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},2);
            }
        }

        connectButton = findViewById(R.id.connectButton);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!btAdapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT,2);
        }

        deviceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String result = intent.getAction();
                if(result.equals(BluetoothDevice.ACTION_FOUND)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ActivityCompat.checkSelfPermission(MainMenu.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    detectedDevices.add(device);
                    Log.d("Devices", "Name: " + device.getName() + " Address: " + device.getAddress());
                    deviceListAdapter.notifyItemInserted(detectedDevices.size());
                    dialogProgressBar.setVisibility(View.GONE);
                }
            }
        };

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpDeviceSelectionDialog();
            }
        });
    }

    private void setUpDeviceSelectionDialog() {

        detectedDevices = new ArrayList<BluetoothDevice>();

        deviceSelectionDialog = new Dialog(MainMenu.this);
        deviceSelectionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deviceSelectionDialog.setContentView(R.layout.device_selection_dialog);

        ImageView exitButton = deviceSelectionDialog.findViewById(R.id.exitBtn);
        TextView noDevicesText = deviceSelectionDialog.findViewById(R.id.emptyText);
        dialogProgressBar = deviceSelectionDialog.findViewById(R.id.progressBar);
        deviceList = deviceSelectionDialog.findViewById(R.id.deviceList);
        deviceListAdapter = new DeviceListAdapter(MainMenu.this, detectedDevices,MainMenu.this);
        deviceList.setAdapter(deviceListAdapter);
        deviceList.setLayoutManager(new LinearLayoutManager(MainMenu.this));

        dialogProgressBar.setVisibility(View.VISIBLE);
        discoverDevices();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainMenu.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if(detectedDevices.isEmpty()){
                    dialogProgressBar.setVisibility(View.GONE);
                    noDevicesText.setVisibility(View.VISIBLE);
                    btAdapter.cancelDiscovery();
                }
            }
        },10000);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainMenu.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                deviceSelectionDialog.dismiss();
            }
        });

        deviceSelectionDialog.show();
        deviceSelectionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (ActivityCompat.checkSelfPermission(MainMenu.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                btAdapter.cancelDiscovery();
                Log.d("Check", "Bluetooth Discover Cancelled");
                unregisterReceiver(deviceReceiver);
                Log.d("Check", "Bluetooth Receiver Unregistered");
            }
        });
        deviceSelectionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        deviceSelectionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void discoverDevices() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Can't Scan for Devices (No Permission for Bluetooth)", Toast.LENGTH_LONG).show();
            return;
        }

        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }

        btAdapter.startDiscovery();
        IntentFilter discoverFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(deviceReceiver, discoverFilter);
    }

    @Override
    public void onDeviceClick(int position) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String deviceAddress = detectedDevices.get(position).getAddress();
        deviceSelectionDialog.dismiss();

        Intent intent = new Intent(MainMenu.this, MainActivity.class);
        intent.putExtra("DeviceAddress",deviceAddress);
        startActivity(intent);
    }


}
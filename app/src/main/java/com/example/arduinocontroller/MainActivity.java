package com.example.arduinocontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public final UUID btID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int connectionAttempts = 5;
    BluetoothAdapter btAdapter;
    BluetoothSocket btSocket;
    BluetoothDevice hc05;
    OutputStream outputStream;
    ImageView upButton, downButton, leftButton, rightButton, menuButton, disconnectButton;
    TextView modeIndicatorText;
    ConstraintLayout progressBar;
    AlertDialog disconnectionDialog;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @SuppressLint("ClickableViewAccessibility")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().getDecorView().setSystemUiVisibility(flags);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0){
                    getWindow().getDecorView().setSystemUiVisibility(flags);
                }
            }
        });

        upButton = findViewById(R.id.upArrow);
        downButton = findViewById(R.id.downArrow);
        leftButton = findViewById(R.id.leftArrow);
        rightButton = findViewById(R.id.rightArrow);
        menuButton = findViewById(R.id.menuBtn);
        disconnectButton = findViewById(R.id.disconnectBtn);
        modeIndicatorText = findViewById(R.id.modeIndicator);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        progressBar = findViewById(R.id.progressBar);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.BLUETOOTH_CONNECT},2);
                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setUpDisconnectionDialog();
            }
        });

        String hc05Code = getIntent().getStringExtra("DeviceAddress");
        hc05 = btAdapter.getRemoteDevice(hc05Code);

        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Connecting to Bluetooth Module", Toast.LENGTH_SHORT).show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Please Grant Bluetooth Permissions", Toast.LENGTH_SHORT).show();
                    finish();
                }
                int counter = 0;
                try {
                    btSocket = hc05.createRfcommSocketToServiceRecord(btID);
                    while(!btSocket.isConnected() && counter <= connectionAttempts){
                        btSocket.connect();
                        counter++;
                    }
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Connection Successful", Toast.LENGTH_SHORT).show();
                    outputStream = btSocket.getOutputStream();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Couldn't Connect to Device", Toast.LENGTH_LONG).show();
                    Toast.makeText(MainActivity.this, "Make Sure the Device you Selected is HC-05", Toast.LENGTH_LONG).show();
                    throw new RuntimeException(e);
                }
            }
        },500);

        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try{
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            upButton.setColorFilter(Color.parseColor("#34E0B4"));
                            outputStream.write('W');
                            break;
                        case MotionEvent.ACTION_UP:
                            upButton.clearColorFilter();
                            outputStream.write('w');
                            break;
                    }
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
                return true;
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try{
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            downButton.setColorFilter(Color.parseColor("#34E0B4"));
                            outputStream.write('S');
                            break;
                        case MotionEvent.ACTION_UP:
                            downButton.clearColorFilter();
                            outputStream.write('s');
                            break;
                    }
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
                return true;
            }
        });

        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try{
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            leftButton.setColorFilter(Color.parseColor("#34E0B4"));
                            outputStream.write('A');
                            break;
                        case MotionEvent.ACTION_UP:
                            leftButton.clearColorFilter();
                            outputStream.write('a');
                            break;
                    }
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
                return true;
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try{
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            rightButton.setColorFilter(Color.parseColor("#34E0B4"));
                            outputStream.write('D');
                            break;
                        case MotionEvent.ACTION_UP:
                            rightButton.clearColorFilter();
                            outputStream.write('d');
                            break;
                    }
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
                return true;
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpModeSelectionDialog();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpDisconnectionDialog();
            }
        });
    }

    private void setUpModeSelectionDialog() {
        final Dialog modeSelectionDialog = new Dialog(MainActivity.this);
        modeSelectionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        modeSelectionDialog.setContentView(R.layout.mode_selection_dialog);

        ImageView exitButton = modeSelectionDialog.findViewById(R.id.exitBtn);
        CardView lineTraceButton = modeSelectionDialog.findViewById(R.id.lineTraceBtn);
        CardView objAvoidButton = modeSelectionDialog.findViewById(R.id.objAvoidBtn);
        CardView bothModeButton = modeSelectionDialog.findViewById(R.id.bothModeBtn);
        CardView remoteControlButton = modeSelectionDialog.findViewById(R.id.remoteControlBtn);
        TextView noModeButton = modeSelectionDialog.findViewById(R.id.noModeBtn);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeSelectionDialog.dismiss();
            }
        });

        lineTraceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    outputStream.write('L');
                    String displayText = "Current Mode: Line Trace";
                    modeIndicatorText.setText(displayText);
                    modeSelectionDialog.dismiss();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        objAvoidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    outputStream.write('O');
                    String displayText = "Current Mode: Object Avoidance";
                    modeIndicatorText.setText(displayText);
                    modeSelectionDialog.dismiss();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        bothModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    outputStream.write('B');
                    String displayText = "Current Mode: Line Tracing & Object Avoidance";
                    modeIndicatorText.setText(displayText);
                    modeSelectionDialog.dismiss();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        remoteControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    outputStream.write('R');
                    String displayText = "Current Mode: Remote Control";
                    modeIndicatorText.setText(displayText);
                    modeSelectionDialog.dismiss();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        noModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    outputStream.write('N');
                    String displayText = "Current Mode: NONE";
                    modeIndicatorText.setText(displayText);
                    modeSelectionDialog.dismiss();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        modeSelectionDialog.show();
        modeSelectionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        modeSelectionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void setUpDisconnectionDialog() {
        disconnectionDialog = new AlertDialog.Builder(MainActivity.this).create();
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        disconnectionDialog.dismiss();
                        try {
                            outputStream.write('N');
                            btSocket.close();
                            finish();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        disconnectionDialog.dismiss();
                        break;
                }
            }
        };

        disconnectionDialog.setTitle("Disconnect Device");
        disconnectionDialog.setMessage("Do you want to disconnect the device and return to the Main Menu?");
        disconnectionDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", dialogClickListener);
        disconnectionDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", dialogClickListener);
        disconnectionDialog.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
}
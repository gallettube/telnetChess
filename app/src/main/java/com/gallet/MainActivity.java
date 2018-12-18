package com.gallet;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements TelnetNotificationHandler, Runnable {

    public static final int STATE_IDLE = -1;
    public static final int STATE_CONNECT = 1;
    public static final int STATE_READ = 2;
    public static final int STATE_DISCONNECT = 3;
    public static final int STATE_WRITE = 4;
    public static final int STATE_CONNECT_AND_READ = 5;


    int state = STATE_IDLE;

    TelnetClient tc = new TelnetClient();
    boolean connected = false;

    String ip = "freechess.org";
    int port = 5000;

    //String ip = "towel.blinkenlights.nl";
    //int port = 23;

    String cmd = "";
    StringBuffer sBuf;

    TextView areaText;
    EditText edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        areaText = findViewById(R.id.areaText);
        edit = findViewById(R.id.write_data);

        showHelp();

        findViewById(R.id.button1).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, edit.getText().toString(), Toast.LENGTH_SHORT).show();
                sendCommand();
            }
        });

        findViewById(R.id.buttonConnect).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

    }


    void sendCommand(){
        cmd = ((EditText)findViewById(R.id.write_data)).getText().toString();
        ((EditText)findViewById(R.id.write_data)).setText("");

        cmd = cmd.trim();
        if(connected){
            write();
            return;
        } else if( !connected){
            connect();
            return;
        }
    }

    void showHelp(){
        areaText.append("\n\t------ Telnet ------\n");
        areaText.append("\n");
    }

    @Override
    public void receivedNegotiation(int negotiation_code, int option_code) {
        areaText.append("negotiation_code: " + negotiation_code + " option_code: " + option_code);
    }

    @Override
    public void run() {
        System.out.println("ESTE ES EL PUTO ESTADO"+state);
        switch (state){
            case STATE_CONNECT:
                connect();
                break;
            case STATE_READ:
                read();
                break;
            case STATE_DISCONNECT:
                disconnect();
                break;
            case STATE_WRITE:
                write();
                break;
            case STATE_CONNECT_AND_READ:
                connect();
                read();
                break;
        }
    }

    void connect(){
        tc.registerNotifHandler(this);
        try {
            tc.connect(ip, port);
            connected = true;
            // I read when connect coz else it never fires
            read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void read(){
        InputStream instr = tc.getInputStream();
        try
        {
            byte[] buff = new byte[1024];
            int ret_read;
            while (connected && (ret_read = instr.read(buff)) != -1){
                sBuf = new StringBuffer();
                sBuf.append(new String(buff,0,ret_read));
                System.out.println("out" +sBuf.toString());
                runOnUiThread(() -> {
                    areaText.append(sBuf.toString());
                });
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void write() {
        OutputStream os = tc.getOutputStream();
        try {
            os.write((edit.getText().toString() + '\r').getBytes());
            os.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void disconnect(){
        connected = false;
        try {
            tc.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tc.unregisterNotifHandler();
    }
}

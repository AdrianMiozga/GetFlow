package com.wentura.pomodoroapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");

        Log.d("Wentura", "" + intent.getStringExtra("action"));

        Intent intentMainActivity = new Intent(context, MainActivity.class);
        intent.putExtra("attachMedia", true);
        context.startActivity(intentMainActivity);

        switch (action) {
            case "Skip":
                Log.d("Log", "Skip");
                break;
            case "StartPause":
                Log.d("Log", "StartPause");
                break;
            case "Stop":
                Log.d("Log", "Stop");
                break;
        }
        Intent closeNotificationTray = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeNotificationTray);
    }
}

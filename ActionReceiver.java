package com.wentura.pomodoroapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ActionReceiver extends BroadcastReceiver {
    private static final String TAG = ActionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");

        switch (action) {
            case "Skip":
                Log.d(TAG, "Skip");
                break;
            case "PauseResume":
                Log.d(TAG, "PauseResume");
                break;
            case "Stop":
//                TimerUtils.stopTimer();
                Log.d(TAG, "Stop");
                break;
        }
        Intent closeNotificationTray = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeNotificationTray);
    }
}

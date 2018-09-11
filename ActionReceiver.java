package com.wentura.pomodoroapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class ActionReceiver extends BroadcastReceiver {
    private static final String TAG = ActionReceiver.class.getSimpleName();
    public static final String BUTTON_CLICKED = "BUTTON_CLICKED";
    public static final String BUTTON_ACTION = "BUTTON_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");

        Intent localIntent = new Intent(BUTTON_CLICKED);
        localIntent.putExtra(BUTTON_ACTION, action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

        if (action.equals("Stop")) {
            Intent closeNotificationTray = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeNotificationTray);
        }
    }
}

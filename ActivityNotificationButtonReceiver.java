package com.wentura.pomodoroapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivityNotificationButtonReceiver extends BroadcastReceiver {
    private static final String TAG = ActivityNotificationButtonReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Constants.BUTTON_ACTION);

        Intent localIntent = new Intent(Constants.BUTTON_CLICKED);
        localIntent.putExtra(Constants.BUTTON_ACTION, action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

        if (action.equals(Constants.BUTTON_STOP)) {
            Intent closeNotificationTray = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeNotificationTray);
        }
    }
}

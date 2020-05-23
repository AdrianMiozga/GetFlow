package com.wentura.focus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

class TimerNotification {
    private Context context;
    private boolean isTimerRunning;

    NotificationCompat.Builder buildNotification(Context context, boolean isTimerRunning) {
        this.context = context;
        this.isTimerRunning = isTimerRunning;
        return setupNotification();
    }

    private NotificationCompat.Builder setupNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_TIMER)
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setShowWhen(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            builder.setContentTitle(context.getString(R.string.app_name));
        }

        if (isTimerRunning) {
            builder.addAction(R.drawable.ic_play_button, context.getString(R.string.pause),
                    createButtonPendingIntent(Constants.BUTTON_PAUSE));
        } else {
            builder.addAction(R.drawable.ic_play_button, context.getString(R.string.resume),
                    createButtonPendingIntent(Constants.BUTTON_START));
        }

        builder.addAction(R.drawable.ic_skip_button, context.getString(R.string.skip),
                createButtonPendingIntent(Constants.BUTTON_SKIP));

        builder.addAction(R.drawable.ic_stop_button, context.getString(R.string.stop),
                createButtonPendingIntent(Constants.BUTTON_STOP));

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(pendingIntent);
        return builder;
    }

    private PendingIntent createButtonPendingIntent(String actionValue) {
        return PendingIntent.getBroadcast(context, getRequestCode(actionValue),
                createButtonIntent(actionValue), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int getRequestCode(String actionValue) {
        switch (actionValue) {
            case Constants.BUTTON_SKIP:
                return Constants.PENDING_INTENT_SKIP_REQUEST_CODE;
            case Constants.BUTTON_START:
                return Constants.PENDING_INTENT_PAUSE_RESUME_REQUEST_CODE;
            case Constants.BUTTON_STOP:
                return Constants.PENDING_INTENT_STOP_REQUEST_CODE;
            default:
                return -1;
        }
    }

    @NonNull
    private Intent createButtonIntent(String actionValue) {
        Intent buttonIntent = new Intent(context, NotificationButtonReceiver.class);
        buttonIntent.putExtra(Constants.BUTTON_ACTION, actionValue);
        return buttonIntent;
    }
}

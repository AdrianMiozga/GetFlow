package com.wentura.pomodoro;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

class TimerNotification {

    private static final String TAG = TimerNotification.class.getSimpleName();
    private Context context;
    private boolean isTimerRunning;
    private boolean isBrakeState;
    private boolean isNotificationCreatedFromActivity;

    NotificationCompat.Builder buildNotification(Context context, long millisUntilFinished,
                                                 boolean isBreakState, boolean isTimerRunning,
                                                 boolean isNotificationCreatedFromActivity) {
        this.context = context;
        this.isTimerRunning = isTimerRunning;
        this.isBrakeState = isBreakState;
        this.isNotificationCreatedFromActivity = isNotificationCreatedFromActivity;
        return setupNotification(millisUntilFinished);
    }

    private NotificationCompat.Builder setupNotification(long millisUntilFinished) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_TIMER)
                .setSmallIcon(R.drawable.ic_logo)
                .setColor(context.getColor(R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(context.getString(R.string.pomodoro))
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setShowWhen(false);

        if (isTimerRunning) {
            builder.addAction(R.drawable.ic_play_button, context.getString(R.string.pause),
                    createButtonPendingIntent(Constants.BUTTON_PAUSE_RESUME));
        } else {
            builder.addAction(R.drawable.ic_play_button, context.getString(R.string.resume),
                    createButtonPendingIntent(Constants.BUTTON_PAUSE_RESUME));
        }

        builder.addAction(R.drawable.ic_skip_button, context.getString(R.string.skip),
                createButtonPendingIntent(Constants.BUTTON_SKIP));

        builder.addAction(R.drawable.ic_stop_button, context.getString(R.string.stop),
                createButtonPendingIntent(Constants.BUTTON_STOP));

        if (!isNotificationCreatedFromActivity) {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    Constants.PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentIntent(pendingIntent);
            Log.d(TAG, "setupNotification: isNotificationCreatedFromActivity");
        } else {
            Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    Constants.PENDING_INTENT_TO_CLOSE_TRAY, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentIntent(pendingIntent);
        }

        if (isBrakeState) {
            builder.setContentText(context.getString(R.string.break_time_left,
                    Utility.formatTime(context, millisUntilFinished)));
        } else {
            builder.setContentText(context.getString(R.string.work_time_left,
                    Utility.formatTime(context, millisUntilFinished)));
        }
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
            case Constants.BUTTON_PAUSE_RESUME:
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

        if (isNotificationCreatedFromActivity) {
            buttonIntent.putExtra(Constants.IS_NOTIFICATION_OPENED_FROM_ACTIVITY, true);
        } else {
            buttonIntent.putExtra(Constants.IS_NOTIFICATION_OPENED_FROM_ACTIVITY, false);
        }
        return buttonIntent;
    }
}

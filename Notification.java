package com.wentura.pomodoroapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

class Notification {

    private Context context;
    private boolean timeLeftNotificationFirstTime;
    private boolean isTimerRunning;
    private boolean isBrakeState;
    private boolean isNotificationOpenedFromActivity;

    void buildNotification(Context context, long millisUntilFinished,
                           boolean timeLeftNotificationFirstTime, boolean breakState,
                           boolean timerIsRunning, boolean isNotificationOpenedFromActivity) {
        this.context = context;
        this.timeLeftNotificationFirstTime = timeLeftNotificationFirstTime;
        this.isTimerRunning = timerIsRunning;
        this.isBrakeState = breakState;
        this.isNotificationOpenedFromActivity = isNotificationOpenedFromActivity;

        setupNotification(millisUntilFinished);
    }

    private void setupNotification(long millisUntilFinished) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.CHANNEL_TIMER)
                .setSmallIcon(R.drawable.ic_logo)
                .setColor(context.getColor(R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(Constants.POMODORO)
                .setOngoing(true)
                .setShowWhen(false);

        addButtonsToNotification(mBuilder);

        createIntentToOpenApp(mBuilder);

        setTimeLeftNotificationContent(millisUntilFinished, mBuilder);
        displayNotification(mBuilder);
    }

    private void displayNotification(NotificationCompat.Builder mBuilder) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.TIME_LEFT_NOTIFICATION, mBuilder.build());
    }

    private void setTimeLeftNotificationContent(long millisUntilFinished, NotificationCompat.Builder mBuilder) {
        // if (!timeLeftNotificationFirstTime) {
        if (isBrakeState) {
            mBuilder.setContentText(context.getString(R.string.break_time_left) + " " + calculateTimeLeft
                    (millisUntilFinished));
        } else {
            mBuilder.setContentText(context.getString(R.string.work_time_left) + " " + calculateTimeLeft
                    (millisUntilFinished));
        }
        // }
    }

    private void createIntentToOpenApp(NotificationCompat.Builder mBuilder) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
    }

    private void addButtonsToNotification(NotificationCompat.Builder mBuilder) {
        addPauseResumeButton(mBuilder);
        addSkipButton(mBuilder);
        addStopButton(mBuilder);
    }

    private void addStopButton(NotificationCompat.Builder mBuilder) {
        mBuilder.addAction(R.drawable.ic_stop_button, context.getString(R.string.stop),
                createButtonPendingIntent(Constants.BUTTON_STOP));
    }

    private void addSkipButton(NotificationCompat.Builder mBuilder) {
        mBuilder.addAction(R.drawable.ic_skip_button, context.getString(R.string.skip),
                createButtonPendingIntent(Constants.BUTTON_SKIP));
    }

    private void addPauseResumeButton(NotificationCompat.Builder mBuilder) {
        if (isTimerRunning) {
            mBuilder.addAction(R.drawable.ic_play_button, context.getString(R.string.pause),
                    createButtonPendingIntent(Constants.BUTTON_PAUSE_RESUME));
        } else {
            mBuilder.addAction(R.drawable.ic_play_button, context.getString(R.string.resume),
                    createButtonPendingIntent(Constants.BUTTON_PAUSE_RESUME));
        }
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

    private String calculateTimeLeft(long milliseconds) {
        return formatTime(getMinutes(milliseconds), getSeconds(milliseconds));
    }

    @NonNull
    private String formatTime(int minutes, int seconds) {
        String timeLeft;

        timeLeft = "" + minutes;
        timeLeft += ":";
        if (seconds < 10) {
            timeLeft += "0";
        }
        timeLeft += "" + seconds;
        return timeLeft;
    }

    private int getSeconds(long milliseconds) {
        return (int) (milliseconds % 60000 / 1000);
    }

    private int getMinutes(long milliseconds) {
        return (int) (milliseconds / 60000);
    }

    @NonNull
    private Intent createButtonIntent(String actionValue) {
        Intent buttonIntent;
        if (isNotificationOpenedFromActivity) {
            buttonIntent = new Intent(context, ActivityNotificationButtonReceiver.class);
        } else {
            buttonIntent = new Intent(context, NonActivityNotificationButtonReceiver.class);
        }
        buttonIntent.putExtra(Constants.BUTTON_ACTION, actionValue);
        return buttonIntent;
    }
}

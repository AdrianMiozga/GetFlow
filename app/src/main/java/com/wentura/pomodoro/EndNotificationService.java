package com.wentura.pomodoro;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class EndNotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showEndNotification();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showEndNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.CHANNEL_TIMER_COMPLETED)
                .setSmallIcon(R.drawable.work_icon)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                Constants.PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(pendingIntent);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        if (isBreakState) {
            builder.setContentText(getString(R.string.break_time));
        } else {
            builder.setContentText(getString(R.string.work_time));
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(Constants.ON_FINISH_NOTIFICATION, builder.build());
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(Constants.ON_FINISH_NOTIFICATION);
        }
        super.onDestroy();
    }
}

package com.wentura.pomodoro;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

class Utility {

    static void toggleDoNotDisturb(Context context, int mode) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Constants.DO_NOT_DISTURB_SETTING, false)) {
            setRingerMode(context, mode);
        }
    }

    private static void setRingerMode(Context context, int mode) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
            AudioManager audioManager = context.getSystemService(AudioManager.class);
            if (audioManager != null) {
                audioManager.setRingerMode(mode);
            }
        }
    }

    static String formatTime(Context context, long milliseconds) {
        return String.format(context.getString(R.string.time_format),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds % 60000));
    }

    static String formatStatisticsTime(Context context, long milliseconds) {
        return String.format(context.getString(R.string.time_format_statistics),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds % 60000));
    }

    static void toggleKeepScreenOn(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Constants.KEEP_SCREEN_ON_SETTING, false)) {
            ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * Returns date in format 2019-02-20
     */
    static String subtractDaysFromCurrentDate(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -days);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * Changes date format from for example 2019-02-20 to February 20
     */
    static String formatDate(String date) {
        SimpleDateFormat oldDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date oldDate = null;
        try {
            oldDate = oldDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat newDateFormat = new SimpleDateFormat("MMMM dd", Locale.US);
        return newDateFormat.format(oldDate);
    }
}
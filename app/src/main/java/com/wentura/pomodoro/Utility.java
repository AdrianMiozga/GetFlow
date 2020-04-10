package com.wentura.pomodoro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utility {

    static void setDoNotDisturb(Context context, int mode) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Constants.DO_NOT_DISTURB_SETTING, false)) {
            setRingerMode(context, mode);
        }
    }

    private static void setRingerMode(Context context, int mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                return;
            }

            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                return;
            }

            AudioManager audioManager = context.getSystemService(AudioManager.class);

            if (audioManager == null) {
                return;
            }
            audioManager.setRingerMode(mode);
        } else {
            AudioManager audioManager =
                    (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager == null) {
                return;
            }
            audioManager.setRingerMode(mode);
        }
    }

    @SuppressLint("DefaultLocale")
    static String formatTime(long milliseconds) {
        // I'm adding 999 milliseconds so that the timer doesn't end one second after 00:00, but
        // exactly when 00:00 strikes. Adding exactly 1000 milliseconds makes the timer show one
        // second more when it's not started.
        milliseconds += 999;
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds % 60000));
    }

    @SuppressLint("DefaultLocale")
    public static String formatStatisticsTime(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));

        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));

        if (seconds > 0) {
            minutes++;
        }

        if (hours > 0 && minutes == 0) {
            return String.format("%dh", hours);
        }

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        }

        return String.format("%dm", minutes);
    }

    @SuppressLint("DefaultLocale")
    static String formatTimeForNotification(long milliseconds) {
        milliseconds += 999;

        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));

        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));

        if (hours > 0 && minutes != 0) {
            return String.format("%dh %dm", hours, minutes);
        }

        if (hours > 0) {
            return String.format("%dh", hours);
        }

        if (minutes > 0 && seconds > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }

        if (minutes > 0 && seconds == 0) {
            return String.format("%dm", minutes);
        }
        return String.format("%ds", seconds);
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

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.datePattern, Locale.US);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getMonth(String fromDate) {
        Date date = stringToDate(fromDate);

        if (date == null) {
            return "";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM", Locale.US);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getFirstMonthOfTheYear(int month) {
        DateFormat formatter = new SimpleDateFormat("MMM", Locale.getDefault());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, month);
        return formatter.format(calendar.getTime());
    }

    /**
     * Returns date in format 2019-02-20
     */
    public static String subtractDaysFromCurrentDate(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -days);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.datePattern, Locale.US);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String calendarToString(Calendar calendar) {
        StringBuilder result = new StringBuilder();

        result.append(calendar.get(Calendar.YEAR));
        result.append("-");

        if (calendar.get(Calendar.MONTH) + 1 < 10) {
            result.append("0");
        }

        result.append((calendar.get(Calendar.MONTH) + 1));
        result.append("-01");
        return result.toString();
    }

    public static Date stringToDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.datePattern, Locale.US);

        try {
            return dateFormat.parse(date);
        } catch (ParseException parseException) {
            parseException.printStackTrace();
            return null;
        }
    }

    /**
     * Changes date format from for example 2019-02-20 to February 20
     */
    public static String formatDate(String date) {
        SimpleDateFormat oldDateFormat = new SimpleDateFormat(Constants.datePattern, Locale.US);
        Date oldDate = null;
        try {
            oldDate = oldDateFormat.parse(date);
        } catch (ParseException exception) {
            exception.printStackTrace();
        }

        if (oldDate == null) {
            return "";
        } else {
            SimpleDateFormat newDateFormat = new SimpleDateFormat("MMMM dd", Locale.US);
            return newDateFormat.format(oldDate);
        }
    }
}

/*
 * Copyright (C) 2020 Adrian Miozga <AdrianMiozga@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.wentura.focus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import java.util.concurrent.TimeUnit;

public final class Utility {
    // Suppress default constructor for noninstantiability
    private Utility() {
        throw new AssertionError();
    }

    static void setWifiEnabled(Context context, boolean enable) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        if (!sharedPreferences.getBoolean(Constants.DISABLE_WIFI, false)) {
            return;
        }

        WifiManager wifiManager =
                (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null) {
            wifiManager.setWifiEnabled(enable);
        }
    }

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
}

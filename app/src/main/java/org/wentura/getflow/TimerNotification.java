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

package org.wentura.getflow;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

final class TimerNotification {

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int activityId = sharedPreferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1);

        Intent buttonIntent = new Intent(context, TimerActionReceiver.class);
        buttonIntent.putExtra(Constants.BUTTON_ACTION, actionValue);
        buttonIntent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, activityId);
        return buttonIntent;
    }
}

package com.wentura.pomodoroapp.settings;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import com.wentura.pomodoroapp.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String KEY_DO_NOT_DISTURB_SETTING = "do_not_disturb_setting";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Log.d("onResume", "Called");

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        assert notificationManager != null;
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            SwitchPreference switchPreference = (SwitchPreference) findPreference(KEY_DO_NOT_DISTURB_SETTING);
            switchPreference.setChecked(false);
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_DO_NOT_DISTURB_SETTING) && sharedPreferences.getBoolean(KEY_DO_NOT_DISTURB_SETTING, false)) {
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

            assert notificationManager != null;
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_access_policy_not_granted)
                        .setPositiveButton(R.string.dialog_go_to_settings, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d("onResume", "Cancel");
                                SwitchPreference switchPreference = (SwitchPreference) findPreference(KEY_DO_NOT_DISTURB_SETTING);
                                switchPreference.setChecked(false);
                            }
                        }).show();
            }
        }
    }
}

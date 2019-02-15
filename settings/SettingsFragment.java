package com.wentura.pomodoroapp.settings;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;

import com.wentura.pomodoroapp.Constants;
import com.wentura.pomodoroapp.R;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

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

        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            SwitchPreference switchPreference = (SwitchPreference) findPreference(Constants.DO_NOT_DISTURB_SETTING);
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
        if (key.equals(Constants.DO_NOT_DISTURB_SETTING) && sharedPreferences.getBoolean(Constants.DO_NOT_DISTURB_SETTING, false)) {
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
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
                                SwitchPreference switchPreference = (SwitchPreference) findPreference(Constants.DO_NOT_DISTURB_SETTING);
                                switchPreference.setChecked(false);
                            }
                        }).show();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.d(TAG, "onPreferenceTreeClick: ");

        String key = preference.getKey();

        if (key.equals(Constants.BREAK_DURATION) || key.equals(Constants.WORK_DURATION)) {
            EditTextPreference editTextPreference = (EditTextPreference) findPreference(key);
            EditText editText = editTextPreference.getEditText();

            if (key.equals(Constants.BREAK_DURATION)) {
                if (editText.getText().toString().equals("")) {
                    editText.setText(Constants.DEFAULT_BREAK_TIME);
                }
            }

            if (key.equals(Constants.WORK_DURATION)) {
                if (editText.getText().toString().equals("")) {
                    editText.setText(Constants.DEFAULT_WORK_TIME);
                }
            }
            editText.requestFocus();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}

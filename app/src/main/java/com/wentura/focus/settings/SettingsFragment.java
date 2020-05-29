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

package com.wentura.focus.settings;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.wentura.focus.Constants;
import com.wentura.focus.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference workDurationSetting = findPreference(Constants.WORK_DURATION_SETTING);

        if (workDurationSetting != null) {
            workDurationSetting.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            editText.selectAll();
                        }
                    });
        }

        EditTextPreference breakDurationSetting = findPreference(Constants.BREAK_DURATION_SETTING);

        if (breakDurationSetting != null) {
            breakDurationSetting.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            editText.selectAll();
                        }
                    });
        }

        SwitchPreferenceCompat doNotDisturbSwitch = findPreference(Constants.DO_NOT_DISTURB_SETTING);

        SwitchPreferenceCompat doNotDisturbBreakSwitch = findPreference(Constants.DO_NOT_DISTURB_BREAK_SETTING);

        Log.d(TAG, "onResume: " + doNotDisturbSwitch + ", " + doNotDisturbBreakSwitch);
        if (doNotDisturbSwitch != null && doNotDisturbBreakSwitch != null) {
            doNotDisturbBreakSwitch.setVisible(doNotDisturbSwitch.isChecked());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        Log.d(TAG, "onResume: ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                return;
            }

            if (notificationManager.isNotificationPolicyAccessGranted()) {
                return;
            }

            SwitchPreferenceCompat doNotDisturbSwitch = findPreference(Constants.DO_NOT_DISTURB_SETTING);

            if (doNotDisturbSwitch != null) {
                doNotDisturbSwitch.setChecked(false);
            }
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
            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted()) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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
                                SwitchPreferenceCompat switchPreference = findPreference(Constants.DO_NOT_DISTURB_SETTING);

                                if (switchPreference == null) {
                                    return;
                                }
                                switchPreference.setChecked(false);

                                SwitchPreferenceCompat doNotDisturbBreakSwitch = findPreference(Constants.DO_NOT_DISTURB_BREAK_SETTING);

                                if (doNotDisturbBreakSwitch == null) {
                                    return;
                                }
                                doNotDisturbBreakSwitch.setVisible(false);

                            }
                        }).show();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals(Constants.DO_NOT_DISTURB_SETTING)) {
            SwitchPreferenceCompat doNotDisturbBreakSwitch = findPreference(Constants.DO_NOT_DISTURB_BREAK_SETTING);

            SwitchPreferenceCompat doNotDisturbSwitch = findPreference(key);

            if (doNotDisturbSwitch != null && doNotDisturbBreakSwitch != null) {
                doNotDisturbBreakSwitch.setVisible(doNotDisturbSwitch.isChecked());
            }
        }

        if (key.equals(Constants.BREAK_DURATION_SETTING) || key.equals(Constants.WORK_DURATION_SETTING)) {
            EditTextPreference editTextPreference = findPreference(key);

            if (editTextPreference == null) {
                return super.onPreferenceTreeClick(preference);
            }

            String editText = editTextPreference.getText();

            if (key.equals(Constants.BREAK_DURATION_SETTING)) {
                if (editText == null || editText.equals("")) {
                    editTextPreference.setText(Constants.DEFAULT_BREAK_TIME);
                }
            }

            if (key.equals(Constants.WORK_DURATION_SETTING)) {
                if (editText == null || editText.equals("")) {
                    editTextPreference.setText(Constants.DEFAULT_WORK_TIME);
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }
}

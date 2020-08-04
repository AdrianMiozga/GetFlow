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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.wentura.focus.Constants;
import com.wentura.focus.R;
import com.wentura.focus.applicationlock.ApplicationLockActivity;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference workDurationSetting = findPreference(Constants.WORK_DURATION_SETTING);

        if (workDurationSetting != null) {
            workDurationSetting.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                String text = preference.getText();

                if (TextUtils.isEmpty(text)) {
                    return Constants.DEFAULT_WORK_TIME + "m";
                }
                return text + "m";
            });

            workDurationSetting.setOnBindEditTextListener(new MyOnBindEditText());
        }

        EditTextPreference breakDurationSetting = findPreference(Constants.BREAK_DURATION_SETTING);

        if (breakDurationSetting != null) {
            breakDurationSetting.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                String text = preference.getText();

                if (TextUtils.isEmpty(text)) {
                    return Constants.DEFAULT_BREAK_TIME + "m";
                }
                return text + "m";
            });

            breakDurationSetting.setOnBindEditTextListener(new MyOnBindEditText());
        }

        EditTextPreference longBreakDurationSetting =
                findPreference(Constants.LONG_BREAK_DURATION_SETTING);

        if (longBreakDurationSetting != null) {
            longBreakDurationSetting.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                String text = preference.getText();

                if (TextUtils.isEmpty(text)) {
                    return Constants.DEFAULT_LONG_BREAK_TIME + "m";
                }
                return text + "m";
            });

            longBreakDurationSetting.setOnBindEditTextListener(new MyOnBindEditText());
        }

        SwitchPreferenceCompat doNotDisturbSwitch = findPreference(Constants.DO_NOT_DISTURB_SETTING);

        SwitchPreferenceCompat doNotDisturbBreakSwitch = findPreference(Constants.DO_NOT_DISTURB_BREAK_SETTING);

        if (doNotDisturbSwitch != null && doNotDisturbBreakSwitch != null) {
            doNotDisturbBreakSwitch.setVisible(doNotDisturbSwitch.isChecked());
        }

        EditTextPreference longBrakeDurationSetting =
                findPreference(Constants.LONG_BREAK_DURATION_SETTING);

        SwitchPreferenceCompat longBreakEnabled = findPreference(Constants.LONG_BREAK_SETTING);

        if (longBreakEnabled != null && longBrakeDurationSetting != null) {
            longBrakeDurationSetting.setVisible(longBreakEnabled.isChecked());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

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
                        .setPositiveButton(R.string.dialog_go_to_settings, (dialog, id) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)))
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
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
                        }).show();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals(Constants.LONG_BREAK_SETTING)) {
            EditTextPreference longBrakeDurationSetting =
                    findPreference(Constants.LONG_BREAK_DURATION_SETTING);

            SwitchPreferenceCompat longBreakEnabled = findPreference(key);

            if (longBreakEnabled != null && longBrakeDurationSetting != null) {
                longBrakeDurationSetting.setVisible(longBreakEnabled.isChecked());
            }
        }

        if (key.equals(Constants.DO_NOT_DISTURB_SETTING)) {
            SwitchPreferenceCompat doNotDisturbBreakSwitch = findPreference(Constants.DO_NOT_DISTURB_BREAK_SETTING);

            SwitchPreferenceCompat doNotDisturbSwitch = findPreference(key);

            if (doNotDisturbSwitch != null && doNotDisturbBreakSwitch != null) {
                doNotDisturbBreakSwitch.setVisible(doNotDisturbSwitch.isChecked());
            }
        }

        if (key.equals(Constants.APPLICATION_LOCK_PREFERENCE)) {
            Intent intent = new Intent(getContext(), ApplicationLockActivity.class);
            preference.setIntent(intent);
        }

        if (key.equals(Constants.BREAK_DURATION_SETTING) || key.equals(Constants.WORK_DURATION_SETTING) || key.equals(Constants.LONG_BREAK_DURATION_SETTING)) {
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

            if (key.equals(Constants.LONG_BREAK_DURATION_SETTING)) {
                if (editText == null || editText.equals("")) {
                    editTextPreference.setText(Constants.DEFAULT_LONG_BREAK_TIME);
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    static class MyOnBindEditText implements EditTextPreference.OnBindEditTextListener {
        @Override
        public void onBindEditText(@NonNull final EditText editText) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.selectAll();
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        if (editable.toString().equals("") ||
                                Integer.parseInt(editable.toString()) < 1 ||
                                Integer.parseInt(editable.toString()) > 999 ||
                                editable.charAt(0) == '0') {
                            editText.getRootView().findViewById(android.R.id.button1).setEnabled(false);
                        } else {
                            editText.getRootView().findViewById(android.R.id.button1).setEnabled(true);
                        }
                    } catch (NumberFormatException e) {
                        editText.getRootView().findViewById(android.R.id.button1).setEnabled(false);
                    }
                }
            });
        }
    }
}

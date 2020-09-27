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

package com.wentura.focus.activities;

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
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.wentura.focus.Constants;
import com.wentura.focus.R;
import com.wentura.focus.Utility;
import com.wentura.focus.database.Database;

public class ActivitySettings extends AppCompatActivity {

    private Database database;
    private int activityId;
    private TextView workDurationSummary;
    private TextView breakDurationSummary;
    private TextView longBreakDurationSummary;
    private TextView sessionsBeforeLongBreakSummary;
    private SwitchMaterial wifiSwitch;
    private SwitchMaterial dndSwitch;
    private SwitchMaterial enableLongBreaksSwitch;
//    private SwitchMaterial enableDNDOnBreaksSwitch;
    private LinearLayout longBreakDurationGroup;
    private LinearLayout sessionsBeforeLongBreakGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        setTitle(getIntent().getStringExtra(Constants.ACTIVITY_NAME));

        activityId = getIntent().getIntExtra(Constants.ACTIVITY_ID_INTENT, 1);

        database = Database.getInstance(this);

        wifiSwitch = findViewById(R.id.wifi_switch);
        dndSwitch = findViewById(R.id.do_not_disturb_switch);
        enableLongBreaksSwitch = findViewById(R.id.enable_long_breaks_switch);
//        enableDNDOnBreaksSwitch = findViewById(R.id.enable_do_not_disturb_on_breaks_switch);
        sessionsBeforeLongBreakGroup = findViewById(R.id.sessions_till_long_break_linear_layout);

        ConstraintLayout constraintLayout = findViewById(R.id.some_constraint_layout);

        LinearLayout workDurationGroup = findViewById(R.id.work_duration_linear_layout);
        LinearLayout breakDurationGroup = findViewById(R.id.break_duration_linear_layout);
        longBreakDurationGroup = findViewById(R.id.long_break_duration_linear_layout);
        RelativeLayout wifiGroup = findViewById(R.id.wifi_group);

        workDurationSummary = findViewById(R.id.work_duration_summary);
        breakDurationSummary = findViewById(R.id.break_duration_summary);
        longBreakDurationSummary = findViewById(R.id.long_break_duration_summary);
        sessionsBeforeLongBreakSummary = findViewById(R.id.sessions_till_long_break_summary);

        setupUi();

        RelativeLayout dndGroup = findViewById(R.id.do_not_disturb_linear_layout);
        dndGroup.setOnClickListener(view -> dndSwitch.performClick());

        workDurationGroup.setOnClickListener(view -> setupDialog(getString(R.string.work_duration_title), WindowType.WORK_DURATION));
        breakDurationGroup.setOnClickListener(view -> setupDialog(getString(R.string.break_duration_title),
                WindowType.BREAK_DURATION));
        longBreakDurationGroup.setOnClickListener(view -> setupDialog(getString(R.string.long_break_duration_title),
                WindowType.LONG_BREAK_DURATION));
        sessionsBeforeLongBreakGroup.setOnClickListener(view -> setupDialog(getResources().getString(R.string.sessions_before_a_long_break_title),
                WindowType.SESSIONS_BEFORE_LONG_BREAK));

        enableLongBreaksSwitch.setOnClickListener(view ->
                Database.databaseExecutor.execute(() -> {
                    database.activityDao().setLongBreaksEnabled(activityId, enableLongBreaksSwitch.isChecked());

                    runOnUiThread(() -> {
                        Transition transition = new AutoTransition();
                        transition.setDuration(Constants.DEFAULT_ANIMATION_LENGTH);

                        TransitionManager.beginDelayedTransition(constraintLayout, transition);

                        longBreakDurationGroup.setVisibility(enableLongBreaksSwitch.isChecked() ?
                                View.VISIBLE : View.GONE);

                        sessionsBeforeLongBreakGroup.setVisibility(enableLongBreaksSwitch.isChecked() ?
                                View.VISIBLE : View.GONE);
                    });
                }));

        dndSwitch.setOnClickListener(view -> {
            Database.databaseExecutor.execute(() -> {
                database.activityDao().setDNDEnabled(activityId, dndSwitch.isChecked());

                runOnUiThread(() -> {
                    Transition transition = new AutoTransition();
                    transition.setDuration(Constants.DEFAULT_ANIMATION_LENGTH);

                    TransitionManager.beginDelayedTransition(constraintLayout, transition);

//                    enableDNDOnBreaksSwitch.setVisibility(dndSwitch.isChecked() ?
//                            View.VISIBLE : View.GONE);
                });
            });

            if (dndSwitch.isChecked()) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager == null) {
                    return;
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                }

                if (notificationManager.isNotificationPolicyAccessGranted()) {
                    return;
                }

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setMessage(R.string.dialog_access_policy_not_granted)
                        .setPositiveButton(R.string.dialog_go_to_settings, (dialog, id) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)))
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel, (dialog, id) -> dndSwitch.performClick()).show();
            }
        });

//        enableDNDOnBreaksSwitch.setOnClickListener(view ->
//                Database.databaseExecutor.execute(() ->
//                        database.activityDao().setKeepDNDOnBreaks(activityId, enableDNDOnBreaksSwitch.isChecked())));

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            wifiGroup.setOnClickListener(view -> wifiSwitch.performClick());

            wifiSwitch.setOnClickListener(view ->
                    Database.databaseExecutor.execute(() ->
                            database.activityDao().setDisableWifi(activityId, wifiSwitch.isChecked())));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activities_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(Constants.FULL_SCREEN_MODE, false)) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_name_button:
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle(R.string.new_activity_name);

                EditText editText = new EditText(this);

                Database.databaseExecutor.execute(() -> {
                    String activityName = database.activityDao().getName(activityId);

                    runOnUiThread(() -> {
                        editText.setText(activityName);
                        editText.selectAll();
                    });
                });

                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                editText.requestFocus();

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (editable.toString().isEmpty() || editable.toString().length() > Constants.MAX_ACTIVITY_NAME_LENGTH) {
                            editText.getRootView().findViewById(android.R.id.button1).setEnabled(false);
                        } else {
                            editText.getRootView().findViewById(android.R.id.button1).setEnabled(true);
                        }
                    }
                });

                builder.setView(getLinearLayoutWithMargins(editText));

                builder.setPositiveButton(R.string.OK, (dialog, which) -> {
                    Database.databaseExecutor.execute(() -> database.activityDao().updateActivityName(activityId, editText.getText().toString()));

                    setTitle(editText.getText().toString());
                });

                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

                AlertDialog dialog = builder.show();

                Window window = dialog.getWindow();

                if (window != null) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
                return true;
            case R.id.delete_button:
                Database.databaseExecutor.execute(() -> {
                    int numberOfActivities = database.activityDao().getNumberOfActivities();

                    if (numberOfActivities <= 1) {
                        runOnUiThread(() -> {
                            MaterialAlertDialogBuilder cantDeleteDialog = new MaterialAlertDialogBuilder(this);
                            cantDeleteDialog.setTitle(getString(R.string.delete_activity));
                            cantDeleteDialog.setMessage(R.string.cantDeleteDialog);
                            cantDeleteDialog.setNegativeButton(getString(R.string.OK), (dialog1, which) -> dialog1.cancel());
                            cantDeleteDialog.show();
                        });
                    } else {
                        boolean isDataWritten = database.pomodoroDao().isDataWrittenWithActivity(activityId);

                        runOnUiThread(() -> {
                            MaterialAlertDialogBuilder deleteDialog = new MaterialAlertDialogBuilder(this);
                            deleteDialog.setTitle(R.string.delete_activity);

                            if (isDataWritten) {
                                deleteDialog.setMessage(R.string.you_have_data_with_this_activity);
                            } else {
                                deleteDialog.setMessage(R.string.delete_activity_message);
                            }

                            deleteDialog.setPositiveButton(getString(R.string.confirm), (dialog1, which) -> {
                                Database.databaseExecutor.execute(() -> {
                                    database.activityDao().deleteActivity(activityId);
                                    database.pomodoroDao().deleteAllDataWithActivityId(activityId);
                                });

                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                                int currentActivityId = sharedPreferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1);

                                if (currentActivityId == activityId) {
                                    Database.databaseExecutor.execute(() ->
                                            sharedPreferences.edit().putInt(Constants.CURRENT_ACTIVITY_ID,
                                                    database.activityDao().getFirstActivityID()).apply());
                                }

                                Intent intent = new Intent(this, Activities.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            });

                            deleteDialog.setNegativeButton(getString(R.string.cancel), (dialog1, which) -> dialog1.cancel());

                            deleteDialog.show();
                        });
                    }
                });
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Activities.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setupUi() {
        Database.databaseExecutor.execute(() -> {
            int workDuration = database.activityDao().getWorkDuration(activityId);
            runOnUiThread(() -> workDurationSummary.setText(getString(R.string.time, workDuration)));
        });

        Database.databaseExecutor.execute(() -> {
            int breakDuration = database.activityDao().getBreakDuration(activityId);
            runOnUiThread(() -> breakDurationSummary.setText(getString(R.string.time, breakDuration)));
        });

        Database.databaseExecutor.execute(() -> {
            int longBreakDuration = database.activityDao().getLongBreakDuration(activityId);
            runOnUiThread(() -> longBreakDurationSummary.setText(getString(R.string.time, longBreakDuration)));
        });

        Database.databaseExecutor.execute(() -> {
            int sessionsBeforeLongBreak = database.activityDao().getSessionsBeforeLongBreak(activityId);
            runOnUiThread(() -> sessionsBeforeLongBreakSummary.setText(String.valueOf(sessionsBeforeLongBreak)));
        });

        Database.databaseExecutor.execute(() -> {
            boolean areLongBreaksEnabled = database.activityDao().areLongBreaksEnabled(activityId);

            runOnUiThread(() -> {
                enableLongBreaksSwitch.setChecked(areLongBreaksEnabled);
                enableLongBreaksSwitch.jumpDrawablesToCurrentState();

                if (areLongBreaksEnabled) {
                    longBreakDurationGroup.setVisibility(View.VISIBLE);
                    sessionsBeforeLongBreakGroup.setVisibility(View.VISIBLE);
                }
            });
        });

//        Database.databaseExecutor.execute(() -> {
//            boolean isDNDKeptOnBreaks = database.activityDao().isDNDKeptOnBreaks(activityId);
//            runOnUiThread(() -> enableDNDOnBreaksSwitch.setChecked(isDNDKeptOnBreaks));
//        });

        Database.databaseExecutor.execute(() -> {
            boolean isDNDEnabled = database.activityDao().isDNDEnabled(activityId);

            runOnUiThread(() -> {
                if (isDNDEnabled) {
                    dndSwitch.performClick();
                }
                dndSwitch.jumpDrawablesToCurrentState();

//                if (isDNDEnabled) {
//                    enableDNDOnBreaksSwitch.setVisibility(View.VISIBLE);
//                }
            });
        });

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Database.databaseExecutor.execute(() -> {
                boolean isWifiDisabled = database.activityDao().isWifiDisabledDuringWorkSession(activityId);
                runOnUiThread(() -> {
                    wifiSwitch.setChecked(isWifiDisabled);
                    wifiSwitch.jumpDrawablesToCurrentState();
                });
            });
        }
    }

    private void setupDialog(String dialogTitle, WindowType windowType) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(dialogTitle);

        EditText editText = new EditText(this);

        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        Database.databaseExecutor.execute(() -> {
            int valueFromDatabase;

            switch (windowType) {
                case WORK_DURATION:
                    valueFromDatabase = database.activityDao().getWorkDuration(activityId);
                    break;
                case BREAK_DURATION:
                    valueFromDatabase = database.activityDao().getBreakDuration(activityId);
                    break;
                case SESSIONS_BEFORE_LONG_BREAK:
                    valueFromDatabase = database.activityDao().getSessionsBeforeLongBreak(activityId);
                    break;
                default:
                    valueFromDatabase = database.activityDao().getLongBreakDuration(activityId);
                    break;
            }

            runOnUiThread(() -> {
                editText.setText(String.valueOf(valueFromDatabase));
                editText.selectAll();
                editText.requestFocus();
            });
        });

        setupRestrictions(editText);

        builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> Database.databaseExecutor.execute(() -> {
            int userInput = Integer.parseInt(editText.getText().toString());

            switch (windowType) {
                case WORK_DURATION:
                    database.activityDao().updateWorkDuration(activityId, userInput);
                    runOnUiThread(() -> workDurationSummary.setText(getString(R.string.time, userInput)));
                    break;
                case BREAK_DURATION:
                    database.activityDao().updateBreakDuration(activityId, userInput);
                    runOnUiThread(() -> breakDurationSummary.setText(getString(R.string.time, userInput)));
                    break;
                case SESSIONS_BEFORE_LONG_BREAK:
                    database.activityDao().updateSessionsBeforeLongBreak(activityId, userInput);
                    runOnUiThread(() -> sessionsBeforeLongBreakSummary.setText(String.valueOf(userInput)));
                    break;
                default:
                    database.activityDao().updateLongBreakDuration(activityId, userInput);
                    runOnUiThread(() -> longBreakDurationSummary.setText(getString(R.string.time, userInput)));
                    break;
            }
        }));

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.setView(getLinearLayoutWithMargins(editText));

        AlertDialog dialog = builder.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void setupRestrictions(EditText editText) {
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
                    if (editable.toString().isEmpty() || editable.charAt(0) == '0') {
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

    private LinearLayout getLinearLayoutWithMargins(EditText input) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) Utility.convertDpToPixel(20, this), 0,
                (int) Utility.convertDpToPixel(20, this), 0);

        input.setLayoutParams(layoutParams);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);

        linearLayout.addView(input, layoutParams);
        return linearLayout;
    }

    enum WindowType {
        WORK_DURATION,
        BREAK_DURATION,
        LONG_BREAK_DURATION,
        SESSIONS_BEFORE_LONG_BREAK
    }
}

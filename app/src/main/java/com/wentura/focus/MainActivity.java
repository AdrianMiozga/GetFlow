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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.wentura.focus.settings.SettingsActivity;
import com.wentura.focus.statistics.StatisticsActivity;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button skipButton;
    private ImageView workIcon;
    private ImageView breakIcon;
    private TextView timerTextView;
    private Animation blinkingAnimation;
    private boolean isScaleAnimationDone = false;
    private boolean isTimerTextViewActionUpCalled = false;

    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constants.UPDATE_UI_ACTION);

            if (action == null) {
                return;
            }

            switch (action) {
                case Constants.BUTTON_SKIP:
                case Constants.BUTTON_START: {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    boolean isBreakState = sharedPreferences.getBoolean(Constants.IS_BREAK_STATE, false);

                    if (isBreakState) {
                        workIcon.setVisibility(View.INVISIBLE);
                        breakIcon.setVisibility(View.VISIBLE);
                    } else {
                        workIcon.setVisibility(View.VISIBLE);
                        breakIcon.setVisibility(View.INVISIBLE);
                    }

                    break;
                }
                case Constants.BUTTON_STOP:
                    stopTimerUI();
                    break;
                case Constants.BUTTON_PAUSE:
                    startBlinkingAnimation();
                    break;
            }
        }
    };

    private final BroadcastReceiver updateTimerTextView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            // Very rarely, this broadcast delivers a little late. If the user presses the stop
            // button, and this happens, it will update the timer text view after the stop timer method.
            // This condition prevents it.
            if (sharedPreferences.getBoolean(Constants.IS_STOP_BUTTON_VISIBLE, false)) {
                updateTimerTextView(intent.getIntExtra(Constants.TIME_LEFT_INTENT, 0));
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        setupUI();

        Utility.toggleKeepScreenOn(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateTimerTextView, new IntentFilter(Constants.ON_TICK));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                statusReceiver, new IntentFilter(Constants.UPDATE_UI));

        overridePendingTransition(R.anim.background_down, R.anim.foreground_down);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.countdown_text_view);
        workIcon = findViewById(R.id.work_icon);
        breakIcon = findViewById(R.id.break_icon);
        skipButton = findViewById(R.id.skip_button);

        setupNotificationChannels();

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setTitle("");
        }

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                if (sharedPreferences.getInt(Constants.TIME_LEFT, 0) == 0) {
                    finish();
                } else {
                    moveTaskToBack(true);
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        timerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                if (sharedPreferences.getBoolean(Constants.IS_TIMER_RUNNING, false)) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        timerTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isTimerTextViewActionUpCalled = true;
                    if (isScaleAnimationDone) {
                        revertTimerAnimation();
                        isScaleAnimationDone = false;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isTimerTextViewActionUpCalled = false;
                    AnimatorSet animatorSet = startTimerAnimation();

                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isScaleAnimationDone = true;
                            if (isTimerTextViewActionUpCalled) {
                                revertTimerAnimation();
                                isTimerTextViewActionUpCalled = false;
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                }
                return false;
            }
        });

        timerTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopTimer();
                isScaleAnimationDone = true;
                return true;
            }
        });

//        stopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
//                alertDialog.setMessage(R.string.dialog_stop)
//                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                stopTimer();
//                            }
//                        })
//                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                            }
//                        }).show();
//            }
//        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipTimer();
            }
        });

        showHelpingSnackbars();
    }

    private void showHelpingSnackbars() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        final int currentStep = sharedPreferences.getInt(Constants.TUTORIAL_STEP, 0);

        List<String> messages = Arrays.asList(getString(R.string.first_tutorial_message),
                getString(R.string.second_tutorial_message));

        if (currentStep >= messages.size()) {
            return;
        }

        final SharedPreferences.Editor editPreferences =
                sharedPreferences.edit();

        final Snackbar snackbar = Snackbar.make(findViewById(R.id.main_activity), messages.get(currentStep),
                Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();

                editPreferences.putInt(Constants.TUTORIAL_STEP, currentStep + 1).apply();
                showHelpingSnackbars();
            }
        });

        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.grey_snackbar));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    private AnimatorSet startTimerAnimation() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(timerTextView,
                "scaleX", 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(timerTextView,
                "scaleY", 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleDownX).with(scaleDownY);
        animatorSet.start();
        return animatorSet;
    }

    private void revertTimerAnimation() {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(
                timerTextView, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(
                timerTextView, "scaleY", 1f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleUpX).with(scaleUpY);
        animatorSet.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateTimerTextView);

        overridePendingTransition(R.anim.foreground_up, R.anim.background_up);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startSettingsActivity();
                return true;
            case R.id.statistics:
                startStatisticsActivity();
                return true;
        }
        return false;
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startStatisticsActivity() {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }

    private void setupUI() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isBreakState = sharedPreferences.getBoolean(Constants.IS_BREAK_STATE, false);
        boolean areLongBreaksEnabled = sharedPreferences.getBoolean(Constants.LONG_BREAK_SETTING, true);
        int workSessionCounter = sharedPreferences.getInt(Constants.WORK_SESSION_COUNTER, 0);
        int timeLeft = sharedPreferences.getInt(Constants.TIME_LEFT, 0);

        if (timeLeft == 0) {
            if (isBreakState) {
                if (workSessionCounter != 0 && workSessionCounter % 4 == 0 && areLongBreaksEnabled) {
                    updateTimerTextView(Integer.parseInt(sharedPreferences.getString(Constants.LONG_BREAK_DURATION_SETTING,
                            Constants.DEFAULT_LONG_BREAK_TIME)) * 60_000);
                } else {
                    updateTimerTextView(Integer.parseInt(sharedPreferences.getString(Constants.BREAK_DURATION_SETTING,
                            Constants.DEFAULT_BREAK_TIME)) * 60_000);
                }
            } else {
                updateTimerTextView(Integer.parseInt(sharedPreferences.getString(Constants.WORK_DURATION_SETTING,
                        Constants.DEFAULT_WORK_TIME)) * 60_000);
            }
        } else {
            updateTimerTextView(timeLeft);
        }

        if (sharedPreferences.getBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, false)) {
            skipButton.setVisibility(View.VISIBLE);
        } else {
            skipButton.setVisibility(View.INVISIBLE);
        }

        if (sharedPreferences.getBoolean(Constants.IS_WORK_ICON_VISIBLE, true)) {
            workIcon.setVisibility(View.VISIBLE);
        } else {
            workIcon.setVisibility(View.INVISIBLE);
        }

        if (sharedPreferences.getBoolean(Constants.IS_BREAK_ICON_VISIBLE, false)) {
            breakIcon.setVisibility(View.VISIBLE);
        } else {
            breakIcon.setVisibility(View.INVISIBLE);
        }

        if (sharedPreferences.getBoolean(Constants.IS_TIMER_BLINKING, false)) {
            startBlinkingAnimation();
        }
    }

    private void stopTimer() {
        Intent stopIntent = new Intent(this, NotificationButtonReceiver.class);
        stopIntent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_STOP);
        sendBroadcast(stopIntent);
    }

    private void stopTimerUI() {
        skipButton.setVisibility(View.INVISIBLE);
        workIcon.setVisibility(View.VISIBLE);
        breakIcon.setVisibility(View.INVISIBLE);

        revertTimerAnimation();
        timerTextView.clearAnimation();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        updateTimerTextView(Integer.parseInt(sharedPreferences.getString(Constants.WORK_DURATION_SETTING,
                Constants.DEFAULT_WORK_TIME)) * 60_000);
    }

    private void skipTimer() {
        revertTimerAnimation();
        timerTextView.clearAnimation();

        Intent intent = new Intent(this, NotificationButtonReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_SKIP);
        sendBroadcast(intent);
    }

    private void startTimer() {
        skipButton.setVisibility(View.VISIBLE);

        timerTextView.clearAnimation();

        Intent intent = new Intent(this, NotificationButtonReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
        sendBroadcast(intent);
    }

    private void pauseTimer() {
        skipButton.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, NotificationButtonReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_PAUSE);
        sendBroadcast(intent);
    }

    private void startBlinkingAnimation() {
        blinkingAnimation = new AlphaAnimation(1.0f, 0.5f);
        blinkingAnimation.setDuration(1000);
        blinkingAnimation.setRepeatMode(Animation.REVERSE);
        blinkingAnimation.setRepeatCount(Animation.INFINITE);
        timerTextView.startAnimation(blinkingAnimation);
    }

    private void updateTimerTextView(long time) {
        timerTextView.setText(Utility.formatTime(time));
    }

    private void setupNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (notificationManager == null) {
            return;
        }

        NotificationChannel timerCompletedChannel = new NotificationChannel(Constants.CHANNEL_TIMER_COMPLETED,
                Constants.CHANNEL_TIMER_COMPLETED, NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel timerChannel = new NotificationChannel(Constants.CHANNEL_TIMER,
                Constants.CHANNEL_TIMER, NotificationManager.IMPORTANCE_LOW);

        timerCompletedChannel.setShowBadge(false);
        timerCompletedChannel.enableLights(true);
        timerCompletedChannel.setSound(null, null);
        timerChannel.setShowBadge(false);
        notificationManager.createNotificationChannel(timerCompletedChannel);
        notificationManager.createNotificationChannel(timerChannel);
    }
}
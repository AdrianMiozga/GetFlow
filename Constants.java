package com.wentura.pomodoroapp;

public class Constants {
    // SharedPreferences Keys
    public static final String DO_NOT_DISTURB_SETTINGS = "do_not_disturb_setting";
    static final int TIME_LEFT_NOTIFICATION = 1;
    // Notification Ids
    static final int ON_FINISH_NOTIFICATION = 0;
    static final int PENDING_INTENT_SKIP_REQUEST_CODE = 1;
    static final int PENDING_INTENT_PAUSE_RESUME_REQUEST_CODE = 2;
    static final int PENDING_INTENT_STOP_REQUEST_CODE = 3;
    // Pending Intent Request Codes
    static final int PENDING_INTENT_OPEN_APP_REQUEST_CODE = 0;
    static final String KEEP_SCREEN_ON_SETTINGS = "keep_screen_on_setting";
    static final String BREAK_DURATION_SETTINGS = "break_duration_setting";
    static final String WORK_DURATION_SETTING = "work_duration_setting";
    static final String IS_WORK_STARTED = "is_work_started";
    static final String IS_BREAK_STARTED = "is_break_started";
    static final String WORK_LEFT_IN_MILLISECONDS = "work_left_in_milliseconds";
    static final String BREAK_LEFT_IN_MILLISECONDS = "break_left_in_milliseconds";
    static final String IS_TIMER_RUNNING = "timer_is_running";
    static final String IS_BREAK_STATE = "is_break_state";
    static final String TIME_LEFT_NOTIFICATION_FIRST_TIME = "time_left_notification_first_time";
    static final String MY_PREFERENCES = "my_preferences";
    // Buttons
    static final String BUTTON_CLICKED = "BUTTON_CLICKED";
    static final String BUTTON_ACTION = "BUTTON_ACTION";
    static final String BUTTON_STOP = "Stop";
    static final String BUTTON_SKIP = "Skip";
    static final String BUTTON_PAUSE_RESUME = "PauseResume";
    // Defaults
    static final String DEFAULT_WORK_TIME = "25";
    static final String DEFAULT_BREAK_TIME = "5";
    // Notification Channels
    static final String CHANNEL_TIMER = "Pomodoro Timer";
    static final String CHANNEL_TIMER_COMPLETED = "Pomodoro Timer Completed";

    static final String POMODORO = "Pomodoro";
}

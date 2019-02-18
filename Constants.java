package com.wentura.pomodoroapp;

public class Constants {
    // SharedPreferences Keys
    public static final String DO_NOT_DISTURB_SETTING = "do_not_disturb";
    static final String KEEP_SCREEN_ON_SETTING = "keep_screen_on";
    static final String BREAK_DURATION_SETTING = "break_duration";
    static final String WORK_DURATION_SETTING = "work_duration";
    static final String IS_WORK_STARTED = "is_work_started";
    static final String IS_BREAK_STARTED = "is_break_started";
    static final String WORK_LEFT_IN_MILLISECONDS = "work_left_in_milliseconds";
    static final String BREAK_LEFT_IN_MILLISECONDS = "break_left_in_milliseconds";
    static final String IS_TIMER_RUNNING = "is_timer_running";
    static final String IS_BREAK_STATE = "is_break_state";
    static final String MY_PREFERENCES = "my_preferences";
    // Notification Ids
    static final int ON_FINISH_NOTIFICATION = 0;
    static final int TIME_LEFT_NOTIFICATION = 1;
    // Pending Intent Request Codes
    static final int PENDING_INTENT_OPEN_APP_REQUEST_CODE = 0;
    static final int PENDING_INTENT_SKIP_REQUEST_CODE = 1;
    static final int PENDING_INTENT_PAUSE_RESUME_REQUEST_CODE = 2;
    static final int PENDING_INTENT_STOP_REQUEST_CODE = 3;
    // Buttons
    static final String BUTTON_CLICKED = "button_clicked";
    static final String BUTTON_ACTION = "button_action";
    static final String BUTTON_STOP = "button_stop";
    static final String BUTTON_SKIP = "button_skip";
    static final String BUTTON_PAUSE_RESUME = "button_pause_resume";
    // Defaults
    public static final String DEFAULT_WORK_TIME = "25";
    public static final String DEFAULT_BREAK_TIME = "5";
    // Notification Channels
    static final String CHANNEL_TIMER = "Pomodoro Timer";
    static final String CHANNEL_TIMER_COMPLETED = "Pomodoro Timer Completed";
    // Preference Keys
    public static final String WORK_DURATION = "work_duration";
    public static final String BREAK_DURATION = "break_duration";

    static final String POMODORO = "Pomodoro";
    // Database
    public static final String DATABASE_NAME = "Pomodoro.db";
}

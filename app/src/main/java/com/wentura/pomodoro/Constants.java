package com.wentura.pomodoro;

public class Constants {
    // SharedPreferences Keys
    public static final String DO_NOT_DISTURB_SETTING = "do_not_disturb";
    public static final String WORK_DURATION_SETTING = "work_duration";
    public static final String BREAK_DURATION_SETTING = "break_duration";
    static final String KEEP_SCREEN_ON_SETTING = "keep_screen_on";
    static final String IS_STOP_BUTTON_VISIBLE = "is_stop_button_visible";
    static final String IS_SKIP_BUTTON_VISIBLE = "is_skip_button_visible";
    static final String IS_START_BUTTON_VISIBLE = "is_start_button_visible";
    static final String IS_PAUSE_BUTTON_VISIBLE = "is_pause_button_visible";
    static final String IS_WORK_ICON_VISIBLE = "is_work_icon_visible";
    static final String IS_BREAK_ICON_VISIBLE = "is_break_icon_visible";
    // Last session duration is needed when the user changes, for example,
    // work duration setting when the work timer is already running.
    // Using the work duration setting would update the wrong time in statistics.
    static final String LAST_SESSION_DURATION = "last_session_duration";
    static final String TIMER_LEFT = "timer_left";

    static final String IS_TIMER_RUNNING = "is_timer_running";
    static final String IS_BREAK_STATE = "is_break_state";
    // Defaults
    public static final String DEFAULT_WORK_TIME = "25";
    public static final String DEFAULT_BREAK_TIME = "5";
    // Database
    public static final String DATABASE_NAME = "Pomodoro.db";
    // TimerNotification Ids
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
    static final String BUTTON_START = "button_start";
    static final String BUTTON_PAUSE = "button_pause";
    // TimerNotification Channels
    static final String CHANNEL_TIMER = "Pomodoro Timer";
    static final String CHANNEL_TIMER_COMPLETED = "Pomodoro Timer Completed";
    // Intents
    static final String UPDATE_DATABASE_INTENT = "update_database";
    static final String UPDATE_COMPLETED_WORKS = "update_completed_works";
    static final String UPDATE_INCOMPLETE_WORKS = "update_incomplete_works";
    static final String UPDATE_BREAKS = "update_breaks";
    static final String NOTIFICATION_SERVICE = "notification_service";
    static final String NOTIFICATION_SERVICE_PAUSE = "notification_service_pause";
    static final String WHAT_TO_UPDATE = "what_to_update";
    static final String TIME_TO_UPDATE = "time_to_update";
    // Statistics
    static final int HOW_MANY_DAYS_TO_SHOW = 7;

    static final String TIME_LEFT = "time_left";
    static final String ON_TICK = "on_tick";

    static final String UPDATE_UI_ACTION = "update_ui_action";
}

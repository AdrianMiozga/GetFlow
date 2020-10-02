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

public final class Constants {

    // Suppress default constructor for noninstantiability
    private Constants() {
        throw new AssertionError();
    }

    // SharedPreferences Keys
    public static final String HISTORY_SPINNER_SETTING = "history_spinner_setting";
    public static final String ACTIVITIES_SPINNER_SETTING = "activities_spinner_setting";
    public static final String APPLICATION_LOCK_PREFERENCE = "application_lock_preference";
    public static final String LOCKED_APPLICATIONS_LIST = "locked_applications_list";
    public static final String MASTER_LOCK_APPLICATION_SETTING = "master_lock_application_setting";
    public static final String CURRENT_ACTIVITY_ID = "current_activity_id";
    static final String KEEP_SCREEN_ON_SETTING = "keep_screen_on";
    static final String IS_STOP_BUTTON_VISIBLE = "is_stop_button_visible";
    static final String IS_SKIP_BUTTON_VISIBLE = "is_skip_button_visible";
    static final String IS_START_BUTTON_VISIBLE = "is_start_button_visible";
    static final String IS_PAUSE_BUTTON_VISIBLE = "is_pause_button_visible";
    static final String IS_WORK_ICON_VISIBLE = "is_work_icon_visible";
    static final String IS_BREAK_ICON_VISIBLE = "is_break_icon_visible";
    static final String IS_TIMER_BLINKING = "is_timer_blinking";
    static final String TUTORIAL_STEP = "tutorial_step";
    public static final String AUTOMATICALLY_START_NEW_ACTIVITIES = "automatically_start_activities";
    public static final String SCROLL_PIE_CHART_AUTOMATICALLY = "scroll_pie_chart_automatically";
    public static final String FULL_SCREEN_MODE = "full_screen_mode";
    public static final String NEVER_SHOW_IGNORE_BATTERY_OPTIMIZATION_DIALOG =
            "never_show_ignore_battery_optimization_dialog";

    /**
     * After long break is consumed or {@link #HOURS_BEFORE_WORK_SESSION_COUNT_RESETS} passes
     * it goes back to one.
     */
    public static final String WORK_SESSION_COUNTER = "work_session_counter";

    // Last session duration is needed when the user changes, for example,
    // work duration setting when the work timer is already running.
    // Using the work duration setting would update the wrong time in statistics.
    /**
     * It's a work duration value taken from activity settings when starting a timer.
     */
    static final String LAST_SESSION_DURATION = "last_session_duration";

    /** It's zero when the timer is stopped or between work/break, break/work timers. */
    public static final String TIME_LEFT = "time_left";

    public static final String IS_TIMER_RUNNING = "is_timer_running";
    public static final String IS_BREAK_STATE = "is_break_state";
    static final String CENTER_BUTTONS = "center_buttons";

    /** Either complete or incomplete. */
    public static final String TIMESTAMP_OF_LAST_WORK_SESSION = "timestamp_of_last_work_session";

    // Defaults
    public static final int DEFAULT_WORK_TIME = 25;
    public static final int DEFAULT_BREAK_TIME = 5;
    public static final int DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4;
    public static final int DEFAULT_LONG_BREAK_TIME = 20;

    public static final String sourceCodeURL = "https://github.com/AdrianMiozga/GetFlow";

    public static final String feedbackURL = "mailto:AdrianMiozga@outlook.com?subject=Feedback about %s";

    /** Over this time the user has to build work sessions again for long break. */
    public static final int HOURS_BEFORE_WORK_SESSION_COUNT_RESETS = 1;

    /** Minimum time to save session to database. */
    public static final int MINIMUM_SESSION_TIME = 30_000;

    /** Frequency of vibration when the timer is waiting for action - after it ends. */
    static final long VIBRATION_REMINDER_FREQUENCY = 30_000;

    public static final int MAX_ACTIVITY_NAME_LENGTH = 50;

    /** Modifies the length of animation when toggling between GONE and VISIBLE on certain View in app settings. */
    public static final int DEFAULT_ANIMATION_LENGTH = 250;

    // Database
    public static final String DATABASE_NAME = "GetFlow.db";

    // TimerNotification Ids
    static final int TIME_LEFT_NOTIFICATION = 1;
    static final int ON_FINISH_NOTIFICATION = 2;

    // Pending Intent Request Codes
    static final int PENDING_INTENT_OPEN_APP_REQUEST_CODE = 0;
    static final int PENDING_INTENT_SKIP_REQUEST_CODE = 1;
    static final int PENDING_INTENT_PAUSE_RESUME_REQUEST_CODE = 2;
    static final int PENDING_INTENT_STOP_REQUEST_CODE = 3;
    static final int PENDING_INTENT_END_REQUEST_CODE = 4;

    // Intent Filters
    static final String UPDATE_UI = "button_clicked";
    static final String ON_TICK = "on_tick";

    // Intent Action
    public static final String BUTTON_ACTION = "button_action";
    static final String UPDATE_UI_ACTION = "update_ui_action";

    // Intent Extra
    public static final String BUTTON_STOP = "button_stop";
    static final String BUTTON_SKIP = "button_skip";
    public static final String BUTTON_START = "button_start";
    static final String BUTTON_PAUSE = "button_pause";
    static final String TIME_LEFT_INTENT = "time_left_intent";
    public static final String ACTIVITY_NAME = "activity_name";
    public static final String ACTIVITY_ID_INTENT = "activity_id";
    public static final String WORK_DURATION_INTENT = "work_duration_intent";
    public static final String BREAK_DURATION_INTENT = "break_duration_intent";
    public static final String ARE_LONG_BREAKS_ENABLED_INTENT = "are_long_breaks_enabled_intent";
    public static final String SESSIONS_BEFORE_LONG_BREAK_INTENT = "sessions_before_long_break_intent";
    public static final String LONG_BREAK_DURATION_INTENT = "long_break_duration_intent";
    public static final String CURRENT_ACTIVITY_ID_INTENT = "current_activity_id_intent";

    // TimerNotification Channels
    static final String CHANNEL_TIMER = "Pomodoro Timer";
    static final String CHANNEL_TIMER_COMPLETED = "Pomodoro Timer Completed";

    // Services
    static final String NOTIFICATION_SERVICE = "notification_service";
    static final String NOTIFICATION_SERVICE_PAUSE = "notification_service_pause";

    // Wake Lock
    static final String WAKE_LOCK_TAG = "pomodoro::wake_lock";

    // Activity Pie Chart
    /** Display percentages on activity pie chart from this value (inclusive). */
    public static final int DISPLAY_PERCENTAGES_FROM = 9;

    /** Any activity on the pie chart below this percent will be clumped into Others. */
    public static final int CLUMP_ACTIVITIES_BELOW_THIS_PERCENT = 9;

    /**
     * Max activities to show on activity chart. This also includes the special "Others" activity. If there is more
     * activities they will be put under Others and shown in the legend.
     */
    public static final int MAX_ITEMS_TO_SHOW_ON_ACTIVITY_CHART = 6;
}

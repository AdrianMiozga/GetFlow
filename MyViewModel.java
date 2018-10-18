package com.wentura.pomodoroapp;

import android.arch.lifecycle.ViewModel;
import android.os.CountDownTimer;

public class MyViewModel extends ViewModel {
    private boolean workStarted;
    private boolean breakStarted;
    private CountDownTimer countDownTimer;
    private long workLeftInMilliseconds;
    private long breakLeftInMilliseconds;
    private boolean timerIsRunning;
    private boolean isBreakState;
    private boolean timeLeftNotificationFirstTime = true;

    public boolean isWorkStarted() {
        return workStarted;
    }

    public void setWorkStarted(boolean workStarted) {
        this.workStarted = workStarted;
    }

    public boolean isBreakStarted() {
        return breakStarted;
    }

    public void setBreakStarted(boolean breakStarted) {
        this.breakStarted = breakStarted;
    }

    public CountDownTimer getCountDownTimer() {
        return countDownTimer;
    }

    public void setCountDownTimer(CountDownTimer countDownTimer) {
        this.countDownTimer = countDownTimer;
    }

    public long getWorkLeftInMilliseconds() {
        return workLeftInMilliseconds;
    }

    public void setWorkLeftInMilliseconds(long workLeftInMilliseconds) {
        this.workLeftInMilliseconds = workLeftInMilliseconds;
    }

    public long getBreakLeftInMilliseconds() {
        return breakLeftInMilliseconds;
    }

    public void setBreakLeftInMilliseconds(long breakLeftInMilliseconds) {
        this.breakLeftInMilliseconds = breakLeftInMilliseconds;
    }

    public boolean isTimerIsRunning() {
        return timerIsRunning;
    }

    public void setTimerIsRunning(boolean timerIsRunning) {
        this.timerIsRunning = timerIsRunning;
    }

    public boolean isBreakState() {
        return isBreakState;
    }

    public void setBreakState(boolean breakState) {
        isBreakState = breakState;
    }

    public boolean isTimeLeftNotificationFirstTime() {
        return timeLeftNotificationFirstTime;
    }

    public void setTimeLeftNotificationFirstTime(boolean timeLeftNotificationFirstTime) {
        this.timeLeftNotificationFirstTime = timeLeftNotificationFirstTime;
    }
}

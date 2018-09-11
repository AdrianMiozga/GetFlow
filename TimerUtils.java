package com.wentura.pomodoroapp;

import android.os.CountDownTimer;

public class TimerUtils {

    private static CountDownTimer countDownTimer;

    public static void startTimer() {
        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    public static void stopTimer() {
        countDownTimer.cancel();
    }
}

package com.guichaguri.trackplayer.service;

import android.os.Handler;

public class PeriodicHandler extends Handler {
    private final Runnable periodicRunnable;
    private boolean isRunning = false;

    public PeriodicHandler(Runnable runnable, long period) {
        this.periodicRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning) {
                    return;
                }

                runnable.run();
                postDelayed(periodicRunnable, period);
            }
        };
    }

    public void start() {
        isRunning = true;
        this.post(periodicRunnable);
    }

    public void stop() {
        isRunning = false;
    }
}

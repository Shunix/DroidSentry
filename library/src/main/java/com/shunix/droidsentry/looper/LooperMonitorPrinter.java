package com.shunix.droidsentry.looper;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Printer;

/**
 * @author shunix
 * @since 2017/3/7
 */

final class LooperMonitorPrinter implements Printer {
    private final static String START_TAG = ">>>>> Dispatching to ";
    private final static String END_TAG = "<<<<< Finished to ";

    private long mThreshold;
    private Handler mWorkerHandler;
    private RunningStatusCollector mCollector;
    private Runnable mCollectorRunnable;

    LooperMonitorPrinter(long threshold) {
        mThreshold = threshold;
        HandlerThread workerThread = new HandlerThread("RunningStatusCollector");
        workerThread.start();
        mWorkerHandler = new Handler(workerThread.getLooper());
        mCollector = new RunningStatusCollector(Looper.myLooper());
        mCollectorRunnable = new Runnable() {
            @Override
            public void run() {
                mCollector.collect();
            }
        };
    }

    @Override
    public void println(String x) {
        if (x.startsWith(START_TAG)) {
            mWorkerHandler.postDelayed(mCollectorRunnable, mThreshold);
        } else if (x.startsWith(END_TAG)) {
            mWorkerHandler.removeCallbacks(mCollectorRunnable);
        }
    }
}

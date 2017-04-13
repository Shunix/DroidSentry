package com.shunix.droidsentry.looper;

import android.os.Looper;

/**
 * @author shunix
 * @since 2017/3/7
 */

public final class LooperMonitor {
    public final static long DEFAULT_INTERVAL_THRESHOLD = 500;
    private static ThreadLocal<LooperMonitor> sInstance = new ThreadLocal<>();

    private LooperMonitor(long intervalThreshold) {
        Looper currentLooper = Looper.myLooper();
        if (currentLooper == null) {
            throw  new RuntimeException("LooperMonitor cannot be initialized in a thread without looper");
        }
        if (currentLooper != Looper.getMainLooper()) {
            throw new RuntimeException("LooperMonitor can only be initialized on main thread");
        }
        currentLooper.setMessageLogging(new LooperMonitorPrinter(intervalThreshold));
    }

    public static void init() {
        init(DEFAULT_INTERVAL_THRESHOLD);
    }

    public static void init(long intervalThreshold) {
        if (sInstance.get() == null) {
            sInstance.set(new LooperMonitor(intervalThreshold));
        }
    }
}

package com.shunix.droidsentry.log;


import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author shunix
 * @since 2017/3/30
 */

public final class SentryLog {
    private static BlockingQueue<String> mStackTraces;

    static {
        mStackTraces = new LinkedBlockingQueue<>();
        Executor stackTracesPersistService  = Executors.newSingleThreadExecutor();
        stackTracesPersistService.execute(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        String line = mStackTraces.take();
                        Log.e("SentryStackTrace", line);
                    } catch (InterruptedException e) {

                    }
                }
            }
        });
    }

    public static boolean logStackTrace(String line) {
        if (TextUtils.isEmpty(line)) {
            return false;
        }
        return mStackTraces.offer(line);
    }
}

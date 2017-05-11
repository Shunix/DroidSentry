package com.shunix.droidsentry.activity;

import android.app.Application;
import android.os.Build;

import com.shunix.droidsentry.log.SentryLog;

/**
 * Wrapper class for {@link BaseActivityMonitor}
 * @author shunix
 * @since 2017/5/5
 */

public final class ActivityMonitor {
    final static String TAG = "ActivityMonitor";
    private static ActivityMonitor sInstance;
    private BaseActivityMonitor mMonitor;

    public static synchronized ActivityMonitor getInstance() {
        if (sInstance == null) {
            sInstance = new ActivityMonitor();
        }
        return sInstance;
    }

    private ActivityMonitor() {
        int androidVersion = Build.VERSION.SDK_INT;
        if (androidVersion >= 14) {
            mMonitor = new ActivityMonitorV14();
            SentryLog.log(TAG, SentryLog.INFO, "Init ActivityMonitorV14");
        } else {
            mMonitor = new ActivityMonitorCompat();
            SentryLog.log(TAG, SentryLog.INFO, "Init ActivityMonitorCompat");
        }
    }

    public void monitor(Application application) {
        mMonitor.monitor(application);
        SentryLog.log(TAG, SentryLog.INFO, "monitor() called " + (application == null));
    }

    public void stop(Application application) {
        mMonitor.stop(application);
        SentryLog.log(TAG, SentryLog.INFO, "stop() called " + (application == null));
    }
}

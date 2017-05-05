package com.shunix.droidsentry.activity;

import android.app.Application;
import android.os.Build;

/**
 * Wrapper class for {@link BaseActivityMonitor}
 * @author shunix
 * @since 2017/5/5
 */

final class ActivityMonitor {
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
        } else {
            mMonitor = new ActivityMonitorCompat();
        }
    }

    public void monitor(Application application) {
        mMonitor.monitor(application);
    }

    public void stop(Application application) {
        mMonitor.stop(application);
    }
}

package com.shunix.droidsentry.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * @author shunix
 * @since 2017/4/13
 */

final class ActivityMonitorV14 extends BaseActivityMonitor {
    private final class ActivityLifecycleMonitor implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(final Activity activity) {
            checkLeakedActivity(activity);
        }
    }
    private ActivityLifecycleMonitor mCallbacks;

    ActivityMonitorV14() {
        mCallbacks = new ActivityLifecycleMonitor();
    }

    @Override
    protected void stop(Application application) {
        super.stop(application);
        application.unregisterActivityLifecycleCallbacks(mCallbacks);
    }

    @Override
    protected void monitor(Application application) {
        application.registerActivityLifecycleCallbacks(mCallbacks);
    }
}

package com.shunix.droidsentry.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

/**
 * @author shunix
 * @since 2017/4/13
 */

final class ActivityMonitorV14 extends BaseActivityMonitor {
    @TargetApi(14)
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

    @CallSuper
    @Override
    @TargetApi(14)
    protected void stop(@NonNull Application application) {
        super.stop(application);
        application.unregisterActivityLifecycleCallbacks(mCallbacks);
    }

    @Override
    @TargetApi(14)
    protected void monitor(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(mCallbacks);
    }
}

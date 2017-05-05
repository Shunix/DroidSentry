package com.shunix.droidsentry.activity;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author shunix
 * @since 2017/4/13
 */

final class ActivityMonitorCompat extends BaseActivityMonitor {
    private Instrumentation mOriginalInstrumentation;

    class DroidSentryInstrumentation extends Instrumentation {
        @Override
        public void callActivityOnDestroy(Activity activity) {
            super.callActivityOnDestroy(activity);
        }
    }

    @Override
    protected void stop(Application application) {
        super.stop(application);
        restoreInstrumentation();
    }

    @Override
    protected void monitor(Application application) {
        super.monitor(application);
        // Save original instrumentation first
        saveOriginalInstrumentation();
        // Replace instrumentation with custom one
        replaceInstrumentation();
    }

    private void saveOriginalInstrumentation() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method getCurrentThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object activityThreadInstance = getCurrentThreadMethod.invoke(null);
            if (activityThreadInstance != null) {
                Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
                mOriginalInstrumentation = (Instrumentation) instrumentationField.get(activityThreadInstance);
            }
        } catch (Exception e) {

        }
    }

    private void replaceInstrumentation() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method getCurrentThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object activityThreadInstance = getCurrentThreadMethod.invoke(null);
            if (activityThreadInstance != null) {
                Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
                instrumentationField.setAccessible(true);
                instrumentationField.set(activityThreadInstance, new DroidSentryInstrumentation());
            }
        } catch (Exception e) {

        }
    }

    private void restoreInstrumentation() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method getCurrentThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object activityThreadInstance = getCurrentThreadMethod.invoke(null);
            if (activityThreadInstance != null) {
                Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
                instrumentationField.setAccessible(true);
                instrumentationField.set(activityThreadInstance, mOriginalInstrumentation);
            }
        } catch (Exception e) {

        }
    }
}

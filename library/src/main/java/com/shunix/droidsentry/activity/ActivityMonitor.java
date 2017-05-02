package com.shunix.droidsentry.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shunix
 * @since 2017/4/13
 */

public final class ActivityMonitor {
    private static final class Initializer {
        private static final ActivityMonitor INSTANCE = new ActivityMonitor();
    }

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
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    enqueueActivityReference(activity);
                    waitForEnqueue();
                    removeGarbagedActivity();
                    reportLeakedActivity();
                }
            });
        }
    }

    private ActivityLifecycleMonitor mCallbacks;
    private ReferenceQueue<Activity> mReferenceQueue;
    private ExecutorService mExecutorService;
    private Set<ActivityReference> mActivityReferenceSet;
    private LeakHandler mLeakHandler;

    private ActivityMonitor() {
        mCallbacks = new ActivityLifecycleMonitor();
        mReferenceQueue = new ReferenceQueue<>();
        mExecutorService = Executors.newSingleThreadExecutor();
        mActivityReferenceSet = new CopyOnWriteArraySet<>();
    }

    public ActivityMonitor getInstance() {
        return Initializer.INSTANCE;
    }

    public void monitor(Application application) {
        application.registerActivityLifecycleCallbacks(mCallbacks);
        mLeakHandler = new LeakHandler(application);
    }

    public void stop(Application application) {
        application.unregisterActivityLifecycleCallbacks(mCallbacks);
        mLeakHandler.destory();
    }

    private ActivityReference enqueueActivityReference(Activity activity) {
        String activityIdentity = String.valueOf(System.identityHashCode(activity));
        ActivityReference.ActivityKey activityKey = new ActivityReference.ActivityKey(activity.getClass().getName(), activityIdentity);
        ActivityReference activityReference = new ActivityReference(activityKey, activity, mReferenceQueue);
        mActivityReferenceSet.add(activityReference);
        return activityReference;
    }

    private void waitForEnqueue() {
        /*
         * Hack. We don't have a programmatic way to wait for the reference queue
         * daemon to move references to the appropriate queues.
         */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
    }

    private void removeGarbagedActivity() {
        ActivityReference activityReference;
        while((activityReference = (ActivityReference) mReferenceQueue.poll()) != null) {
            mActivityReferenceSet.remove(activityReference);
        }
    }

    private void reportLeakedActivity() {
        // TODO
        mLeakHandler.requestHeapDump();
    }
}

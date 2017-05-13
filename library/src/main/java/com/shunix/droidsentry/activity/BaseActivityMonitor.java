package com.shunix.droidsentry.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;

import com.shunix.droidsentry.log.SentryLog;

import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author shunix
 * @since 2017/5/5
 */

abstract class BaseActivityMonitor {
    private final static String WORKER_THREAD_NAME = "ActivityMonitor-Worker";
    private final static int CHECK_LEAK_INTERVAL = 5000;
    private ReferenceQueue<Activity> mReferenceQueue;
    private Set<ActivityReference> mActivityReferenceSet;

    BaseActivityMonitor() {
        mReferenceQueue = new ReferenceQueue<>();
        mActivityReferenceSet = new CopyOnWriteArraySet<>();
        mWorkerThread = new HandlerThread(WORKER_THREAD_NAME);
        mWorkerThread.start();
    }
    private HandlerThread mWorkerThread;

    private ActivityReference createActivityReference(Activity activity) {
        String activityIdentity = String.valueOf(System.identityHashCode(activity));
        ActivityReference.ActivityKey activityKey = new ActivityReference.ActivityKey(activity.getClass().getName(), activityIdentity);
        ActivityReference activityReference = new ActivityReference(activityKey, activity, mReferenceQueue);
        mActivityReferenceSet.add(activityReference);
        return activityReference;
    }

    private void forceGc() {
        Runtime.getRuntime().gc();
        /*
         * Hack. We don't have a programmatic way to wait for the reference queue
         * daemon to move references to the appropriate queues.
         */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
        System.runFinalization();
    }

    private void removeGarbageCollectedActivity() {
        ActivityReference activityReference;
        while ((activityReference = (ActivityReference) mReferenceQueue.poll()) != null) {
            mActivityReferenceSet.remove(activityReference);
        }
    }

    private void reportLeakedActivity() {
        if (Debug.isDebuggerConnected()) {
            SentryLog.log(ActivityMonitor.TAG, SentryLog.INFO, "Debugger connected, abort heap dump");
        } else {
            SentryLog.log(ActivityMonitor.TAG, SentryLog.INFO, "Request heap dump");
            SentryLog.requestHeapDump();
        }
    }

    abstract void monitor(Application application);

    protected void stop(Application application) {
        mActivityReferenceSet.clear();
        mWorkerThread.quit();
    }

    void checkLeakedActivity(final Activity activity) {
        SentryLog.log(ActivityMonitor.TAG, SentryLog.INFO, "checkLeakedActivity");
        createActivityReference(activity);
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                Handler handler = new Handler(mWorkerThread.getLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        forceGc();
                        removeGarbageCollectedActivity();
                        if (!mActivityReferenceSet.isEmpty()) {
                            ActivityReference[] array = new ActivityReference[mActivityReferenceSet.size()];
                            mActivityReferenceSet.toArray(array);
                            for (ActivityReference reference : array) {
                                SentryLog.log(ActivityMonitor.TAG, SentryLog.WARNING,
                                        "Leaked Activity " + reference.getKey().getActivityName() + "@" + reference.getKey().getIdentity());
                            }
                            mActivityReferenceSet.clear();
                            reportLeakedActivity();
                        }
                    }
                }, CHECK_LEAK_INTERVAL);
                return false;
            }
        });
    }

}

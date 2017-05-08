package com.shunix.droidsentry.activity;

import android.app.Activity;
import android.app.Application;

import com.shunix.droidsentry.log.SentryLog;

import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shunix
 * @since 2017/5/5
 */

abstract class BaseActivityMonitor {
    private ReferenceQueue<Activity> mReferenceQueue;
    private ExecutorService mExecutorService;
    private Set<ActivityReference> mActivityReferenceSet;

    BaseActivityMonitor() {
        mReferenceQueue = new ReferenceQueue<>();
        mExecutorService = Executors.newSingleThreadExecutor();
        mActivityReferenceSet = new CopyOnWriteArraySet<>();
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
        SentryLog.requestHeapDump();
    }

    abstract void monitor(Application application);

    protected void stop(Application application) {
        mExecutorService.shutdown();
        mActivityReferenceSet.clear();
    }

    void checkLeakedActivity(final Activity activity) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                enqueueActivityReference(activity);
                waitForEnqueue();
                removeGarbagedActivity();
                if (!mActivityReferenceSet.isEmpty()) {
                    mActivityReferenceSet.clear();
                    reportLeakedActivity();
                }
            }
        });
    }
}

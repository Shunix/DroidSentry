package com.shunix.droidsentry.activity;

import android.app.Activity;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * @author shunix
 * @since 2017/4/10
 */

final class ActivityReference extends WeakReference<Activity> {
    final static class ActivityKey {
        private String mActivityName;
        private String mIdentity;

        ActivityKey(@NonNull String activityName, @NonNull String identity) {
            if (activityName == null || identity == null) {
                throw new IllegalArgumentException("ActivityName and Identity cannot be null");
            }
            this.mActivityName = activityName;
            this.mIdentity = identity;
        }

        String getActivityName() {
            return mActivityName;
        }

        String getIdentity() {
            return mIdentity;
        }
    }

    private ActivityKey mKey;

    @SuppressWarnings("unchecked")
    ActivityReference(@NonNull ActivityKey key, @NonNull Activity referent, @NonNull ReferenceQueue queue) {
        super(referent, queue);
        if (key == null || referent == null || queue == null) {
            throw new IllegalArgumentException("Referent and ReferenceQueue cannot be null");
        }
        mKey = key;
    }

    ActivityKey getKey() {
        return mKey;
    }

    @Override
    public int hashCode() {
        return mKey.mActivityName.hashCode() * 31 + mKey.mIdentity.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return hashCode() == o.hashCode();
    }
}

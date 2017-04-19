package com.shunix.droidsentry.activity;

import android.app.Activity;

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

        ActivityKey(String activityName, String identity) {
            this.mActivityName = activityName;
            this.mIdentity = identity;
        }

        public String getActivityName() {
            return mActivityName;
        }

        public String getIdentity() {
            return mIdentity;
        }
    }

    private ActivityKey mKey;

    ActivityReference(ActivityKey key, Activity referent, ReferenceQueue queue) {
        super(referent, queue);
        if (referent == null || queue == null) {
            throw new IllegalArgumentException("Referent and ReferenceQueue cannot be empty");
        }
        mKey = key;
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

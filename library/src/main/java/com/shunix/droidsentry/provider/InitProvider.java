package com.shunix.droidsentry.provider;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.shunix.droidsentry.activity.ActivityMonitor;
import com.shunix.droidsentry.log.SentryLog;
import com.shunix.droidsentry.looper.LooperMonitor;

/**
 * @author shunix
 * @since 2017/5/6
 */

public final class InitProvider extends ContentProvider {
    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public boolean onCreate() {
        Application application = (Application) getContext();
        SentryLog.setContext(application);
        LooperMonitor.init();
        ActivityMonitor.getInstance().monitor(application);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

package com.shunix.droidsentry.activity;

import android.app.Application;
import android.os.Debug;
import android.os.Environment;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shunix
 * @since 2017/4/25
 */

final class LeakHandler {
    private final static String DUMP_FILE_DIRECTORY = "heapdump/";
    private final static String FILENAME_PATTERN = "heapdump-%1$ty.%1$tm.%1$te-%1$tk.%1$tM.%1$tS.hprof";
    private Application mApp;
    private ExecutorService mExecutor;

    LeakHandler(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("application cannot be null");
        }
        mApp = application;
        mExecutor = Executors.newSingleThreadExecutor();
    }

    private String getDumpFilePath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File packageRootDir = mApp.getExternalFilesDir(null);
            if (packageRootDir != null) {
                File dumpFileDir = new File(packageRootDir, DUMP_FILE_DIRECTORY);
                if (!dumpFileDir.exists()) {
                    boolean result = dumpFileDir.mkdir();
                    if (result) {
                        String fileName = String.format(Locale.getDefault(), FILENAME_PATTERN, Calendar.getInstance());
                        File hprofFile = new File(dumpFileDir, fileName);
                        return hprofFile.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    public void requestHeapDump() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String dumpFilePath = getDumpFilePath();
                    if (dumpFilePath != null) {
                        Debug.dumpHprofData(dumpFilePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void destroy() {
        mExecutor.shutdown();
    }
}

package com.shunix.droidsentry.log;


import android.app.Application;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shunix
 * @since 2017/3/30
 */

public final class SentryLog {
    private final static String STACKTRACE_FILE_DIRECTORY = "stacktraces/";
    private final static String FILENAME_PATTERN = "stacktraces-%1$ty.%1$tm.%1$te-%1$tk.%1$tM.%1$tS.hprof";
    private static Application mApp;
    private static ExecutorService mStackTracePersistExecutor;

    static {
        mStackTracePersistExecutor = Executors.newSingleThreadExecutor();
    }

    public static void persistStackTraces(final StackTraceElement[] elements) {
        mStackTracePersistExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mApp != null && elements != null && elements.length > 0) {
                    File stackTracesFile = getStackTracesFile();
                    if (stackTracesFile.exists()) {
                        stackTracesFile.delete();
                    } else {
                        try {
                            boolean ret = stackTracesFile.createNewFile();
                            if (ret) {
                                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(stackTracesFile));
                                for (StackTraceElement element : elements) {
                                    bufferedWriter.write(element.toString());
                                    bufferedWriter.newLine();
                                }
                                bufferedWriter.flush();
                                bufferedWriter.close();
                            }
                        } catch (IOException e) {

                        }
                    }
                }
            }
        });
    }

    private static File getStackTracesFile() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File packageRootDir = mApp.getExternalFilesDir(null);
            if (packageRootDir != null) {
                File traceFileDir = new File(packageRootDir, STACKTRACE_FILE_DIRECTORY);
                if (!traceFileDir.exists()) {
                    boolean result = traceFileDir.mkdir();
                    if (result) {
                        String fileName = String.format(Locale.getDefault(), FILENAME_PATTERN, Calendar.getInstance());
                        return new File(traceFileDir, fileName);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Caller must pass application context here.
     * Call this method in a ContentProvider is recommended.
     * @param application
     */
    public static void setContext(Application application) {
        mApp = application;
    }
}

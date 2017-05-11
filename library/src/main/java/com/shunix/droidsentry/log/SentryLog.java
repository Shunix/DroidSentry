package com.shunix.droidsentry.log;


import android.app.Application;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shunix
 * @since 2017/3/30
 */

public final class SentryLog {
    public final static int DEBUG = 0;
    public final static int INFO = 1;
    public final static int WARNING = 2;
    public final static int ERROR = 3;
    private final static int MAX_LOG_ITEM = 500;
    private final static int SYNC_INTERVAL = 30 * 1000;
    private final static String TAG = "SentryLog";
    private final static String STACKTRACE_FILE_DIRECTORY = "stacktraces/";
    private final static String STACKTRACE_FILENAME_PATTERN = "stacktraces-%1$ty.%1$tm.%1$te-%1$tk.%1$tM.%1$tS.log";
    private final static String DUMP_FILE_DIRECTORY = "heapdump/";
    private final static String DUMP_FILENAME_PATTERN = "heapdump-%1$ty.%1$tm.%1$te-%1$tk.%1$tM.%1$tS.hprof";
    private final static String LOG_ITEM_PATTERN = "%1$s|%2$s|%3$s|%4$s";
    private final static String LOG_FILENAME = "sentry.log";
    private static Application mApp;
    private static ExecutorService mStackTracePersistExecutor;
    private static ExecutorService mDumpExecutor;
    private static ExecutorService mLogWriterExecutor;
    private static Map<Integer, String> mLogs;
    private static DateFormat mFormatter;

    static {
        mStackTracePersistExecutor = Executors.newSingleThreadExecutor();
        mDumpExecutor = Executors.newSingleThreadExecutor();
        mLogWriterExecutor = Executors.newSingleThreadExecutor();
        mLogs = Collections.synchronizedMap(new LinkedHashMap<Integer, String>());
        mFormatter = SimpleDateFormat.getDateInstance();
        schedulePeriodicSync();
    }

    private static void schedulePeriodicSync() {
        Looper looper = Looper.getMainLooper();
        final Handler handler = new Handler(looper);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                syncLog();
                handler.postDelayed(this, SYNC_INTERVAL);
            }
        }, SYNC_INTERVAL);
    }

    /**
     * log message should not contain '\n' or '\'
     *
     * @param tag
     * @param level
     * @param msg
     */
    public static void log(String tag, @LogLevel int level, String msg) {
        if (mLogs.size() >= MAX_LOG_ITEM) {
            syncLog();
        }
        String logMsg = String.format(LOG_ITEM_PATTERN, getDisplayTime(), tag, level, msg);
        int hashCode = logMsg.hashCode();
        mLogs.put(hashCode, logMsg);
    }

    private static void syncLog() {
        if (mLogs.isEmpty()) {
            return;
        }
        final LinkedHashMap<Integer, String> mCache = new LinkedHashMap<>(mLogs);
        mLogs.clear();
        mLogWriterExecutor.execute(new Runnable() {
            @Override
            public void run() {
                SentryLog.log(TAG, SentryLog.INFO, "Sync logs to disk " + mCache.size());
                writeLogsToFile(mCache);
            }
        });
    }

    private static void writeLogsToFile(Map<Integer, String> map) {
        File logFile = getFileWithCheck(null, LOG_FILENAME);
        if (logFile != null) {
            if (!logFile.exists()) {
                try {
                    if (!logFile.createNewFile()) {
                        return;
                    }
                } catch (IOException e) {

                }
            }
            FileWriter fileWriter = null;
            BufferedWriter bufferedWriter = null;
            try {
                fileWriter = new FileWriter(logFile, true);
                bufferedWriter = new BufferedWriter(fileWriter);
                for (Map.Entry<Integer, String> entry : map.entrySet()) {
                    bufferedWriter.write(entry.getValue());
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
            } catch (IOException e) {

            } finally {
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (fileWriter != null) {
                        fileWriter.close();
                    }
                } catch (IOException e) {

                }
            }
        }
    }

    private static String getDisplayTime() {
        TimeZone timeZone = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(timeZone);
        return mFormatter.format(calendar.getTime());
    }

    public static void persistStackTraces(final StackTraceElement[] elements) {
        mStackTracePersistExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mApp != null && elements != null && elements.length > 0) {
                    File stackTracesFile = getStackTracesFile();
                    if (stackTracesFile != null) {
                        FileWriter fileWriter = null;
                        BufferedWriter bufferedWriter = null;
                        try {
                            fileWriter = new FileWriter(stackTracesFile);
                            bufferedWriter = new BufferedWriter(fileWriter);
                            for (StackTraceElement element : elements) {
                                bufferedWriter.write(element.toString());
                                bufferedWriter.newLine();
                            }
                            bufferedWriter.flush();
                            SentryLog.log(TAG, SentryLog.INFO, "Write stacktraces to file " + stackTracesFile.getAbsolutePath());
                        } catch (IOException e) {

                        } finally {
                            try {
                                if (bufferedWriter != null) {
                                    bufferedWriter.close();
                                }
                                if (fileWriter != null) {
                                    fileWriter.close();
                                }
                            } catch (IOException e) {

                            }
                        }
                    }
                }
            }
        });
    }

    @Nullable
    private static File getStackTracesFile() {
        String fileName = String.format(Locale.getDefault(), STACKTRACE_FILENAME_PATTERN, Calendar.getInstance());
        return getFileWithCheck(STACKTRACE_FILE_DIRECTORY, fileName);
    }

    @Nullable
    private static String getDumpFilePath() {
        String fileName = String.format(Locale.getDefault(), DUMP_FILENAME_PATTERN, Calendar.getInstance());
        File hprofFile = getFileWithCheck(DUMP_FILE_DIRECTORY, fileName);
        if (hprofFile != null) {
            return hprofFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Return a file on external storage
     *
     * @param dirName  pass null to create file under the root of package directory
     * @param fileName
     * @return
     */
    @Nullable
    private static File getFileWithCheck(@Nullable String dirName, String fileName) {
        try {
            if (!TextUtils.isEmpty(fileName) && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File packageRootDir = mApp.getExternalFilesDir(null);
                if (packageRootDir != null) {
                    if (dirName != null) {
                        File dir = new File(packageRootDir, dirName);
                        if (!dir.exists()) {
                            boolean result = dir.mkdir();
                            if (result) {
                                return new File(dir, fileName);
                            }
                        } else {
                            return new File(dir, fileName);
                        }
                    } else {
                        return new File(packageRootDir, fileName);
                    }
                }
            }
        } catch (Exception e) {

        }
        return null;
    }

    public static void requestHeapDump() {
        mDumpExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String dumpFilePath = getDumpFilePath();
                    if (dumpFilePath != null) {
                        SentryLog.log(TAG, SentryLog.INFO, "Dump hprof file at " + dumpFilePath);
                        Debug.dumpHprofData(dumpFilePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Caller must pass application context here.
     * Call this method in a ContentProvider is recommended.
     *
     * @param application
     */
    public static void setContext(Application application) {
        mApp = application;
    }

    @SuppressWarnings("unused")
    public static void onDestroy() {
        mStackTracePersistExecutor.shutdown();
        mDumpExecutor.shutdown();
    }
}

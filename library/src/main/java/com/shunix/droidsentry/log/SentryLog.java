package com.shunix.droidsentry.log;


import android.app.Application;
import android.os.Debug;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
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
    private final static int MAX_LOG_ITEM = 1000;
    private final static String STACKTRACE_FILE_DIRECTORY = "stacktraces/";
    private final static String STACKTRACE_FILENAME_PATTERN = "stacktraces-%1$ty.%1$tm.%1$te-%1$tk.%1$tM.%1$tS.hprof";
    private final static String DUMP_FILE_DIRECTORY = "heapdump/";
    private final static String DUMP_FILENAME_PATTERN = "heapdump-%1$ty.%1$tm.%1$te-%1$tk.%1$tM.%1$tS.hprof";
    private final static String LOG_ITEM_PATTERN = "%1$s|%2$s|%3$s|%4$s";
    private final static String LOG_FILENAME = "sentry.log";
    private static Application mApp;
    private static ExecutorService mStackTracePersistExecutor;
    private static ExecutorService mDumpExecutor;
    private static ExecutorService mLogWriterExecutor;
    private static ConcurrentHashMap<Integer, String> mLogs;
    private static DateFormat mFormatter;

    static {
        mStackTracePersistExecutor = Executors.newSingleThreadExecutor();
        mDumpExecutor = Executors.newSingleThreadExecutor();
        mLogWriterExecutor = Executors.newSingleThreadExecutor();
        mLogs = new ConcurrentHashMap<>();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                syncLog();
            }
        }));
        mFormatter = SimpleDateFormat.getDateInstance();
    }

    /**
     * log message should not contain '\n' or '\'
     * @param tag
     * @param level
     * @param msg
     */
    public static void log(String tag, @LogLevel int level, String msg) {
        if (mLogs.size() >= MAX_LOG_ITEM ) {
            syncLog();
        }
        String logMsg = String.format(LOG_ITEM_PATTERN, getDisplayTime(), level, level, msg);
        int hashCode = logMsg.hashCode();
        mLogs.put(hashCode, logMsg);
    }

    public static void syncLog() {
        final ConcurrentHashMap<Integer, String> mCache = new ConcurrentHashMap<>(mLogs);
        mLogs.clear();
        mLogWriterExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeLogsToFile(mCache);
            }
        });
    }

    private static void writeLogsToFile(Map<Integer, String> map) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File packageRootDir = mApp.getExternalFilesDir(null);
            if (packageRootDir != null) {
                File logFile = new File(packageRootDir, LOG_FILENAME);
                try {
                    if (!logFile.exists()) {
                        logFile.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(logFile, true);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    for (Map.Entry<Integer, String> entry : map.entrySet()) {
                        bufferedWriter.write(entry.getValue());
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.flush();
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
                        String fileName = String.format(Locale.getDefault(), STACKTRACE_FILENAME_PATTERN, Calendar.getInstance());
                        return new File(traceFileDir, fileName);
                    }
                }
            }
        }
        return null;
    }

    private static String getDumpFilePath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File packageRootDir = mApp.getExternalFilesDir(null);
            if (packageRootDir != null) {
                File dumpFileDir = new File(packageRootDir, DUMP_FILE_DIRECTORY);
                if (!dumpFileDir.exists()) {
                    boolean result = dumpFileDir.mkdir();
                    if (result) {
                        String fileName = String.format(Locale.getDefault(), DUMP_FILENAME_PATTERN, Calendar.getInstance());
                        File hprofFile = new File(dumpFileDir, fileName);
                        return hprofFile.getAbsolutePath();
                    }
                }
            }
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
     * @param application
     */
    public static void setContext(Application application) {
        mApp = application;
    }

    public static void onDestroy() {
        mStackTracePersistExecutor.shutdown();
        mDumpExecutor.shutdown();
    }
}

package com.shunix.droidsentry.looper;

import android.os.Looper;

import com.shunix.droidsentry.log.SentryLog;

import java.util.Map;

/**
 * @author shunix
 * @since 2017/3/30
 */

final class RunningStatusCollector {
    // We only monitor main thread currently, so WeakReference is not required here.
    private Looper mMonitoredLooper;

    RunningStatusCollector(Looper looper) {
        if (looper == null) {
            throw new RuntimeException("looper cannot be null");
        }
        mMonitoredLooper = looper;
    }

    void collect() {
        persistStackTrace(collectStackTrace());
    }

    private StackTraceElement[] collectStackTrace() {
        Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
        if (stackTraceMap != null && stackTraceMap.size() > 0) {
            Thread monitoredThread = mMonitoredLooper.getThread();
            return stackTraceMap.get(monitoredThread);
        }
        return null;
    }

    private boolean persistStackTrace(StackTraceElement[] elements) {
        boolean result = true;
        if (elements != null && elements.length > 0) {
            for (StackTraceElement element : elements) {
                result &= SentryLog.logStackTrace(element.toString());
            }
        }
        return result;
    }
}

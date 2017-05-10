package com.shunix.droidsentry.log;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author shunix
 * @since 2017/5/9
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({SentryLog.DEBUG, SentryLog.INFO, SentryLog.WARNING, SentryLog.ERROR})
public @interface LogLevel {
}

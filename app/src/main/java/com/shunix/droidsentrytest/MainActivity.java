package com.shunix.droidsentrytest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.shunix.droidsentry.log.SentryLog;
import com.shunix.droidsentry.looper.LooperMonitor;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

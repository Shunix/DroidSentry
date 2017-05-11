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
        for (int i = 0; i < 100; ++i) {
            SentryLog.log(MainActivity.class.getSimpleName(), SentryLog.WARNING, "红红火火恍恍惚惚" + i);
        }
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (Exception e) {
//
//                }
//                handler.postDelayed(this, 2000);
//            }
//        }, 2000);
    }
}

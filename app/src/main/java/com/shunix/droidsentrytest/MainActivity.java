package com.shunix.droidsentrytest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.shunix.droidsentry.looper.LooperMonitor;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LooperMonitor.init();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }
}

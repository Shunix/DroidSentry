package com.shunix.droidsentrytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.shunix.droidsentry.log.SentryLog;
import com.shunix.droidsentry.looper.LooperMonitor;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private Button mBlockBtn;
    private Button mLeakBtn;
    // Leaked Activity
    static LeakedActivity leakedActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBlockBtn = (Button) findViewById(R.id.blockBtn);
        mLeakBtn = (Button) findViewById(R.id.leakBtn);
        ClickAction action = new ClickAction();
        mBlockBtn.setOnClickListener(action);
        mLeakBtn.setOnClickListener(action);
    }

    class ClickAction implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.blockBtn:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });
                    break;
                case R.id.leakBtn:
                    startActivity(new Intent(MainActivity.this, LeakedActivity.class));
                    break;
                default:
                    break;
            }
        }
    }
}

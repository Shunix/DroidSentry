package com.shunix.droidsentrytest;

import android.app.Activity;
import android.os.Bundle;

public class LeakedActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaked);
        MainActivity.leakedActivity = this;
    }
}

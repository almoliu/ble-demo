package com.goertek.asp.bledemo.activity;

import android.os.Bundle;
import android.os.Handler;

import com.goertek.asp.bledemo.R;

public class BtDevActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_dev);
    }

    @Override
    Handler getUiHandler() {
        return null;
    }
}

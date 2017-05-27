package com.goertek.asp.bledemo.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public  abstract class BaseActivity extends AppCompatActivity implements BaseBroadcastReceiver
        .BroadcastReceiverListener{

    private BaseBroadcastReceiver mReceiver;

    protected BluetoothAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterReceiver();
    }

    private void init() {
        mReceiver = new BaseBroadcastReceiver(this);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void startEnabledBtActivity() {
        if(mBtAdapter==null)
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBtAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver,intentFilter);
    }

    private void unRegisterReceiver() {
        unregisterReceiver(mReceiver);
    }

    abstract Handler getUiHandler();

    @Override
    public void onBluetoothDisabled() {
        startEnabledBtActivity();
    }
    @Override
    public void onBluetoothEnabled() {

    }
}

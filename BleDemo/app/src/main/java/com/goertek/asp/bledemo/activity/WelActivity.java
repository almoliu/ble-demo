package com.goertek.asp.bledemo.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.goertek.asp.bledemo.R;

import java.lang.ref.WeakReference;

public class WelActivity extends AppCompatActivity {

    private static final String TAG = WelActivity.class.getSimpleName();

    private static final int HANDLER_MSG_TOAST = 0x01;

    private BluetoothAdapter mBtAdapter;
    private UiHandler mUiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wel);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBtEnabledActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void init() {
        mUiHandler = new UiHandler(this);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startBtEnabledActivity() {
        if(mBtAdapter==null)
            return;
        if(!mBtAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }else {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        if(mUiHandler==null)
            return;
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelActivity.this,MainActivity.class);
                startActivity(intent);
            }
        },3000);
    }

    private static class UiHandler extends Handler {

        private WeakReference<WelActivity> mWeakRef;

        public UiHandler(WelActivity activity) {
            super();
            mWeakRef = new WeakReference<WelActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            Activity activity = mWeakRef.get();
            if(activity==null)
                return;
            switch (msg.what) {
                case HANDLER_MSG_TOAST:
                    String toast = (String)msg.obj;
                    Toast.makeText(activity,toast,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

        }
    }
}

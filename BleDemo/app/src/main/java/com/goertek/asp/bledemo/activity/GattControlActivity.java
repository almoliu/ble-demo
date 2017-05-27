package com.goertek.asp.bledemo.activity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;

import com.goertek.asp.bledemo.R;
import com.goertek.asp.bledemo.service.BluetoothLeService;
import com.goertek.asp.bledemo.util.GattLink;
import com.goertek.asp.bledemo.view.GattNotifyViewManager;
import com.goertek.asp.bledemo.view.GattReadViewManager;
import com.goertek.asp.bledemo.view.GattWriteViewManager;

public class GattControlActivity extends AppCompatActivity {

    private static final String TAG = GattControlActivity.class.getSimpleName();

    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothLeService mLeService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLeService = ((BluetoothLeService.LocalBinder)service).getService();
            initViews();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLeService = null;
        }
    };

    private GattNotifyViewManager mGattNotifyViewManager;
    private GattReadViewManager mGattReadViewManager;
    private GattWriteViewManager mGattWriteViewManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt_control);
        bindLeService();
    }

    private void initViews() {
        mCharacteristic = GattLink.getInstance().getCharacteristic();
        if(mCharacteristic==null)
            finish();
        Log.d(TAG,"uuid:"+mCharacteristic.getUuid());

        int properties = mCharacteristic.getProperties();
        if((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY)>0) {
            Log.d(TAG,"notification view");
            final View view = ((ViewStub)findViewById(R.id.id_notify_viewStub)).inflate();
            mGattNotifyViewManager = new GattNotifyViewManager(view,
                    GattControlActivity.this);
            mGattNotifyViewManager.initViews();
        }

        if((properties & BluetoothGattCharacteristic.PROPERTY_READ)>0) {
            Log.d(TAG,"read view");
            final View view = ((ViewStub)findViewById(R.id.id_read_viewStub)).inflate();
            mGattReadViewManager = new GattReadViewManager(view,GattControlActivity.this);
            mGattReadViewManager.initViews();

        }

        if((properties & BluetoothGattCharacteristic.PROPERTY_WRITE)>0) {
            Log.d(TAG,"write view");
            final View view = ((ViewStub)findViewById(R.id.id_write_viewStub)).inflate();
            mGattWriteViewManager = new GattWriteViewManager(view,GattControlActivity.this);
            mGattWriteViewManager.initViews();
        }
    }

    public BluetoothLeService getLeService() {
        return mLeService;
    }

    public BluetoothGattCharacteristic getLeCharacteristic() {
        return mCharacteristic;
    }

    private void bindLeService() {
        Intent intent = new Intent(GattControlActivity.this,BluetoothLeService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    private void unbindLeService() {
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mGattNotifyViewManager!=null)
            mGattNotifyViewManager.onDestroy();
        if(mGattReadViewManager!=null)
            mGattReadViewManager.onDestroy();
        if(mGattWriteViewManager!=null)
            mGattWriteViewManager.onDestroy();
        unbindLeService();
    }
}

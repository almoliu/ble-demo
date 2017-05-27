package com.goertek.asp.bledemo.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.goertek.asp.bledemo.R;
import com.goertek.asp.bledemo.adapter.LeDevsAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

public class BleScanActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = BleScanActivity.class.getSimpleName();

    public static final String RESULT_BT_DEVICE = "bluetooth device";

    private BleCallBack mBleCallBack;
    private UiHandler mUiHandler;

    private LeDevsAdapter mLeDevAdapter;

    private boolean isLeScanning = false;

    private ListView mListView;

    private Runnable mStopLeScanRunnable  = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"stop ble scan...");
            startScanBleDev(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);
        initViews();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       final MenuItem scanItem = menu.getItem(0);
        if(isLeScanning)
            scanItem.setTitle("stop");
        else
            scanItem.setTitle("start");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ble_scan_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG,"onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menu_scan_btn:
                if(isLeScanning) {
                    mUiHandler.removeCallbacks(mStopLeScanRunnable);
                    startScanBleDev(false);
                } else {
                    startScanBleDev(true);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startScanBleDev(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
       if(isLeScanning) {
           mUiHandler.removeCallbacksAndMessages(null);
           startScanBleDev(false);
       }
    }

    private void initViews() {

        final Toolbar toolbar = (Toolbar)findViewById(R.id.menu_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ble Scan");

        mBleCallBack = new BleCallBack(this);
        mUiHandler  = new UiHandler(this);
        mLeDevAdapter = new LeDevsAdapter(getLayoutInflater());
        mListView = (ListView)findViewById(R.id.ble_device_list);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mLeDevAdapter);
    }

    private void startScanBleDev(boolean value) {

        if(value) {
            mLeDevAdapter.clear();
            mBtAdapter.getBluetoothLeScanner().startScan(mBleCallBack);
            mUiHandler.postDelayed(mStopLeScanRunnable,5000);
            isLeScanning = true;
        }else {
            mBtAdapter.getBluetoothLeScanner().stopScan(mBleCallBack);
            isLeScanning = false;
        }
        invalidateOptionsMenu();
    }


    @Override
   protected Handler getUiHandler() {
        if(mUiHandler==null)
            mUiHandler = new UiHandler(this);
        return mUiHandler;
    }

    public LeDevsAdapter getAdapter() {
        return mLeDevAdapter;
    }

    @Override
    public void onBluetoothDisabled() {
        super.onBluetoothDisabled();
    }

    @Override
    public void onBluetoothEnabled() {
        super.onBluetoothEnabled();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG,"position:"+position);
        Object object = mLeDevAdapter.getItem(position);
        if(object instanceof BluetoothDevice) {
            BluetoothDevice btDevice = (BluetoothDevice)object;
            Log.d(TAG,"device name:"+btDevice.getName());
            Intent intent = new Intent();
            intent.putExtra(RESULT_BT_DEVICE,btDevice);
            setResult(RESULT_OK,intent);
            finish();
        }
    }

    private static class UiHandler extends Handler {

        private WeakReference<BleScanActivity> mWeakRef;

        public UiHandler(BleScanActivity activity) {
            super();
            mWeakRef = new WeakReference<BleScanActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = mWeakRef.get();
            if(activity==null)
                return;
        }
    }

    private static class BleCallBack extends ScanCallback {

        private WeakReference<BleScanActivity> mBleWeakRef;

        public BleCallBack(BleScanActivity activity) {
            super();
            mBleWeakRef = new WeakReference<BleScanActivity>(activity);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BleScanActivity bleScanActivity = mBleWeakRef.get();
            if(bleScanActivity==null)
                return;
            bleScanActivity.getAdapter().addDev(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG,"onBatchScanResults");
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG,"onScanFailed");
            super.onScanFailed(errorCode);
        }
    }

}

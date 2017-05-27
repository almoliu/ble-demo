package com.goertek.asp.bledemo.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.goertek.asp.bledemo.R;
import com.goertek.asp.bledemo.adapter.GattServiceAdapter;
import com.goertek.asp.bledemo.service.BluetoothLeService;
import com.goertek.asp.bledemo.util.Constant;
import com.goertek.asp.bledemo.util.GattLink;
import com.goertek.asp.bledemo.util.SampleGattAttributes;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

public class MainActivity extends BaseActivity implements ExpandableListView
        .OnGroupClickListener,ExpandableListView.OnChildClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_SCAN_BLE = 0x12;

    private BluetoothLeService mLeService;
    private UiHandler mUiHandler;

    private BluetoothDevice mBtCntDev;

    private boolean is_connected = false;

    private ExpandableListView mList;
    private GattServiceAdapter mGattServiceAdapter;

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG,"onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        mBtCntDev = savedInstanceState.getParcelable("bluetooth device");
        is_connected = savedInstanceState.getBoolean("bluetooth connection");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"onSaveInstanceState");
        outState.putParcelable("bluetooth device",mBtCntDev);
        outState.putBoolean("bluetooth connection",is_connected);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem cntItem = menu.findItem(R.id.menu_main_connection_state);
        if(is_connected) {
            cntItem.setTitle(mBtCntDev.getName());
        } else {
            cntItem.setTitle("disconnected");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_connection_state:
                if(mBtCntDev==null)
                    return true;
                if(is_connected) {
                    mLeService.disconnect();
                } else {
                    Log.d(TAG,"reconnect");
                    mLeService.reconnect();
                }
                return true;
            case R.id.menu_main_scan_ble_devices:
                if(mLeService==null)
                    return true;
                mLeService.disconnect();
                startBleScanActivity();
                return true;
            case R.id.menu_launch_bt_dev_activity:
                launchBtDevActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK&&requestCode==REQUEST_CODE_SCAN_BLE) {
            mBtCntDev = data.getParcelableExtra(BleScanActivity.RESULT_BT_DEVICE);
            Log.d(TAG,"result bt :"+mBtCntDev.getName());

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLeService.connect(mBtCntDev);
                }
            },200);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void init() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        mList = (ExpandableListView) findViewById(R.id.main_list);
        mList.setOnGroupClickListener(this);
        mList.setOnChildClickListener(this);
        mUiHandler = new UiHandler(this);
        Intent intent = new Intent(MainActivity.this, BluetoothLeService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    public Handler getUiHandler() {
        if(mUiHandler==null)
            mUiHandler = new UiHandler(this);
        return mUiHandler;
    }

    private BluetoothDevice getCntDevice() {
        return mBtCntDev;
    }

    private void closeDevice() {
        mLeService.close();
    }

    private void updateCntState(boolean value) {
        is_connected = value;
        if(!value) {
            mList.setAdapter((BaseExpandableListAdapter)null);
            mGattServiceAdapter = null;
        }
        invalidateOptionsMenu();
    }

    private void displayGattServices() {
        if(mLeService==null)
            return;
        mGattServiceAdapter = new GattServiceAdapter(getLayoutInflater(),
                mLeService.getAllSupportedGattServices());
        mList.setAdapter(mGattServiceAdapter);
    }

    private void startBleScanActivity() {
        Intent intent = new Intent(MainActivity.this,BleScanActivity.class);
        startActivityForResult(intent,REQUEST_CODE_SCAN_BLE);
    }

    private void connectGattCharacteristic(final  BluetoothGattCharacteristic characteristic) {
        GattLink.getInstance().setCharacteristic(characteristic);
        Intent intent = new Intent(MainActivity.this,GattControlActivity.class);
        startActivity(intent);
    }

    private void connectGattService(final BluetoothGattService service) {
        if(service.getUuid().equals(UUID.fromString(SampleGattAttributes.UUID_HR_SERVICE))) {
            List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
            for(final BluetoothGattCharacteristic characteristic:gattCharacteristics) {
                if (characteristic.getUuid().equals(UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT))) {
                    final int charaProp = characteristic.getProperties();
                    Log.d(TAG,"charaProp:"+charaProp);
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        Log.d(TAG,"BluetoothGattCharacteristic.PROPERTY_READ");
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (mNotifyCharacteristic != null) {
                            mLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mLeService.readCharacteristic(characteristic);
                    }

                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        Log.d(TAG,"BluetoothGattCharacteristic.PROPERTY_NOTIFY");
                        mLeService.setCharacteristicNotification(characteristic, true);
                    }
                }
            }
        }else if(service.getUuid().equals(UUID.fromString(SampleGattAttributes.UUID_DEVICE_INFORMATION_SERVICE))){
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            int  delay = 0;
            for(final BluetoothGattCharacteristic characteristic:characteristics) {
                int property = characteristic.getProperties();
                if ((property | BluetoothGattCharacteristic.PERMISSION_READ) > 0) {

                   delay = delay+1000;
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "read-----------------");
                            mLeService.readCharacteristic(characteristic);
                        }
                    },delay);
                }
            }
        }
    }

    private void launchBtDevActivity() {
        Intent intent = new Intent(MainActivity.this,BtDevActivity.class);
        startActivity(intent);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mLeService.setMsgHandler(mUiHandler);
            if(mBtCntDev!=null)
                mLeService.connect(mBtCntDev);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLeService = null;
        }
    };

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        Log.d(TAG,"group clicked:"+groupPosition);
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Log.d(TAG,"child clicked:"+groupPosition+":"+childPosition);
        Object obj = mGattServiceAdapter.getChild(groupPosition,childPosition);
        if(obj instanceof BluetoothGattCharacteristic) {
            BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic)obj;
            connectGattCharacteristic(characteristic);
        }
        return true;
    }

    private static class UiHandler extends Handler {

        private WeakReference<MainActivity> mWeakRef;

        public UiHandler(MainActivity activity) {
            super();
            mWeakRef = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            MainActivity activity = mWeakRef.get();
            if(activity==null)
                return;
            switch (msg.what) {
                case Constant.MSG_BLE_CONNECTION:
                    if(msg.obj instanceof BluetoothDevice) {
                        BluetoothDevice btDev = (BluetoothDevice) msg.obj;
                        if(!btDev.getAddress().equals(activity.getCntDevice().getAddress())) {
                            Log.d(TAG,"not the same device address");
                            activity.closeDevice();
                           activity.mBtCntDev = null;
                        }
                        activity.updateCntState(true);
                    }
                    break;
                case Constant.MSG_BLE_DISCONNECTION:
                    activity.updateCntState(false);
                    break;
                case Constant.MSG_BLE_DISCOVERY_END:
                    activity.displayGattServices();
                    break;
                case Constant.MSG_BLE_DATA_AVAILABLE:
                default:
                    break;
            }
        }
    }

}

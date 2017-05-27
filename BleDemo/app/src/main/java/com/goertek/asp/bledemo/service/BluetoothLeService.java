package com.goertek.asp.bledemo.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.goertek.asp.bledemo.util.Constant;
import com.goertek.asp.bledemo.util.SampleGattAttributes;
import com.goertek.asp.bledemo.view.GattNotifyViewManager;
import com.goertek.asp.bledemo.view.GattReadViewManager;
import com.goertek.asp.bledemo.view.GattWriteViewManager;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

    private static final String TAG = BluetoothLeService.class.getSimpleName();

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    private BluetoothGatt mBluetoothGatt;

    private final IBinder mBinder = new LocalBinder();
    private Handler mMsgHandler;

    private Handler mMsgNotifyHandler;
    private Handler mMsgReadHandler;
    private Handler mMsgWriteHandler;

    public BluetoothLeService() {
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG,"onConnectionStateChange:"+status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG,"Ble connected...");
                notifyStateUpdate(Constant.MSG_BLE_CONNECTION,mBluetoothGatt.getDevice());
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG,"Ble disconnected...");
               notifyStateUpdate(Constant.MSG_BLE_DISCONNECTION);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG,"onServicesDiscovered");
            super.onServicesDiscovered(gatt, status);
            notifyStateUpdate(Constant.MSG_BLE_DISCOVERY_END);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG,"onCharacteristicRead");
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
              updateReadData(characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG,"onCharacteristicWrite");
            super.onCharacteristicWrite(gatt, characteristic, status);
            updateWriteData(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG,"onCharacteristicChanged");
            super.onCharacteristicChanged(gatt, characteristic);
            updateNotifyData(characteristic);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void setMsgHandler(final Handler handler) {
        mMsgHandler = handler;
    }

    public void setMsgNotifyHandler(final  Handler handler) {
        mMsgNotifyHandler = handler;
    }

    public void  setMsgReadHandler(final  Handler handler) {
        mMsgReadHandler = handler;
    }

    public void setMsgWriteHandler(final  Handler handler) {
        mMsgWriteHandler = handler;
    }

    public void connect(final BluetoothDevice device) {
        if(mBluetoothGatt!=null) {
            Log.d(TAG,"disconnected in connect()");
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    public void reconnect() {
        mBluetoothGatt.connect();
    }

    public void disconnect() {
        if(mBluetoothGatt!=null) {
            mBluetoothGatt.disconnect();
        }
    }

    public void close() {
        if(mBluetoothGatt!=null) {
            Log.d(TAG,"Ble close");
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if ( mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        for(BluetoothGattDescriptor descriptor:characteristic.getDescriptors()) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        /*
        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        */
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic,String value) {
        Log.d(TAG," writeCharacteristic:"+value);
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        characteristic.setValue(value.getBytes());
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public List<BluetoothGattService> getAllSupportedGattServices() {
        if(mBluetoothGatt==null)
            return null;
        return mBluetoothGatt.getServices();
    }


    private void notifyStateUpdate(int action) {
        mMsgHandler.obtainMessage(action).sendToTarget();
    }

    private void notifyStateUpdate(int action,BluetoothDevice bluetoothDevice) {
        mMsgHandler.obtainMessage(action,bluetoothDevice)
                .sendToTarget();
    }

    private void updateReadData(BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        Log.d(TAG,"data is:\n"+new String(data));
        mMsgReadHandler.obtainMessage(GattReadViewManager.MSG_READ_DATA_AVAILABLE,new
                String(data)).sendToTarget();
    }

    private void updateNotifyData(BluetoothGattCharacteristic characteristic) {

        if(UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if((flag&0x01)!=0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            }else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            final int heartRate = characteristic.getIntValue(format,1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            mMsgNotifyHandler.obtainMessage(GattNotifyViewManager.MSG_NOTIFY_DATA_AVAILABLE,heartRate)
                    .sendToTarget();
        }else {

            final byte[] data = characteristic.getValue();
            Log.d(TAG,"data is:\n"+new String(data));
            mMsgNotifyHandler.obtainMessage(GattNotifyViewManager.MSG_NOTIFY_DATA_AVAILABLE,new
                    String(data)).sendToTarget();
        }

    }

    private void updateWriteData(BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        Log.d(TAG,"data is:\n"+new String(data));
        mMsgWriteHandler.obtainMessage(GattWriteViewManager.MSG_WRITE_DATA_AVAILABLE,new
                String(data)).sendToTarget();
    }

    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        if(null != gatt){
            try {
                BluetoothGatt localBluetoothGatt = gatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod( "refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(
                            localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        }
        return false;
    }

    private void  notifyStateUpdate(BluetoothGattCharacteristic characteristic,int action) {
        if(UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if((flag&0x01)!=0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            }else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            final int heartRate = characteristic.getIntValue(format,1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            mMsgHandler.obtainMessage(action,heartRate).sendToTarget();
        }else {
            final byte[] data = characteristic.getValue();
            Log.d(TAG,"data is:\n"+new String(data));
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}

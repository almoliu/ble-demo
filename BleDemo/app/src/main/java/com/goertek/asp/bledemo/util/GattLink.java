package com.goertek.asp.bledemo.util;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by almo.liu on 2017/5/26.
 */

public class GattLink {

    private BluetoothGattCharacteristic mCharacteristic;
    private static GattLink mGattLink;

    static {
        mGattLink = new GattLink();
    }

    public static GattLink getInstance() {
        return mGattLink;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(mCharacteristic!=null)
            mCharacteristic = null;
        mCharacteristic = characteristic;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return mCharacteristic;
    }

}

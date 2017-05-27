package com.goertek.asp.bledemo.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BaseBroadcastReceiver extends BroadcastReceiver {

    private BroadcastReceiverListener mListener;

    public BaseBroadcastReceiver(BroadcastReceiverListener listener) {
        super();
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1);
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    mListener.onBluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    mListener.onBluetoothDisabled();
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * The interface used to communicate with this object.
     */
    public interface BroadcastReceiverListener {
        /**
         * When the application is informed that the Bluetooth is disabled, this method is called.
         */
        void onBluetoothDisabled();
        /**
         * When the application is informed that the Bluetooth is enabled, this method is called.
         */
        void onBluetoothEnabled();
    }
}

package com.goertek.asp.bledemo.view;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goertek.asp.bledemo.R;
import com.goertek.asp.bledemo.activity.GattControlActivity;

import java.lang.ref.WeakReference;

/**
 * Created by almo.liu on 2017/5/26.
 */

public class GattNotifyViewManager {

    private static final String TAG = GattNotifyViewManager.class.getSimpleName();

    public static final int MSG_NOTIFY_DATA_AVAILABLE = 0x101;

    private GattControlActivity mGattCtrlActivity;
    private View mRootView;
    private TextView mValueField;

    private MsgHandler mMsgHandler;
    private boolean is_notified = false;


    public GattNotifyViewManager(View view, GattControlActivity activity) {
        mGattCtrlActivity = activity;
        mRootView = view;
    }

    public void initViews() {
        mMsgHandler = new MsgHandler(this);
        mGattCtrlActivity.getLeService().setMsgNotifyHandler(mMsgHandler);
        final Button notifyBtn = (Button)mRootView.findViewById(R.id.id_notify_view_btn);
        notifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!is_notified) {
                    mGattCtrlActivity.getLeService().setCharacteristicNotification(mGattCtrlActivity
                            .getLeCharacteristic(), true);
                    notifyBtn.setText("Stop Notification");
                    is_notified = true;
                } else {
                    mGattCtrlActivity.getLeService().setCharacteristicNotification(mGattCtrlActivity
                            .getLeCharacteristic(), false);
                    notifyBtn.setText("Set Notification");
                    is_notified = false;
                }
            }
        });

        mValueField = (TextView)mRootView.findViewById(R.id.id_notify_view_value);

    }

    public void onDestroy() {
        if(is_notified) {
            mGattCtrlActivity.getLeService().setCharacteristicNotification(mGattCtrlActivity
                    .getLeCharacteristic(), false);
            is_notified = false;
        }
        mMsgHandler = null;
        mGattCtrlActivity.getLeService().setMsgNotifyHandler(null);
    }

    private void updateData(Object value) {
            mValueField.setText(String.valueOf(value));
    }

    private static class MsgHandler extends Handler {

        private WeakReference<GattNotifyViewManager> mWeakRef;

        public MsgHandler(GattNotifyViewManager activity) {
            super();
            mWeakRef = new WeakReference<GattNotifyViewManager>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            GattNotifyViewManager activity = mWeakRef.get();
            if(activity==null)
                return;
            if(msg.what==MSG_NOTIFY_DATA_AVAILABLE) {
                activity.updateData(msg.obj);
            }

        }
    }

}

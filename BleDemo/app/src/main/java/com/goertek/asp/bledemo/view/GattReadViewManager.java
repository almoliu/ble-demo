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

public class GattReadViewManager {
    private static final String TAG = GattReadViewManager.class.getSimpleName();

    public static final int MSG_READ_DATA_AVAILABLE = 0x102;

    private GattControlActivity mGattCtrlActivity;
    private View mRootView;
    private TextView mValueField;
    private MsgHandler mMsgHandler;

    public GattReadViewManager(View view, GattControlActivity activity) {
        mGattCtrlActivity = activity;
        mRootView = view;
    }

    public void initViews() {
        mMsgHandler = new MsgHandler(this);
        mGattCtrlActivity.getLeService().setMsgReadHandler(mMsgHandler);
        final Button notifyBtn = (Button)mRootView.findViewById(R.id.id_read_view_btn);
        notifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mGattCtrlActivity.getLeService().readCharacteristic(mGattCtrlActivity.getLeCharacteristic());
            }
        });

        mValueField = (TextView)mRootView.findViewById(R.id.id_read_view_value);

    }

    public void onDestroy() {
        mMsgHandler = null;
    }

    private void updateData(Object value) {
        mValueField.setText(String.valueOf(value));
    }

    private static class MsgHandler extends Handler {

        private WeakReference<GattReadViewManager> mWeakRef;

        public MsgHandler(GattReadViewManager activity) {
            super();
            mWeakRef = new WeakReference<GattReadViewManager>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            GattReadViewManager activity = mWeakRef.get();
            if(activity==null)
                return;
            if(msg.what==MSG_READ_DATA_AVAILABLE) {
                activity.updateData(msg.obj);
            }

        }
    }
}

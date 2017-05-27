package com.goertek.asp.bledemo.view;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.goertek.asp.bledemo.R;
import com.goertek.asp.bledemo.activity.GattControlActivity;

import java.lang.ref.WeakReference;

/**
 * Created by almo.liu on 2017/5/26.
 */

public class GattWriteViewManager {

    private static final String TAG = GattReadViewManager.class.getSimpleName();

    public static final int MSG_WRITE_DATA_AVAILABLE = 0x103;

    private GattControlActivity mGattCtrlActivity;
    private View mRootView;
    private EditText mValueField;
    private TextView mReturnValueField;
    private MsgHandler mMsgHandler;

    public GattWriteViewManager(View view, GattControlActivity activity) {
        mGattCtrlActivity = activity;
        mRootView = view;
    }

    public void initViews() {
        mMsgHandler = new MsgHandler(this);
        mGattCtrlActivity.getLeService().setMsgWriteHandler(mMsgHandler);
        final Button writeBtn = (Button)mRootView.findViewById(R.id.id_write_view_btn);
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGattCtrlActivity.getLeService().writeCharacteristic(mGattCtrlActivity
                        .getLeCharacteristic(),mValueField.getText().toString());
            }
        });

        mValueField = (EditText) mRootView.findViewById(R.id.id_write_view_value);
        mReturnValueField = (TextView)mRootView.findViewById(R.id.id_write_view_return_value);

    }

    public void onDestroy() {
        mMsgHandler = null;
    }

    private void updateData(Object value) {
        mReturnValueField.setText(String.valueOf(value));
    }

    private static class MsgHandler extends Handler {

        private WeakReference<GattWriteViewManager> mWeakRef;

        public MsgHandler(GattWriteViewManager activity) {
            super();
            mWeakRef = new WeakReference<GattWriteViewManager>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            GattWriteViewManager activity = mWeakRef.get();
            if(activity==null)
                return;
            if(msg.what==MSG_WRITE_DATA_AVAILABLE) {
                activity.updateData(msg.obj);
            }

        }
    }
}

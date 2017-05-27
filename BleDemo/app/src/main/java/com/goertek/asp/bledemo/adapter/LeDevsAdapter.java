package com.goertek.asp.bledemo.adapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.goertek.asp.bledemo.R;

import java.util.ArrayList;

/**
 * Created by almo.liu on 2017/5/22.
 */

public class LeDevsAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mLeDevsList;
    private LayoutInflater mInflator;

    public LeDevsAdapter(LayoutInflater inflater) {
        super();
        mLeDevsList = new ArrayList<>();
        mInflator = inflater;
    }

    public void addDev(BluetoothDevice dev) {
        if(!mLeDevsList.contains(dev))
            mLeDevsList.add(dev);
        notifyDataSetChanged();
    }

    public void clear() {
        mLeDevsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLeDevsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mLeDevsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null) {
            convertView = mInflator.inflate(R.layout.le_dev_list_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.deviceConnected = (CheckBox) convertView.findViewById(R.id.scanicon);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mLeDevsList.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText("unknown device");
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.deviceConnected.setChecked(false);
        return convertView;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        CheckBox deviceConnected;
    }
}

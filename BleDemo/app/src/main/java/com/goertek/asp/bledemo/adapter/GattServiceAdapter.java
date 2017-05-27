package com.goertek.asp.bledemo.adapter;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.goertek.asp.bledemo.R;
import com.goertek.asp.bledemo.util.SampleGattAttributes;

import java.util.List;

/**
 * Created by almo.liu on 2017/5/25.
 */

public class GattServiceAdapter extends BaseExpandableListAdapter {

    private static final String TAG = GattServiceAdapter.class.getSimpleName();

    private List<BluetoothGattService> mGroupList;
    private LayoutInflater mInflater;

    public GattServiceAdapter(LayoutInflater inflater) {
        super();
        mInflater = inflater;
    }

    public GattServiceAdapter(LayoutInflater inflater,List<BluetoothGattService> list) {
        super();
        mGroupList = list;
        mInflater = inflater;
    }

    public void setGroupList(List<BluetoothGattService> list) {
        mGroupList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroupList.get(groupPosition).getCharacteristics().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroupList.get(groupPosition).getCharacteristics().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*10+childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder gViewHolder;
        if(convertView==null) {
            convertView = mInflater.inflate(R.layout.gatt_services_item_layout, null);
            gViewHolder = new GroupViewHolder();
            gViewHolder.gattName = (TextView) convertView.findViewById(R.id.id_gatt_name);
            gViewHolder.gattUuid = (TextView)convertView.findViewById(R.id.id_uuid);
            gViewHolder.gattInstantId = (TextView)convertView.findViewById(R.id.id_instantId);
            gViewHolder.gattType = (TextView)convertView.findViewById(R.id.id_type);
            convertView.setTag(gViewHolder);
        }else {
            gViewHolder =  (GroupViewHolder)convertView.getTag();
        }

        final BluetoothGattService service = mGroupList.get(groupPosition);

        gViewHolder.gattName.setText(SampleGattAttributes.lookup(service.getUuid().toString(),"unknown device"));
        gViewHolder.gattUuid.setText(service.getUuid().toString());
        gViewHolder.gattInstantId.setText(String.valueOf(service.getInstanceId()));
        if(service.getType()==0) {
           gViewHolder.gattType.setText("primary");
        }else if(service.getType()==1) {
           gViewHolder.gattType.setText("secondary");
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder cViewHolder;
        if(convertView==null) {
            convertView = mInflater.inflate(R.layout.gatt_chact_item_layout,null);
            cViewHolder = new ChildViewHolder();
            cViewHolder.chractName = (TextView)convertView.findViewById(R.id.id_chact_name);
            cViewHolder.chractUuid = (TextView)convertView.findViewById(R.id.id_chact_uuid);
            cViewHolder.chractInstantId = (TextView)convertView.findViewById(R.id
                    .id_chact_instantId);
            cViewHolder.chractProperty = (TextView)convertView.findViewById(R.id
                    .id_chact_property);

            convertView.setTag(cViewHolder);
        }else {
            cViewHolder =(ChildViewHolder) convertView.getTag();
        }
        BluetoothGattCharacteristic characteristic = mGroupList.get(groupPosition)
                .getCharacteristics().get(childPosition);

        cViewHolder.chractName.setText(SampleGattAttributes.lookup(characteristic.getUuid().toString(),
                "unknown characteristic"));
        cViewHolder.chractUuid.setText(characteristic.getUuid().toString());
        cViewHolder.chractInstantId.setText(String.valueOf(characteristic.getInstanceId()));
        cViewHolder.chractProperty.setText(String.valueOf(characteristic.getProperties()));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView gattName;
        TextView gattUuid;
        TextView gattInstantId;
        TextView gattType;
    }
    static class ChildViewHolder {
        TextView chractName;
        TextView chractUuid;
        TextView chractInstantId;
        TextView chractProperty;
    }
}

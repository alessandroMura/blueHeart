package com.example.blueheart;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.TextView;

import java.util.ArrayList;

public class deviceAdapter extends ArrayAdapter<BluetoothDevice> {

    deviceAdapter(Activity context, ArrayList<BluetoothDevice> devices) {
        super(context, 0, devices);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_layout, parent, false);
        }

        BluetoothDevice currentDevice = getItem(position);
        TextView deviceText = listItemView.findViewById(R.id.item_button);
        if (currentDevice != null) {
            deviceText.setText(currentDevice.getName());
        }
        TextView deviceText2 = listItemView.findViewById(R.id.item_button2);
        if (currentDevice != null) {
            deviceText2.setText(currentDevice.getAddress());
        }
        return listItemView;
    }
}
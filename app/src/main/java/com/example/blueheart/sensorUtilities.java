package com.example.blueheart;

import android.content.Context;
import android.util.Log;

import java.util.List;

import sew.CommunicationException;
import sew.DeviceException;
import sew.DeviceFinder;
import sew.SewBluetoothDevice;

import static com.example.blueheart.deviceListActivity.sewDevice;
import static com.example.blueheart.utilities.showToast;

public class sensorUtilities {

    public static  void setupSensor(Context context) {
        List<SewBluetoothDevice> bluetoothDeviceList = DeviceFinder.findPairedDevices("sew");
        for (int i = 0; i < bluetoothDeviceList.size(); i++) {
            if (bluetoothDeviceList.get(i).getName().equals(deviceListActivity.currentID)) {
                sewDevice = bluetoothDeviceList.get(i);
                Log.v("sewdevice", "created");
                Log.v("sewdevice", sewDevice.getAddress());
            } else {
                showToast(context,"Selected!= found");
            }
        }

    }

    public static void tryConnect(SewBluetoothDevice s) {
        try {
            Log.v("sewdevice", "Connecting....");
            s.connect();
            Log.v("sewdevice", "Connected");
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }

    }

    public static void tryDisconnect(SewBluetoothDevice s) {
        try {
            Log.v("sewdevice", "Disconnecting...");
            s.disconnect();
            Log.v("sewdevice", "Disconnected");
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }

    }

    public static int isConnected(SewBluetoothDevice s) {
        try {
            int stat;
            Log.v("sewdevice", "Checking Status...");
            stat = s.getStatus();
            Log.v("sewdevice", "Status=  " + stat);
            return stat;


        } catch (CommunicationException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void tryStream(SewBluetoothDevice s) {
        try {
            Log.v("sewdevice", "Starting Stream....");
            s.startStreaming();
            Log.v("sewdevice", "Streaming Started!");
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }

    }

    public static void tryStopStream(SewBluetoothDevice s) {
        try {
            Log.v("sewdevice", "Stopping Stream....");
            s.stopStreaming();
            Log.v("sewdevice", "Streaming Stopped!");
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInStreaming(SewBluetoothDevice s) {
        try {
            boolean is;
            Log.v("sewdevice", "Checking Stream...");
            is = s.isStreaming();
            if (is) {
                Log.v("sewdevice", "Is streaming");
                return true;

            } else {
                Log.v("sewdevice", "Not streaming");
                return false;
            }

        } catch (CommunicationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long tryGetClock(SewBluetoothDevice s) {

        try {

            return s.getClock();

        } catch (CommunicationException e) {
            e.printStackTrace();
        }
        return -1;
    }

}

package com.psimao.bluetoothsample.bluetooth.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by pedro on 6/30/16.
 * Receiver for Bluetooth interactions sample
 */
public class BluetoothReceiver extends BroadcastReceiver {

    private OnDeviceFoundListener deviceFoundListener;
    private OnDiscoveryFinishedListener discoveryFinishedListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(deviceFoundListener != null)
                deviceFoundListener.onDeviceFound(device);
        } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            if(discoveryFinishedListener != null)
                discoveryFinishedListener.onDeviceFinished();
        }
    }

    public void setOnDeviceFoundListener(OnDeviceFoundListener deviceFoundListener){
        this.deviceFoundListener = deviceFoundListener;
    }

    public void setOnDiscoveryFinishedListener(OnDiscoveryFinishedListener discoveryFinishedListener){
        this.discoveryFinishedListener = discoveryFinishedListener;
    }

    public interface OnDeviceFoundListener {
        void onDeviceFound(BluetoothDevice device);
    }

    public interface OnDiscoveryFinishedListener {
        void onDeviceFinished();
    }
}

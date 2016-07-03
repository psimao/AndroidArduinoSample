package com.psimao.bluetoothsample.bluetooth.view;

import android.bluetooth.BluetoothDevice;
import android.os.Message;

import java.util.List;

/**
 * Created by pedro on 6/30/16.
 * View Interface para ações a serem feitas quando novos devices forem encontrados e para quando
 * a descoberta for finalizada.
 */
public interface BluetoothView {

    void onDeviceDiscoveryStart();

    void onDeviceDiscoveryFinished(List<BluetoothDevice> devices);

    void onMessageReceived(Message message);
}

package com.psimao.bluetoothsample.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.psimao.bluetoothsample.bluetooth.receiver.BluetoothReceiver;
import com.psimao.bluetoothsample.bluetooth.thread.ThreadClient;
import com.psimao.bluetoothsample.bluetooth.view.BluetoothView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedro on 6/30/16.
 * Dedicated class for bluetooth controlling
 */
public class BluetoothController implements BluetoothReceiver.OnDeviceFoundListener, BluetoothReceiver.OnDiscoveryFinishedListener {

    private Context context;
    private BluetoothView view;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothReceiver receiver;

    private ThreadClient threadClient;
    private Handler handler;

    public BluetoothController(Context context) {
        this(context, null);
    }

    public BluetoothController(Context context, final BluetoothView view) {
        this.context = context;
        this.view = view;
        this.handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                if(BluetoothController.this.view != null)
                    BluetoothController.this.view.onMessageReceived(inputMessage);
            }
        };
    }

    private List<BluetoothDevice> devices = new ArrayList<>();

    /**
     * Inicia adapter do bluetooth e checa se o aparelho é compativel, além de se está habilitado.
     */
    public void init() {
        // Inicia Adapter e caso tenha suporte, checa se está desligado para pedir permissão para ativá-lo
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initReceiver();
    }

    /**
     * Para os receivers e Threads de conexão Bluetooth
     */
    public void destroy() {
        context.unregisterReceiver(receiver);
        stopAll();
    }

    /**
     * Checa se está habilitado
     */
    public boolean isEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Inicia a descoberta de dispositivos
     */
    public void startDeviceDiscovery() {
        stopAll();
        devices.clear();
        bluetoothAdapter.startDiscovery();
        if (view != null)
            view.onDeviceDiscoveryStart();
    }

    /**
     * Cancela a descoberta de dispositivos
     */
    public void cancelDeviceDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            stopAll();
        }
    }

    public BluetoothDevice getDiscoveredDeviceAtPosition(int position) {
        if (devices.size() >= (position - 1))
            return devices.get(position);
        return null;
    }

    public void connectToDevice(BluetoothDevice device) {
        stopAll();
        threadClient = new ThreadClient(device, handler);
        threadClient.start();
    }

    /**
     * Envia uma mensagem ao dispositivo conectado
     *
     * @param msg mensagem a ser enviada
     */
    public void sendMessage(String msg) {
        if (threadClient != null)
            threadClient.writeMessage(msg);
    }

    /**
     * Inicia receiver
     */
    private void initReceiver() {
        receiver = new BluetoothReceiver();
        receiver.setOnDeviceFoundListener(this);
        receiver.setOnDiscoveryFinishedListener(this);
        context.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    /**
     * Desativa threads
     */
    private void stopAll() {
        if (threadClient != null) {
            threadClient.close();
            threadClient = null;
        }
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        devices.add(device);
    }

    @Override
    public void onDeviceFinished() {
        if (view != null)
            view.onDeviceDiscoveryFinished(this.devices);
    }
}

package com.psimao.bluetoothsample.bluetooth.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by pedro on 7/3/16.
 * Thread Client para conexão Bluetooth
 */
public class ThreadClient extends Thread {

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private String nome;

    private DataInputStream in;
    private DataOutputStream out;

    private Handler handler;

    public static final int HANDLER_RECEIVED_TEXT = 1;
    public static final int HANDLER_ERROR = 2;

    public ThreadClient(BluetoothDevice device){
        this(device, null);
    }

    public ThreadClient(BluetoothDevice device, Handler handler){
        this.device = device;
        this.handler = handler;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            socket.connect();
            nome = socket.getRemoteDevice().getName();
            if(handler != null) {
                handler.obtainMessage(HANDLER_RECEIVED_TEXT, "Conectado ao dispositivo " + nome).sendToTarget();
            }
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            String string = null;
            while (true) {
                if(in != null)
                    string = in.readLine();
                if(handler != null)
                    handler.obtainMessage(HANDLER_RECEIVED_TEXT, nome + ": " + string).sendToTarget();
            }
        } catch (IOException e) {
            e.printStackTrace();
            //handler.obtainMessage(HANDLER_ERROR, e.getMessage()).sendToTarget();
        }
    }

    public void writeMessage(String msg){
        try {
            if (out != null) {
                out.writeUTF(msg);
            }
        } catch (IOException e) {
            handler.obtainMessage(HANDLER_ERROR, e.getMessage()).sendToTarget();
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if(in != null) {
                in.close();
                in = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(out != null) {
                out = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(handler != null) {
            handler.obtainMessage(HANDLER_RECEIVED_TEXT, "Conexão encerrada").sendToTarget();
        }
    }

}

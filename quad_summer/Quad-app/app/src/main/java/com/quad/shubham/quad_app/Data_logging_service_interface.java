package com.quad.shubham.quad_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by shubham on 14/6/15.
 */
public class Data_logging_service_interface {
    public static BluetoothDevice device;
    public static UUID uuid;
    public static BluetoothSocket socket;

    public static void start_data_logging(Context context,BluetoothDevice _device){
        device=_device;
        try {
            uuid=UUID.fromString("00001105-0000-1000-8000-00805F9B34FB");
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            Toast.makeText(context,"Connected", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    public static void stop_data_logging(){

    }
}

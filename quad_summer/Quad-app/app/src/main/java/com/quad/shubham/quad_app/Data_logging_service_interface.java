package com.quad.shubham.quad_app;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
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
    public static final String intent_filter_prefix="com.quad.shubham.quad_app";

    public static void start_data_logging(Context context,BluetoothDevice _device){
        device=_device;
        if(device!=null) {
            Intent intent = new Intent(context, Data_logging_service.class);
            intent.putExtra("bt_device",device);
            context.startService(intent);
        }else{
            Toast.makeText(context,"Null device",Toast.LENGTH_SHORT).show();
        }
    }

    public static void stop_data_logging(Context context){
        Intent intent=new Intent(context,Data_logging_service.class);
        context.stopService(intent);
    }
}

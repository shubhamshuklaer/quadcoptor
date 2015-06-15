package com.quad.shubham.quad_app;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by shubham on 14/6/15.
 */
public class Data_logging_service extends Service {
    Thread thread;
    public static final String intent_filter_prefix="com.quad.shubham.quad_app";

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bluetooth_sender_receiver_runnable r=new Bluetooth_sender_receiver_runnable(
                Data_logging_service.this,(BluetoothDevice)intent.getExtras().getParcelable("bt_device"));
        thread=new Thread(r);
        thread.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //No binds
        return null;
    }

    @Override
    public void onDestroy() {
        thread.interrupt();
    }
}

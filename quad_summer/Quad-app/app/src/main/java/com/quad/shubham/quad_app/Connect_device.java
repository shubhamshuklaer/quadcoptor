package com.quad.shubham.quad_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by shubham on 14/6/15.
 */
public class Connect_device extends Activity {
    ListView list;
    ArrayAdapter<String> arr_adapter;
    ArrayList<BluetoothDevice> scanned_devices;
    private final BroadcastReceiver action_found_reciever=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arr_adapter.add(device.getName()+"\n"+device.getAddress());
                scanned_devices.add(device);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list=new ListView(Connect_device.this);
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(action_found_reciever,filter);

        arr_adapter=new ArrayAdapter<String>(Connect_device.this,android.R.layout.simple_list_item_1,android.R.id.text1);
        scanned_devices= new ArrayList<BluetoothDevice>();

        list.setAdapter(arr_adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                BluetoothDevice device = scanned_devices.get(position);
//                Data_logging_service_interface.start_data_logging(Connect_device.this, device);
            }
        });
        setContentView(list);
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Imp to unregister receiver
        unregisterReceiver(action_found_reciever);
    }
}

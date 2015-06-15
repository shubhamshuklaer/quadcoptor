package com.quad.shubham.quad_app;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by shubham on 14/6/15.
 */
public class Connect_device extends Activity {
    ListView list;
    ArrayAdapter<String> arr_adapter;
    ArrayList<String> scanned_devices_addr;
    private final BroadcastReceiver action_found_reciever=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arr_adapter.add(device.getName()+"\n"+device.getAddress());
                scanned_devices_addr.add(device.getAddress());
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
        scanned_devices_addr = new ArrayList<String>();

        list.setAdapter(arr_adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(scanned_devices_addr.get(position));

                if(device!=null) {
//                    NotificationCompat.Builder m_builder=new NotificationCompat.Builder(Connect_device.this)
//                            .setSmallIcon(R.drawable.notification_template_icon_bg).setContentTitle("Data logger").setContentText("Starting");
//
//                    Intent result_intent=new Intent(Connect_device.this,Data_logging_service.class);
//
//                    // The stack builder object will contain an artificial back stack for the
//                    // started Activity.
//                    // This ensures that navigating backward from the Activity leads out of
//                    // your application to the Home screen.
//                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(Connect_device.this);
//                    // Adds the back stack for the Intent (but not the Intent itself)
//                    stackBuilder.addParentStack(Data_logging_service.class);
//                    // Adds the Intent that starts the Activity to the top of the stack
//                    stackBuilder.addNextIntent(result_intent);
//                    PendingIntent resultPendingIntent =
//                            stackBuilder.getPendingIntent(
//                                    0,
//                                    PendingIntent.FLAG_UPDATE_CURRENT
//                            );
//
//                    m_builder.setContentIntent(resultPendingIntent);
//
//                    Service.startForeground




                    Intent intent = new Intent(Connect_device.this, Data_logging_service.class);
                    intent.putExtra("bt_device",device);
                    startService(intent);
                }else{
                    Toast.makeText(Connect_device.this, "Null device", Toast.LENGTH_SHORT).show();
                }
            }
        });
        setContentView(list);
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    protected void onDestroy() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        //Imp to unregister receiver
        unregisterReceiver(action_found_reciever);
        super.onDestroy();
    }
}

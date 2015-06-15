package com.quad.shubham.quad_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by shubham on 14/6/15.
 */
public class Bluetooth extends Activity {
    ListView list;
    static final int ENABLE_REQUEST=1;
    String[] actions={"Enable Bluetooth","Disable Bluetooth","Start Data Logging","Stop Data Logging"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list=new ListView(Bluetooth.this);
        ArrayAdapter<String> arr_adapter=new ArrayAdapter<String>(Bluetooth.this,android.R.layout.simple_list_item_1,android.R.id.text1,actions);
        list.setAdapter(arr_adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                BluetoothAdapter b_adapter = BluetoothAdapter.getDefaultAdapter();
                if (b_adapter == null) {
                    Toast.makeText(Bluetooth.this, "Device does not have bluetooth capabilities", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (position) {
                    case 0://Enable Bluetooth
                        if (!b_adapter.isEnabled()) {
                            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(intent, ENABLE_REQUEST);
                        } else {
                            Toast.makeText(Bluetooth.this, "Already Enabled", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 1://Disable Bluetooth
                        if (b_adapter.isEnabled()) {
                            if (b_adapter.disable()) {
                                Toast.makeText(Bluetooth.this, "Bluetooth disable began", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Bluetooth.this, "Bluetooth disable error", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Bluetooth.this, "Already Disabled", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2://Connect device
                        if (b_adapter.isEnabled()) {
                            intent = new Intent(Bluetooth.this, Connect_device.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Bluetooth.this, "Enable bluetooth first", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3://Stop data logging
                        intent=new Intent(Bluetooth.this,Data_logging_service.class);
                        stopService(intent);
                        break;

                }
            }
        });



        setContentView(list);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case ENABLE_REQUEST:
                if(resultCode==Activity.RESULT_OK) {
                    Toast.makeText(Bluetooth.this, "Enabled", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(Bluetooth.this, "Not Enabled", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

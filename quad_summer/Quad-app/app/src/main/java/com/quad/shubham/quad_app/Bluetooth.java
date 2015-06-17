package com.quad.shubham.quad_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by shubham on 14/6/15.
 */
public class Bluetooth extends Activity {
    ListView list;
    static final int ENABLE_REQUEST=1;
    final int log_message_view_id=19;
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
                            if(Data_logging_service.running){
                                Toast.makeText(Bluetooth.this,"Service already running",Toast.LENGTH_SHORT).show();
                            }else {
                                intent = new Intent(Bluetooth.this, Connect_device.class);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(Bluetooth.this, "Enable bluetooth first", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3://Stop data logging
                        if(Data_logging_service.running) {
                            AlertDialog.Builder builder=new AlertDialog.Builder(Bluetooth.this);
                            EditText log_message_view=new EditText(Bluetooth.this);
                            log_message_view.setId(log_message_view_id);
                            log_message_view.setHint("Enter Log message");
                            builder.setView(log_message_view).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String log_message=((EditText)((AlertDialog)dialog).findViewById(log_message_view_id)).getText().toString();
                                    Db_helper db_helper_1=new Db_helper(Bluetooth.this);
                                    db_helper_1.update_data_log(log_message);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.create().show();
                            intent = new Intent(Bluetooth.this, Data_logging_service.class);
                            stopService(intent);
                        }else{
                            Toast.makeText(Bluetooth.this,"Not running",Toast.LENGTH_SHORT).show();
                        }
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

package com.quad.shubham.quad_app;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by shubham on 14/6/15.
 */
public class Data_logging_service extends IntentService{

    byte[] data;
    final int buffer_size=1024;
    int buffer_pos;
    BluetoothDevice device;
    UUID uuid;
    BluetoothSocket socket;
    InputStream i_stream;
    OutputStream o_stream;
    final byte new_line_ascii='\n';
    final byte carrige_return_ascii='\r';
    public static final String intent_filter_prefix="com.quad.shubham.quad_app";
    volatile boolean stop;


    private BroadcastReceiver data_receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    public Data_logging_service() {
        super("Data_logging_service");
        i_stream=null;
        o_stream=null;
        socket=null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(initialize(intent)){
            while(!stop){
                receive();
            }
            destroy();
        }
    }


    private boolean initialize(Intent intent){
        try {
//            uuid = UUID.fromString("00001105-0000-1000-8000-00805F9B34FB"); //works with android device
            uuid= UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Standard serialportservice uuid
            device=(BluetoothDevice)intent.getExtras().getParcelable("bt_device");
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            i_stream=socket.getInputStream();
            o_stream=socket.getOutputStream();
            data=new byte[buffer_size];
            buffer_pos=0;
            stop=false;
            return true;
        }catch (IOException e){
            Log.e("normal", e.getMessage());
            return false;
        }
    }

    private void destroy(){
        try {
            if(i_stream!=null)
                i_stream.close();
            if(o_stream!=null)
                o_stream.close();
            if(socket!=null)
                socket.close();
        }catch (IOException e){
            Log.e("normal", e.getMessage());
        }
    }


    private void receive() {
        byte read_byte;
        try {
            if( buffer_pos >= buffer_size) {
                stop=true;
                Log.e("normal","Buffer overflow");
            }else if (i_stream.available() >0) {
                int read_byte_int=i_stream.read();
                if(read_byte_int!=-1) {
                    read_byte = (byte) (read_byte_int);//reads 1 byte... since returns a int between 0-255 we can cast it directly
                    if (read_byte == carrige_return_ascii) {
                        String data_line = new String(data, 0, buffer_pos, "US-ASCII");
                        data_line = data_line.trim().replaceAll("\\s+", " ");//Will trim and convert all multiple spaces to single
                        if (data_line.charAt(data_line.length() - 1) == '\n') {
                            data_line = data_line.substring(0, data_line.length() - 1);//removing the last /n
                        }
                        String[] seperated = data_line.split(" ", 2);// Data line will be of format "prefix int int\n"
                        if (seperated.length == 2) { //Sometimes it is lenght 1 causes error
                            Intent intent = new Intent(Data_logging_service.intent_filter_prefix + ":" + seperated[0]);
                            intent.putExtra("data", seperated[1]);
                            LocalBroadcastManager.getInstance(Data_logging_service.this.getApplicationContext()).sendBroadcast(intent);
                        }
                        buffer_pos = 0;
                    } else {
                        data[buffer_pos] = read_byte;
                        buffer_pos++;
                    }
                }else {
                    Log.d("normal", "End of stream reached");
                    stop = true;
                }
            }
        }catch(IOException e){
            Log.e("normal",e.getMessage());
            stop=true;
        }
    }

    @Override
    public void onDestroy() {
        stop=true;
        //from https://github.com/android/platform_frameworks_base/blob/master/core/java/android/app/IntentService.java
        //super.onDestroy() only quits the looper i.e no more requests will be taken and also all pending
        //requests are cleared... but the current request will still run
        super.onDestroy();
    }
}
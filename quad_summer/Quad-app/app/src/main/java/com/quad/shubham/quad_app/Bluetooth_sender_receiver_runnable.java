package com.quad.shubham.quad_app;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
public class Bluetooth_sender_receiver_runnable implements Runnable{
    byte[] data;
    final int buffer_size=1024;
    int buffer_pos;
    BluetoothDevice device;
    UUID uuid;
    BluetoothSocket socket;
    Context context;
    InputStream i_stream;
    OutputStream o_stream;
    final byte new_line_ascii='\n';
    final byte carrige_return_ascii='\r';
    boolean stop;

    private BroadcastReceiver data_receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    public void run() {
        if(initialize()){
            while(!Thread.currentThread().interrupted() && !stop){
                receive();
            }
            destroy();
        }
    }

    private void receive() {
        byte read_byte;
        try {
            while(i_stream.available() >0 && buffer_pos < buffer_size) {
                read_byte =(byte) (i_stream.read());//reads 1 byte... since returns a int between 0-255 we can cast it directly
                if (read_byte == carrige_return_ascii) {
                    String data_line=new String(data,0,buffer_pos,"US-ASCII");
                    data_line=data_line.trim().replaceAll("\\s+", " ");//Will trim and convert all multiple spaces to single
                    if(data_line.charAt(data_line.length()-1)=='\n'){
                        data_line=data_line.substring(0,data_line.length()-1);//removing the last /n
                    }
                    String[] seperated=data_line.split(" ",2);// Data line will be of format "prefix int int\n"
                    if(seperated.length==2) { //Sometimes it is lenght 1 causes error
                        Intent intent = new Intent(Data_logging_service.intent_filter_prefix + ":" + seperated[0]);
                        intent.putExtra("data", seperated[1]);
                        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
                    }
                    buffer_pos=0;
                } else if (read_byte==-1){
                    Log.d("normal","End of stream reached");
                    stop=true;
                }else{
                    data[buffer_pos]=read_byte;
                    buffer_pos++;
                }
            }
        }catch(IOException e){
            Log.e("normal", e.getMessage());
            stop=true;
        }
    }

    //Constructor
    public Bluetooth_sender_receiver_runnable(Context _context,BluetoothDevice _device){
        context=_context;
        device=_device;
    }

    private boolean initialize(){
        try {
//            uuid = UUID.fromString("00001105-0000-1000-8000-00805F9B34FB"); //works with android device
            uuid=UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Standard serialportservice uuid
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
            i_stream.close();
            o_stream.close();
            socket.close();
        }catch (IOException e){
            Log.e("normal", e.getMessage());
        }
    }

}




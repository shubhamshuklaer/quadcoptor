package com.quad.shubham.quad_app;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
    public static final int START_NOTIFICATION_ID =19;
    public static final int STOP_NOTIFICATION_ID =21;
    public static final String send_intent_filter="com.quad.shubham.quad_app_send";
    public static boolean running;
    private Binder my_binder=null;
    volatile boolean stop;

    Sender_thread sender_thread;
    Handler sender_handler;

    public class My_Binder extends Binder{
        private Handler handler;

        public My_Binder(Handler _handler){
            this.handler=_handler;
        }

        public void send_data(String data){
            Message msg=Message.obtain();
            msg.obj=data;
            msg.setTarget(handler);
            msg.sendToTarget();
        }
    }

    public Data_logging_service() {
        super("Data_logging_service");
        i_stream=null;
        o_stream=null;
        socket=null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running=true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(initialize(intent)){
            while(!stop){
                receive();
            }
            destroy();
        }

        running=false;
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

            //  http://www.truiton.com/2014/10/android-foreground-service-example/
            // For foreground service

            Intent bluetooth_activity_intent=new Intent(this,Bluetooth.class);

            // http://stackoverflow.com/questions/13632480/android-build-a-notification-taskstackbuilder-addparentstack-not-working
            // http://developer.android.com/guide/topics/ui/notifiers/notifications.html
            // So that when we press back from the activity opened by notification click we go
            // to home activity. In manifest the android:parent parameter is for this only.
            // Also the android:launchMode=singleTop is so if the activity is already running it is not started twice
            TaskStackBuilder stack_builder=TaskStackBuilder.create(this);
            stack_builder.addParentStack(Bluetooth.class);
            stack_builder.addNextIntent(bluetooth_activity_intent);

            PendingIntent pending_intent=stack_builder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification=new NotificationCompat.Builder(this)
                    .setContentTitle("Data Logging service started")
                    .setContentText("Click to stop")
                    .setContentIntent(pending_intent)
                    .setSmallIcon(R.mipmap.ic_launcher).build();

            startForeground(Data_logging_service.START_NOTIFICATION_ID, notification);

            IntentFilter filter=new IntentFilter(Data_logging_service.send_intent_filter);
//            registerReceiver(data_receiver,filter,null,this.getHa)

            sender_thread=new Sender_thread("Sender_thread",o_stream);
            sender_thread.start();
            sender_handler=sender_thread.get_handler();
            my_binder=new My_Binder(sender_handler);
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
        sender_thread.quit();
        Notification service_stopped_notification=new NotificationCompat.Builder(this)
                .setContentTitle("Quad app")
                .setContentText("Data logging stopped")
                .setSmallIcon(R.mipmap.ic_launcher).build();

        NotificationManager notification_manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.cancel(STOP_NOTIFICATION_ID);
        notification_manager.notify(STOP_NOTIFICATION_ID,service_stopped_notification);
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
                        if (data_line.length()>0 && data_line.charAt(data_line.length() - 1) == '\n') {
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
        synchronized (this) {
            stop = true;
        };
        //from https://github.com/android/platform_frameworks_base/blob/master/core/java/android/app/IntentService.java
        //super.onDestroy() only quits the looper i.e no more requests will be taken and also all pending
        //requests are cleared... but the current request will still run
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return my_binder;
    }
}
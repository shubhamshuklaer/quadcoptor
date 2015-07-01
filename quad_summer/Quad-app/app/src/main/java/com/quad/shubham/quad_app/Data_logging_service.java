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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
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
    public static final String intent_filter_prefix="com.quad.shubham.quad_app:";
    public static final int START_NOTIFICATION_ID =19;
    public static final int STOP_NOTIFICATION_ID =21;
    public static final String send_intent_filter="com.quad.shubham.quad_app_send";
    private FileOutputStream receive_data_log_file_stream;
    private FileOutputStream send_data_log_file_stream;
    public static boolean running;
    volatile boolean stop;

    Sender_thread sender_thread;
    Handler sender_handler;

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
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

            Db_helper db_helper=new Db_helper(Data_logging_service.this);
            db_helper.insert_data_log();
            File receive_data_log_file=new File(getExternalFilesDir(null), Integer.toString(db_helper.get_num_rows(Db_helper.DATA_LOGS_TBL_NAME))+"_receive_log.txt");
            File send_data_log_file=new File(getExternalFilesDir(null), Integer.toString(db_helper.get_num_rows(Db_helper.DATA_LOGS_TBL_NAME))+"_send_log.txt");
            receive_data_log_file_stream =new FileOutputStream(receive_data_log_file);
            send_data_log_file_stream=new  FileOutputStream(send_data_log_file);
            sender_thread=new Sender_thread("Sender_thread",o_stream,send_data_log_file_stream);
            sender_thread.start();
            return true;
        }catch (IOException e){
            Log.e("normal", e.getMessage());
            return false;
        }
    }

    private void destroy(){
        sender_thread.quit();
        try {
            if(i_stream!=null)
                i_stream.close();
            if(o_stream!=null)
                o_stream.close();
            if(socket!=null)
                socket.close();

            receive_data_log_file_stream.write("##########".getBytes());
            Map<String,String> cur_tune_data=Data_store.get_all(Data_logging_service.this,Data_store.TUNER_DATA_FILE);
            for(Map.Entry<String,String> entry:cur_tune_data.entrySet()){
                receive_data_log_file_stream.write((entry.getKey() + " : " + entry.getValue() + "\n").getBytes());
            }
            receive_data_log_file_stream.close();
            send_data_log_file_stream.close();
        }catch (IOException e){
            Log.e("normal", e.getMessage());
        }
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
                    if (read_byte == new_line_ascii) {//println does "\r\n"
                        String data_line = new String(data, 0, buffer_pos, "UTF-8");//this will give us till \r
                        data_line = data_line.trim().replaceAll("\\s+", " ");//Will trim and convert all multiple spaces to single

                        //\S+ represent any string with no whitespace chars
                        if(data_line.matches("^\\S++\\s-?[0-9]+(\\.[0-9]+)?$")) {
                            //The trim function also removes the \n or \r characters from the ends in addition to spaces at ends
                            receive_data_log_file_stream.write((data_line + "\n").getBytes());
                            String[] seperated = data_line.split(" ", 2);// Data line will be of format "prefix int int\n"
                            Intent intent = new Intent(Data_logging_service.intent_filter_prefix + seperated[0]);
                            intent.putExtra("data", seperated[1]);
                            LocalBroadcastManager.getInstance(Data_logging_service.this.getApplicationContext()).sendBroadcast(intent);
                        }else{
                            receive_data_log_file_stream.write(("Wrong Format: "+data_line+"\n").getBytes());
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
        if(sender_thread==null)
            return null;
        else
            sender_handler=sender_thread.get_handler();
            return My_binder.new_instance(sender_handler);
    }
}
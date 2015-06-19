package com.quad.shubham.quad_app;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by shubham on 16/6/15.
 */
public class Sender_thread extends HandlerThread {
    Handler m_handler;
    OutputStream o_stream;

    public Sender_thread(String name,OutputStream _o_stream) {
        super(name,android.os.Process.THREAD_PRIORITY_FOREGROUND);
        o_stream=_o_stream;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        m_handler=new Handler(getLooper()){
            @Override
            public void handleMessage(Message msg) {
                String data=(String)msg.obj;
                try {
                    o_stream.write(data.getBytes("UTF-8"));
                }catch (IOException e){
                    Log.e("normal", e.getMessage());
                }
            }
        };
    }

    @Override
    public void run() {
        super.run();
    }

    public Handler get_handler(){
        return m_handler;
    }
}

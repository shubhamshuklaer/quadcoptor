package com.quad.shubham.quad_app;

import android.os.Binder;
import android.os.Handler;
import android.os.Message;

/**
 * Created by shubham on 16/6/15.
 */
public class My_binder extends Binder {
    private Handler handler;

    public My_binder(Handler _handler){
        this.handler=_handler;
    }

    public static My_binder new_instance(Handler _handler){
        if(_handler!=null)
            return new My_binder(_handler);
        else
            return null;
    }

    public void send_data(String data){
        Message msg=Message.obtain();
        msg.obj=data;
        msg.setTarget(handler);
        msg.sendToTarget();
    }
}

package com.quad.shubham.quad_app;

import android.content.Context;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

/**
 * Created by shubham on 14/6/15.
 */
public class Bluetooth_interface {
    public static BluetoothSPP bt=null;
    public static boolean enable_bluetooth(Context context){
        if(bt==null)
            bt=new BluetoothSPP(context.getApplicationContext());
        if(bt.isBluetoothAvailable()){

        }else{
            Toast.makeText(context,"Bluetooth not enabled",Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}

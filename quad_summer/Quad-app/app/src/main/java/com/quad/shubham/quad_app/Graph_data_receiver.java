package com.quad.shubham.quad_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by shubham on 15/6/15.
 */
public class Graph_data_receiver extends BroadcastReceiver {
    LineGraphSeries<DataPoint> series;
    public String prefix;
    Context context;

    public Graph_data_receiver(Context _context,LineGraphSeries<DataPoint> _series,String _prefix){
        series=_series;
        prefix=_prefix;
        context=_context;
        Log.e("normal","Created");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String data=intent.getStringExtra("data");
        String[] seperated=data.split(" ",2);
        DataPoint point=new DataPoint(Integer.parseInt(seperated[0]),Integer.parseInt(seperated[1]));
        series.appendData(point, true, 50);
        Log.e("normal", "err..." + data);
    }

    public void register_receiver(){
        IntentFilter intent_filter=new IntentFilter(Data_logging_service_interface.intent_filter_prefix+":"+prefix);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(this, intent_filter);
        Log.d("normal",prefix);
    }

    public void unregister_receiver(){
        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(this);
    }

}

package com.quad.shubham.quad_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


/**
 * Created by shubham on 15/6/15.
 */
public class Graph_data_receiver extends BroadcastReceiver {
    LineGraphSeries<DataPoint> series;
    public String prefix;
    Context context;
    GraphView graph_view;
    public final int view_port_size=50;

    public Graph_data_receiver(Context _context,GraphView _graph_view,LineGraphSeries<DataPoint> _series,String _prefix){
        series=_series;
        prefix=_prefix;
        context=_context;
        graph_view=_graph_view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String data=intent.getStringExtra("data");
        String[] seperated=data.split(" ",2);
        if(seperated.length==2) {
            try {
                DataPoint point = new DataPoint(Integer.parseInt(seperated[0]), Integer.parseInt(seperated[1]));
                series.appendData(point, false, this.view_port_size);
                graph_view.getViewport().setMinX(Integer.parseInt(seperated[0]) - this.view_port_size);
                graph_view.getViewport().setMaxX(Integer.parseInt(seperated[0]));
            }catch(Exception e){
                Toast.makeText(context.getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void register_receiver(){
        IntentFilter intent_filter=new IntentFilter(Data_logging_service.intent_filter_prefix+":"+prefix);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(this, intent_filter);
        Log.d("normal",prefix);
    }

    public void unregister_receiver(){
        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(this);
    }

}

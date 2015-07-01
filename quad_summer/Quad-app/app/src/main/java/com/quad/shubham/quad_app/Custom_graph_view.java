package com.quad.shubham.quad_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

/**
 * Created by shubham on 18/6/15.
 */
public class Custom_graph_view extends LinearLayout {
    GraphView graph_view;
    ArrayList<LineGraphSeries> series_list;
    Context context;
    LinearLayout check_box_layout,top_layout;
    TextView num_points_label;
    EditText num_points_text;
    ArrayList<String> prefix_list;
    ArrayList<String> cur_x_list;
    String graph_name,x_prefix;
    TextView graph_name_view;
    public int max_points=50;
    boolean registered=false;
    final long update_delay=500;//500ms
    boolean update_now=true;
    boolean first_time=true;
    int h_m=10,v_m=5;

    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data=intent.getStringExtra("data");
            String action=intent.getAction();
            if(action.length()>=Data_logging_service.intent_filter_prefix.length()
                    && action.substring(0,Data_logging_service.intent_filter_prefix.length())
                    .equals(Data_logging_service.intent_filter_prefix)) {

                String prefix=action.substring(Data_logging_service.intent_filter_prefix.length());
                if(prefix.equals(x_prefix)){

                    //This is for when we press reset on arduino while the bluetooth connection is still present
                    if(cur_x_list.size()>0 && Double.parseDouble(cur_x_list.get(cur_x_list.size()-1))>=Double.parseDouble(data)){
                        cur_x_list.clear();
                        for(LineGraphSeries temp_series:series_list){
                            temp_series.resetData(new DataPoint[0]);
                        }
                    }
                    if(cur_x_list.size()<max_points) {
                        cur_x_list.add(data);
                    }else{
                        if(cur_x_list.size()>0)
                            cur_x_list.remove(0);
                        cur_x_list.add(data);
                    }
                }else {
                    int index = prefix_list.indexOf(prefix);
                    if (index != -1 && cur_x_list.size()>0) {//wait till you get correct cur_x ignore all data till then
                        DataPoint point = new DataPoint(Double.parseDouble(cur_x_list.get(cur_x_list.size() - 1)), Double.parseDouble(data));
                        series_list.get(index).appendDataWithoutUpdate(point, max_points);
                        if (update_now) {
                            update_now = false;
                            Custom_graph_view.this.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        update_now = true;
                                        if(cur_x_list.size()>0) {//cur_x_list might have been empty when this was called(caused its caused delayed)
                                            graph_view.getViewport().setMinX(Double.parseDouble(cur_x_list.get(0)));
                                            graph_view.getViewport().setMaxX(Double.parseDouble(cur_x_list.get(cur_x_list.size() - 1)));
                                            graph_view.onDataChanged(!first_time, false);
                                        }
                                        first_time = false;
                                    } catch (Exception e) {
                                        Toast.makeText(Custom_graph_view.this.context.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, update_delay);
                        }
                    }
                }
            }
        }
    };

    public Custom_graph_view(Context _context, ArrayList<LineGraphSeries> _series_list, ArrayList<String> _prefix_list,String _graph_name,String _x_prefix) {
        super(_context);
        context=_context;
        series_list=_series_list;
        prefix_list=_prefix_list;
        graph_name=_graph_name;
        x_prefix=_x_prefix;
        cur_x_list=new ArrayList<String>();
        this.setOrientation(VERTICAL);

        graph_view=new GraphView(context);
        graph_view.getViewport().setScalable(true);
        graph_view.getViewport().setScrollable(true);
        graph_view.getViewport().setXAxisBoundsManual(true);
        graph_view.getLegendRenderer().setVisible(true);

        top_layout=new LinearLayout(context);

        graph_name_view=new TextView(context);
        graph_name_view.setText(graph_name);

        num_points_label =new TextView(context);
        num_points_label.setText("Num points");

        num_points_text =new EditText(context);
        num_points_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        max_points=try_parse_int(Data_store.get_attribute(context,
                Data_store.USER_SETTING_PREFIX + graph_name + ":max_points",
                Integer.toString(max_points)),max_points);

        num_points_text.setText(Integer.toString(max_points));

        num_points_text.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    max_points = try_parse_int(num_points_text.getText().toString(), max_points);
                    while (cur_x_list.size()>0 && cur_x_list.size()>max_points) {//just a precaution if max_points is -ve
                        cur_x_list.remove(0);
                    }
                    Data_store.set_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":max_points",
                            Integer.toString(max_points));
                }
            }
        });


        CheckBox y_range_auto_box=new CheckBox(context);

        y_range_auto_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    graph_view.getViewport().setYAxisBoundsManual(false);
                    graph_view.onDataChanged(false, false);
                    Data_store.set_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_range_auto", "true");
                } else {
                    graph_view.getViewport().setYAxisBoundsManual(true);
                    int min_y=Integer.parseInt(Data_store.get_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_min", "0"));
                    int max_y=Integer.parseInt(Data_store.get_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_max", "0"));
                    graph_view.getViewport().setMinY(min_y);
                    graph_view.getViewport().setMaxY(max_y);
                    graph_view.onDataChanged(false, false);
                    Data_store.set_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_range_auto", "false");
                }
            }
        });


        boolean checked=Boolean.parseBoolean(
                Data_store.get_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_range_auto", "true"));
        y_range_auto_box.setChecked(checked);
        graph_view.getViewport().setYAxisBoundsManual(!checked);
        if(!checked){
            int min_y=Integer.parseInt(Data_store.get_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_min", "0"));
            int max_y=Integer.parseInt(Data_store.get_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_max", "0"));
            graph_view.getViewport().setMinY(min_y);
            graph_view.getViewport().setMaxY(max_y);
        }
        graph_view.onDataChanged(false, false);
        y_range_auto_box.setText("Y range auto");


        TextView y_min_label=new TextView(context);
        y_min_label.setText("Y min");
        TextView y_max_label=new TextView(context);
        y_max_label.setText("Y max");

        EditText y_min_text=new EditText(context);
        String y_min_val=Data_store.get_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_min", "0");
        y_min_text.setText(y_min_val);
        graph_view.getViewport().setMinY(Integer.parseInt(y_min_val));
        y_min_text.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
        y_min_text.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int min_y = try_parse_int(((EditText) v).getText().toString(), 0);
                    graph_view.getViewport().setMinY(min_y);
                    graph_view.onDataChanged(false, false);
                    Data_store.set_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_min", Integer.toString(min_y));
                }
            }
        });

        EditText y_max_text=new EditText(context);
        String y_max_val=Data_store.get_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_max", "0");
        y_max_text.setText(y_max_val);
        graph_view.getViewport().setMaxY(Integer.parseInt(y_max_val));
        y_max_text.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
        y_max_text.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int max_y = try_parse_int(((EditText) v).getText().toString(), 0);
                    graph_view.getViewport().setMaxY(max_y);
                    graph_view.onDataChanged(false, false);
                    Data_store.set_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":y_max", Integer.toString(max_y));
                }
            }
        });

        LayoutParams top_layout_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        top_layout_params.setMargins(h_m, v_m, h_m, v_m);

        top_layout.addView(graph_name_view, top_layout_params);
        top_layout.addView(y_range_auto_box, top_layout_params);
        top_layout.addView(y_min_label, top_layout_params);
        top_layout.addView(y_min_text, top_layout_params);
        top_layout.addView(y_max_label, top_layout_params);
        top_layout.addView(y_max_text, top_layout_params);
        top_layout.addView(num_points_label, top_layout_params);
        top_layout.addView(num_points_text, top_layout_params);



        check_box_layout=new LinearLayout(context);

        for(int i=0;i<series_list.size();i++){
            CheckBox temp_check_box=new CheckBox(context);
            temp_check_box.setText(series_list.get(i).getTitle());
            temp_check_box.setId(i + 1);
            temp_check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int index=buttonView.getId() - 1;
                    LineGraphSeries series = series_list.get(index);
                    if (isChecked) {
                        Data_store.set_attribute(context,Data_store.USER_SETTING_PREFIX + graph_name+":"+series.getTitle(),"true");
                        if (graph_view.getSeries().indexOf(series) == -1) {
                            graph_view.addSeries(series);
                            register_receiver();
                        }
                    } else {
                        Data_store.set_attribute(context,Data_store.USER_SETTING_PREFIX + graph_name+":"+series.getTitle(),"false");
                        if (graph_view.getSeries().indexOf(series) != -1) {
                            graph_view.removeSeries(series);
                            register_receiver();
                        }
                    }
                }
            });

            //Do it after setOnCheckeListner so that it will add the series to graph too depending
            //on checked state
            temp_check_box.setChecked(Boolean.parseBoolean(Data_store
                    .get_attribute(context, Data_store.USER_SETTING_PREFIX + graph_name + ":" + series_list.get(i).getTitle(), "true")));

            check_box_layout.addView(temp_check_box);
        }

        this.addView(top_layout,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
        this.addView(graph_view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.FILL));
        this.addView(check_box_layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));

        this.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                register_receiver();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                unregister_receiver();
            }
        });
    }

    public void register_receiver(){
        unregister_receiver();
        IntentFilter filter=new IntentFilter();
        for(int i=0;i<check_box_layout.getChildCount();i++){
            if(((CheckBox)check_box_layout.getChildAt(i)).isChecked())
                filter.addAction(Data_logging_service.intent_filter_prefix+prefix_list.get(i));
        }
        filter.addAction(Data_logging_service.intent_filter_prefix+x_prefix);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,filter);
        registered=true;
    }

    public void unregister_receiver() {
        if(registered) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            registered = false;
        }
    }

    public int try_parse_int(String str,int default_val){
        try{
            return Integer.parseInt(str);
        }catch (NumberFormatException e){
            return default_val;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        register_receiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregister_receiver();
    }
}

package com.quad.shubham.quad_app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by shubham on 12/6/15.
 */
public class tuner_data_list_adapter extends BaseAdapter {
    TreeMap<String,String> tuner_data;
    int h_padding=20;
    int v_padding=5;

    public tuner_data_list_adapter(Map<String,String> _tuner_data){
        super();
        tuner_data=new TreeMap<String,String>(_tuner_data);//TreeMap sorts the data
    }
    @Override
    public int getCount() {
        return this.tuner_data.size();
    }

    @Override
    public Object getItem(int position) {
        String key=this.tuner_data.keySet().toArray()[position].toString();
        String value=this.tuner_data.get(key);
        String[] key_val={key,value};
        return key_val;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout=new LinearLayout(parent.getContext());
        TextView key=new TextView(parent.getContext());
        TextView value=new TextView(parent.getContext());
        layout.addView(key);
        layout.addView(value);
        layout.setPadding(h_padding,v_padding,h_padding,v_padding);
        return layout;
    }
}

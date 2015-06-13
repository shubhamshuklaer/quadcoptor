package com.quad.shubham.quad_app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by shubham on 12/6/15.
 */
public class Map_list_adapter extends BaseAdapter {
    TreeMap<String,String> data;
    int h_padding=5;
    int v_padding=5;

    public Map_list_adapter(Map<String, String> _tuner_data){
        super();
        data =new TreeMap<String,String>(_tuner_data);//TreeMap sorts the data
    }
    @Override
    public int getCount() {
        return this.data.size();
    }

    @Override
    public Object getItem(int position) {
        String key=this.data.keySet().toArray()[position].toString();
        String value=this.data.get(key);
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
        String[] key_val=(String[])getItem(position);
        key.setText(key_val[0]);
        value.setText(key_val[1]);

        layout.setPadding(h_padding, v_padding, h_padding, v_padding);
        LayoutParams params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.setMargins(h_padding, v_padding, h_padding, v_padding);

        layout.addView(key,params);
        layout.addView(value,params);

        return layout;
    }
}

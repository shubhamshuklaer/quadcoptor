package com.quad.shubham.quad_app;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

//import android.view.ViewGroup.LayoutParams;


/**
 * Created by shubham on 9/6/15.
 */
public class My_slider extends LinearLayout {
    public enum Direction{HORIZONTAL,VERTICAL};
    SeekBar seek_bar;
    EditText min_text, max_text;
    TextView min_label,max_label,cur_label,cur_text;
    protected LayoutParams seek_bar_params;
    TextWatcher min_max_text_watcher,cur_text_watcher;
    SeekBar.OnSeekBarChangeListener seek_bar_change_listner;
    String parameter_name;
    Context parent_context;

    public My_slider(Context context) {
        super(context);
    }

    public static My_slider new_instance(Context context,Direction dir,String _parameter_name){
        final My_slider new_slider=new My_slider(context);
        new_slider.parent_context=context;

        if(dir==Direction.HORIZONTAL) {
            new_slider.setOrientation(LinearLayout.HORIZONTAL);
            new_slider.seek_bar=new SeekBar(context);
        }else if(dir==Direction.VERTICAL) {
            new_slider.setOrientation(LinearLayout.VERTICAL);
            new_slider.seek_bar=new VerticalSeekBar(context);
        }

        new_slider.parameter_name=_parameter_name;

        new_slider.min_text =new EditText(context);
        new_slider.max_text =new EditText(context);
        new_slider.cur_text =new EditText(context);
        new_slider.min_label=new TextView(context);
        new_slider.max_label=new TextView(context);
        new_slider.cur_label=new TextView(context);

        new_slider.min_label.setText("min");
        new_slider.max_label.setText("max");
        new_slider.cur_label.setText("cur");


        int min=Integer.parseInt(Global_tune_data.get_attribute(new_slider.parent_context, new_slider.parameter_name + "^min"));
        int max=Integer.parseInt(Global_tune_data.get_attribute(new_slider.parent_context, new_slider.parameter_name + "^max"));
        int cur=Integer.parseInt(Global_tune_data.get_attribute(new_slider.parent_context, new_slider.parameter_name + "^cur"));

        if(max<min || cur>max || cur<min){
            if(max<min){
                max=min;
                new_slider.update_shared_pref(new_slider.parameter_name+"^max",Integer.toString(max));
            }
            if(cur>max){
                max=cur;
                new_slider.update_shared_pref(new_slider.parameter_name + "^max", Integer.toString(max));
            }
            if(cur<min){
                min=cur;
                new_slider.update_shared_pref(new_slider.parameter_name + "^min", Integer.toString(min));
            }
        }


        new_slider.min_text.setText(Integer.toString(min));
        new_slider.max_text.setText(Integer.toString(max));
        new_slider.cur_text.setText(Integer.toString(cur));
        new_slider.seek_bar.setMax(max-min);
        new_slider.seek_bar.setProgress(cur);

        new_slider.max_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        new_slider.min_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        new_slider.cur_text.setInputType(InputType.TYPE_CLASS_NUMBER);


        LinearLayout max_layout=new LinearLayout(context);
        LinearLayout min_layout=new LinearLayout(context);
        LinearLayout cur_layout=new LinearLayout(context);


        max_layout.addView(new_slider.max_label);
        max_layout.addView(new_slider.max_text);

        min_layout.addView(new_slider.min_label);
        min_layout.addView(new_slider.min_text);

        cur_layout.addView(new_slider.cur_label);
        cur_layout.addView(new_slider.cur_text);


        new_slider.seek_bar_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        new_slider.seek_bar_params.weight=1;
        new_slider.seek_bar.setLayoutParams(new_slider.seek_bar_params);

        new_slider.addView(max_layout);
        new_slider.addView(min_layout);
        new_slider.addView(new_slider.seek_bar);
        new_slider.addView(cur_layout);

        new_slider.seek_bar_change_listner=new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                new_slider.set_cur_text(Integer.toString(progress + My_slider.try_parse_int(new_slider.min_text.getText().toString())));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };



        new_slider.min_max_text_watcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int max_val,min_val,cur_val;
                max_val=My_slider.try_parse_int(new_slider.max_text.getText().toString());
                min_val=My_slider.try_parse_int(new_slider.min_text.getText().toString());
                cur_val=My_slider.try_parse_int(new_slider.cur_text.getText().toString());
                if(max_val<min_val){
                    new_slider.max_text.setText(Integer.toString(min_val));
                    max_val=min_val;
                }else if(cur_val<min_val){
                    new_slider.min_text.setText(Integer.toString(cur_val));
                    min_val=cur_val;
                }else if(cur_val>max_val){
                    new_slider.max_text.setText(Integer.toString(cur_val));
                    max_val=cur_val;
                }
                new_slider.seek_bar.setMax(max_val - min_val);
                new_slider.seek_bar.setProgress(cur_val - min_val);
                new_slider.update_shared_pref(new_slider.parameter_name + "^max", Integer.toString(max_val));
                new_slider.update_shared_pref(new_slider.parameter_name + "^min", Integer.toString(min_val));
            }
        };

        new_slider.cur_text_watcher= new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int max_val,min_val,cur_val;
                max_val=My_slider.try_parse_int(new_slider.max_text.getText().toString());
                min_val=My_slider.try_parse_int(new_slider.min_text.getText().toString());
                cur_val=My_slider.try_parse_int(new_slider.cur_text.getText().toString());
                if(cur_val<min_val){
                    new_slider.min_text.setText(Integer.toString(cur_val));
                    min_val=cur_val;
                    new_slider.update_shared_pref(new_slider.parameter_name + "^min", Integer.toString(min_val));
                    new_slider.seek_bar.setMax(max_val - min_val);
                }else if(cur_val>max_val){
                    new_slider.max_text.setText(Integer.toString(cur_val));
                    max_val=cur_val;
                    new_slider.update_shared_pref(new_slider.parameter_name + "^max", Integer.toString(max_val));
                    new_slider.seek_bar.setMax(max_val - min_val);
                }
                new_slider.seek_bar.setProgress(cur_val - min_val);
                new_slider.update_shared_pref(new_slider.parameter_name+"^cur",Integer.toString(cur_val));
//                Selection.setSelection(s,new_slider.cur_text.getText().length());
            }
        };

        new_slider.min_text.addTextChangedListener(new_slider.min_max_text_watcher);
        new_slider.max_text.addTextChangedListener(new_slider.min_max_text_watcher);
        new_slider.cur_text.addTextChangedListener(new_slider.cur_text_watcher);
        new_slider.seek_bar.setOnSeekBarChangeListener(new_slider.seek_bar_change_listner);

        return new_slider;
    }

    public static int try_parse_int(String str){
        try {
            return Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return 0;
        }
    }

    public void set_cur_text(String str){
        this.cur_text.removeTextChangedListener(this.cur_text_watcher);
        this.cur_text.setText(str);
        this.cur_text.addTextChangedListener(this.cur_text_watcher);
        this.update_shared_pref(this.parameter_name+"^cur",str);
    }

    public void update_shared_pref(String key,String value){
        Global_tune_data.set_attribute(this.parent_context, key, value);
    }

}

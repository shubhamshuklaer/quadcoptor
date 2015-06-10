package com.quad.shubham.quad_app;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.widget.Space;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.rongi.rotate_layout.layout.RotateLayout;

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

    public My_slider(Context context) {
        super(context);
    }

    public static My_slider new_instance(Context context,Direction dir,String min_str,String max_str){
        final My_slider new_slider=new My_slider(context);

        if(dir==Direction.HORIZONTAL) {
            new_slider.setOrientation(LinearLayout.HORIZONTAL);
            new_slider.seek_bar=new SeekBar(context);
        }else if(dir==Direction.VERTICAL) {
            new_slider.setOrientation(LinearLayout.VERTICAL);
            new_slider.seek_bar=new VerticalSeekBar(context);
        }

        new_slider.min_text =new EditText(context);
        new_slider.max_text =new EditText(context);
        new_slider.cur_text =new EditText(context);
        new_slider.min_label=new TextView(context);
        new_slider.max_label=new TextView(context);
        new_slider.cur_label=new TextView(context);

        new_slider.min_label.setText("min");
        new_slider.max_label.setText("max");
        new_slider.cur_label.setText("cur");
        new_slider.cur_text.setText("0");

        int min,max;
        min=My_slider.try_parse_int(min_str);
        max=My_slider.try_parse_int(max_str);
        if(max<min)
            max=min;

        new_slider.min_text.setText(Integer.toString(min));
        new_slider.max_text.setText(Integer.toString(max));
        new_slider.seek_bar.setMax(max-min);

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
                    new_slider.seek_bar.setMax(max_val - min_val);
                }else if(cur_val>max_val){
                    new_slider.max_text.setText(Integer.toString(cur_val));
                    max_val=cur_val;
                    new_slider.seek_bar.setMax(max_val - min_val);
                }
                new_slider.seek_bar.setProgress(cur_val - min_val);
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
    }

}

package com.quad.shubham.quad_app;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.widget.Space;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VerticalSeekBar;

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
    protected int rotation_angle;
    protected LayoutParams seek_bar_params;
    public My_slider(Context context) {
        super(context);
    }

    public static My_slider new_instance(Context context,Direction dir){
        My_slider new_slider=new My_slider(context);

        if(dir==Direction.HORIZONTAL) {
            new_slider.setOrientation(LinearLayout.HORIZONTAL);
            new_slider.seek_bar=new SeekBar(context);
        }else if(dir==Direction.VERTICAL) {
            new_slider.setOrientation(LinearLayout.VERTICAL);
            new_slider.seek_bar=new VerticalSeekBar(context);
        }

        new_slider.min_text =new EditText(context);
        new_slider.max_text =new EditText(context);
        new_slider.cur_text =new TextView(context);
        new_slider.min_label=new TextView(context);
        new_slider.max_label=new TextView(context);
        new_slider.cur_label=new TextView(context);

        new_slider.min_label.setText("min");
        new_slider.max_label.setText("max");
        new_slider.cur_label.setText("cur");
        new_slider.cur_text.setText("cur_val");

        new_slider.max_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        new_slider.min_text.setInputType(InputType.TYPE_CLASS_NUMBER);


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
        return new_slider;
    }
}

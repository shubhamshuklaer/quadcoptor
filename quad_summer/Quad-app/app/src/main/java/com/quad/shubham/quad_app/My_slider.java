package com.quad.shubham.quad_app;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

//import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.LayoutParams;

/**
 * Created by shubham on 9/6/15.
 */
public class My_slider extends LinearLayout {
    public enum Direction{HORIZONTAL,VERTICAL};
    SeekBar seek_bar;
    TextView min_val_text_view,max_val_text_view,current_val_text_view;
    protected int rotation_angle;
    public My_slider(Context context) {
        super(context);
        seek_bar=new SeekBar(context);
        min_val_text_view=new TextView(context);
        max_val_text_view=new TextView(context);
        current_val_text_view=new TextView(context);
        min_val_text_view.setText("min val");
        max_val_text_view.setText("max");
        current_val_text_view.setText("cur");
        rotation_angle=0;
        seek_bar.setRotation(rotation_angle);
        LayoutParams seek_bar_params=new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        seek_bar_params.gravity= Gravity.FILL;
        seek_bar.setLayoutParams(seek_bar_params);
        this.addView(max_val_text_view);
        this.addView(min_val_text_view);
        this.addView(seek_bar);
        this.addView(current_val_text_view);

    }



    public void set_direction(Direction dir){
        if(dir==Direction.HORIZONTAL) {
            this.setOrientation(LinearLayout.HORIZONTAL);
            rotation_angle=0;
        }else if(dir==Direction.VERTICAL) {
            this.setOrientation(LinearLayout.VERTICAL);
            rotation_angle=270;
        }
        seek_bar.setRotation(rotation_angle);
        this.invalidate();
    }


}

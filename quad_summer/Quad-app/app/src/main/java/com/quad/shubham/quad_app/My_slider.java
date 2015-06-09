package com.quad.shubham.quad_app;

import android.content.Context;
import android.graphics.Canvas;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

//import android.view.ViewGroup.LayoutParams;


/**
 * Created by shubham on 9/6/15.
 */
public class My_slider extends LinearLayout {
    public enum Direction{HORIZONTAL,VERTICAL};
    My_seek_bar seek_bar;
    EditText min_text, max_text;
    TextView min_label,max_label,cur_label,cur_text;
    protected int rotation_angle;
    protected LayoutParams seek_bar_params;
    public My_slider(Context context) {
        super(context);
        seek_bar=new My_seek_bar(context);
        min_text =new EditText(context);
        max_text =new EditText(context);
        cur_text =new TextView(context);
        min_label=new TextView(context);
        max_label=new TextView(context);
        cur_label=new TextView(context);

        min_label.setText("min");
        max_label.setText("max");
        cur_label.setText("cur");
        cur_text.setText("cur_val");

        max_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        min_text.setInputType(InputType.TYPE_CLASS_NUMBER);


        LinearLayout max_layout=new LinearLayout(context);
        LinearLayout min_layout=new LinearLayout(context);
        LinearLayout cur_layout=new LinearLayout(context);

        max_layout.addView(max_label);
        max_layout.addView(max_text);

        min_layout.addView(min_label);
        min_layout.addView(min_text);

        cur_layout.addView(cur_label);
        cur_layout.addView(cur_text);

        rotation_angle=0;
        seek_bar.setRotation(rotation_angle);
        seek_bar_params=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        seek_bar_params.weight=1;
        seek_bar.setLayoutParams(seek_bar_params);

        this.addView(max_layout);
        this.addView(min_layout);
        this.addView(seek_bar);
        this.addView(cur_layout);

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(rotation_angle==90 || rotation_angle==270)
            seek_bar.set_measured_dimension(400, seek_bar.getMeasuredWidth());

//            seek_bar.set_measured_dimension(seek_bar.getMeasuredHeight(), seek_bar.getMeasuredWidth());
    }
}

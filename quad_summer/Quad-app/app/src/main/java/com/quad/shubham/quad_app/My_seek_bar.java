package com.quad.shubham.quad_app;

import android.content.Context;
import android.support.v4.widget.Space;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

/**
 * Created by shubham on 9/6/15.
 */
public class My_seek_bar extends LinearLayout {
    SeekBar seek_bar;
    Space space1,space2;
    public My_seek_bar(Context context) {
        super(context);
        space1=new Space(context);
        seek_bar=new SeekBar(context);
        space2=new Space(context);

        this.setOrientation(LinearLayout.VERTICAL);

        LayoutParams space_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, Gravity.FILL);
        space_params.weight=1;
        space1.setLayoutParams(space_params);
        space2.setLayoutParams(space_params);

        this.addView(space1);
        this.addView(seek_bar);
        this.addView(space2);

    }

    public My_seek_bar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public My_seek_bar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void set_measured_dimension(int widthMeasureSpec, int heightMeasureSpec){
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}

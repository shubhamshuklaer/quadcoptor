package com.quad.shubham.quad_app;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by shubham on 9/6/15.
 */
public class My_seek_bar extends SeekBar {
    public My_seek_bar(Context context) {
        super(context);
    }

    public My_seek_bar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public My_seek_bar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void set_measured_dimension(int widthMeasureSpec, int heightMeasureSpec){
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }
}

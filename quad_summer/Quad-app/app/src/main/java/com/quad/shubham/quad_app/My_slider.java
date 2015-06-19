package com.quad.shubham.quad_app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
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
    TextView min_label,max_label,cur_label,cur_text,name_label;
    protected LayoutParams seek_bar_params;
    OnFocusChangeListener min_max_focus_change_listner, cur_text_focus_change_listner;
    SeekBar.OnSeekBarChangeListener seek_bar_change_listner;
    String parameter_name;
    Context parent_context;
    My_binder my_binder;
    String command;
    ServiceConnection conn;
    private final String replace_str="%";
    String value_to_send;
    final int margin=10;
    long send_interval=500;//in ms;
    boolean send_now=true;
    final int min_text_id=19,max_text_id=21;

    Runnable send_command_runnable =new Runnable() {
        @Override
        public void run() {
            if (my_binder != null) {
                my_binder.send_data(command.replace(replace_str, value_to_send));
            }
            update_tuner_data(parameter_name + "^cur", value_to_send);
            send_now=true;
        }
    };

    public My_slider(Context context) {
        super(context);
    }



    public static My_slider new_instance(final Context context,Direction dir,String _parameter_name,String _command){
        final My_slider new_slider=new My_slider(context);
        new_slider.command=_command;
        new_slider.parent_context=context;

        if(dir==Direction.HORIZONTAL) {
            new_slider.setOrientation(LinearLayout.HORIZONTAL);
            new_slider.seek_bar=new SeekBar(context);
        }else if(dir==Direction.VERTICAL) {
            new_slider.setOrientation(LinearLayout.VERTICAL);
            new_slider.seek_bar=new VerticalSeekBar(context);
        }

        new_slider.parameter_name=_parameter_name;

        new_slider.setClickable(true);
        new_slider.setFocusableInTouchMode(true);
        new_slider.setFocusable(true);

        new_slider.min_text =new EditText(context);
        new_slider.min_text.setId(new_slider.min_text_id);
        new_slider.max_text =new EditText(context);
        new_slider.max_text.setId(new_slider.max_text_id);
        new_slider.cur_text =new EditText(context);
        new_slider.min_label=new TextView(context);
        new_slider.max_label=new TextView(context);
        new_slider.cur_label=new TextView(context);
        new_slider.name_label=new TextView(context);

        new_slider.min_label.setText("min");
        new_slider.max_label.setText("max");
        new_slider.cur_label.setText("cur");
        new_slider.name_label.setText(new_slider.parameter_name);


        int min=Integer.parseInt(Data_store.get_attribute(new_slider.parent_context, new_slider.parameter_name + "^min", "0"));
        int max=Integer.parseInt(Data_store.get_attribute(new_slider.parent_context, new_slider.parameter_name + "^max", "0"));
        int cur=Integer.parseInt(Data_store.get_attribute(new_slider.parent_context, new_slider.parameter_name + "^cur", "0"));

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

//        new_slider.min_text.seton


        new_slider.min_max_focus_change_listner =new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    int max_val, min_val, cur_val;
                    max_val = My_slider.try_parse_int(new_slider.max_text.getText().toString());
                    min_val = My_slider.try_parse_int(new_slider.min_text.getText().toString());
                    cur_val = My_slider.try_parse_int(new_slider.cur_text.getText().toString());

                    if(v.getId()==new_slider.min_text_id){
                        if( min_val > cur_val){//even if its greater than max_val it will still be greater than cur_val
                            String default_val=Integer.toString(cur_val);
                            min_val=Integer.parseInt(Data_store.get_attribute(
                                    new_slider.parent_context,new_slider.parameter_name + "^min",default_val));
                            new_slider.min_text.setText(Integer.toString(min_val));
                        }

                        new_slider.update_tuner_data(new_slider.parameter_name + "^min", Integer.toString(min_val));
                    }else if(v.getId()==new_slider.max_text_id){
                        if(max_val<cur_val){//even if its lesser than min_val it will still be lesser than cur_val
                            String default_val=Integer.toString(cur_val);
                            max_val=Integer.parseInt(Data_store.get_attribute(
                                    new_slider.parent_context,new_slider.parameter_name + "^max",default_val));
                            new_slider.max_text.setText(Integer.toString(max_val));
                        }
                        new_slider.update_tuner_data(new_slider.parameter_name + "^max", Integer.toString(max_val));
                    }
                    new_slider.seek_bar.setMax(max_val - min_val);
                    new_slider.seek_bar.setProgress(cur_val - min_val);//do this even if only max_val changed cause max-min has changed

                }
            }
        };

        new_slider.cur_text_focus_change_listner = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    int max_val, min_val, cur_val;
                    max_val = My_slider.try_parse_int(new_slider.max_text.getText().toString());
                    min_val = My_slider.try_parse_int(new_slider.min_text.getText().toString());
                    cur_val = My_slider.try_parse_int(new_slider.cur_text.getText().toString());

                    if(cur_val < min_val || cur_val > max_val){//Since user is changing cur val so that should be corrected and not min or max val
                        String default_val;
                        if(cur_val<min_val) {
                            default_val = Integer.toString(min_val);
                        }else {
                            default_val = Integer.toString(max_val);
                        }

                        String cur_val_str =Data_store.get_attribute(
                                new_slider.parent_context,new_slider.parameter_name+"^cur",
                                default_val);//take the previous value of cur_val or if not exists take the default value

                        new_slider.cur_text.setText(cur_val_str);
                        cur_val=Integer.parseInt(cur_val_str);
                    }

                    new_slider.seek_bar.setProgress(cur_val - min_val);
                    new_slider.update_tuner_data(new_slider.parameter_name + "^cur", Integer.toString(cur_val));
                }
            }
        };


        new_slider.min_text.setText(Integer.toString(min));
        new_slider.max_text.setText(Integer.toString(max));
        new_slider.set_cur_text(Integer.toString(cur));
        new_slider.seek_bar.setMax(max - min);
        new_slider.seek_bar.setProgress(cur);

        new_slider.max_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        new_slider.min_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        new_slider.cur_text.setInputType(InputType.TYPE_CLASS_NUMBER);


        LinearLayout max_layout=new LinearLayout(context);
        LinearLayout min_layout=new LinearLayout(context);
        LinearLayout cur_layout=new LinearLayout(context);

        LayoutParams temp_layout_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        temp_layout_params.gravity=Gravity.CENTER;

        max_layout.addView(new_slider.max_label);
        max_layout.addView(new_slider.max_text);

        min_layout.addView(new_slider.min_label);
        min_layout.addView(new_slider.min_text);

        cur_layout.addView(new_slider.cur_label);
        cur_layout.addView(new_slider.cur_text);


        new_slider.seek_bar_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        new_slider.seek_bar_params.weight=1;
        new_slider.seek_bar_params.gravity= Gravity.CENTER;

        new_slider.addView(new_slider.name_label,temp_layout_params);
        new_slider.addView(max_layout,temp_layout_params);
        new_slider.addView(min_layout,temp_layout_params);
        new_slider.addView(new_slider.seek_bar, new_slider.seek_bar_params);
        new_slider.addView(cur_layout, temp_layout_params);

        LayoutParams full_layout_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        full_layout_params.leftMargin=new_slider.margin;
        full_layout_params.rightMargin=new_slider.margin;
        full_layout_params.topMargin=new_slider.margin;
        full_layout_params.bottomMargin=new_slider.margin;

        new_slider.setLayoutParams(full_layout_params);


        new_slider.min_text.setOnFocusChangeListener(new_slider.min_max_focus_change_listner);
        new_slider.max_text.setOnFocusChangeListener(new_slider.min_max_focus_change_listner);
        new_slider.cur_text.setOnFocusChangeListener(new_slider.cur_text_focus_change_listner);
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

    public void set_cur_text(String str) {
        this.cur_text.setText(str);
        value_to_send=str;
        if(send_now) {
            send_now=false;
            this.postDelayed(send_command_runnable, send_interval);
        }
    }

    public void update_tuner_data(String key, String value){
        Data_store.set_attribute(this.parent_context, key, value);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(conn==null){
            conn=new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    my_binder = (My_binder) service;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    my_binder = null;
                }
            };
        }
        this.parent_context.bindService(new Intent(this.parent_context, Data_logging_service.class),conn , Context.BIND_DEBUG_UNBIND);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.parent_context.unbindService(conn);
        conn=null;
    }
}

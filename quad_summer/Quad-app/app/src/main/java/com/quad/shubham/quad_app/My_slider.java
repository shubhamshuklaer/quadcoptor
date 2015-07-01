package com.quad.shubham.quad_app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

//import android.view.ViewGroup.LayoutParams;


/**
 * Created by shubham on 9/6/15.
 */
public class My_slider extends LinearLayout {
    public enum Direction{HORIZONTAL,VERTICAL};
    SeekBar seek_bar;
    EditText min_text, max_text, prec_text,cur_text;
    TextView min_label,max_label,cur_label,name_label, prec_label,act_text;
    protected LayoutParams seek_bar_params;
    OnFocusChangeListener min_max_focus_change_listner, cur_text_focus_change_listner,prec_text_focus_change_listner;
    SeekBar.OnSeekBarChangeListener seek_bar_change_listner;
    String parameter_name;
    Context parent_context;
    My_binder my_binder;
    String command;
    ServiceConnection conn;
    private final String replace_str="%";
    final int margin=10;
    long send_interval=500;//in ms;
    boolean send_now=true;
    final int min_text_id=19,max_text_id=21;
    final int max_prec=10;
    final int disconnected_color=Color.CYAN;
    final int default_color =Color.WHITE;
    final int act_val_mismatch_color =Color.RED;

    LinearLayout max_layout,min_layout,cur_layout,prec_layout,act_layout;
    final String garbage="Err";

    Runnable send_command_runnable =new Runnable() {
        @Override
        public void run() {
            String value_to_send=cur_text.getText().toString();
            if (my_binder != null) {
                String act_val=act_text.getText().toString();
                if(!act_val.equals(value_to_send))
                    My_slider.this.setBackgroundColor(act_val_mismatch_color);
                my_binder.send_data(command.replace(replace_str, value_to_send));
            }
            //delayed update cur sharedPreference also
            update_tuner_data(parameter_name + "^cur", value_to_send);
            send_now=true;
        }
    };

    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data=intent.getStringExtra("data");
            int prec_val= My_slider.try_parse_int(prec_text.getText().toString(), 0);
            act_text.setText(fix_decimal(Float.parseFloat(data),prec_val));
            if(act_text.getText().toString().equals(cur_text.getText().toString()))
                My_slider.this.setBackgroundColor(default_color);
            else
                My_slider.this.setBackgroundColor(act_val_mismatch_color);

        }
    };

    public My_slider(Context context) {
        super(context);
    }



    public static My_slider new_instance(final Context context,Direction dir,String _parameter_name,String _command){
        final My_slider new_slider=new My_slider(context);

        new_slider.command=_command;
        new_slider.parent_context=context;
        new_slider.parameter_name=_parameter_name;

        new_slider.initialize_components(dir);

        new_slider.set_component_vals();
        new_slider.setup_listners();
        new_slider.setup_layout();

        return new_slider;
    }

    public static int try_parse_int(String str,int default_val){
        try {
            return Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return default_val;
        }
    }

    public static float try_parse_float(String str,float default_val){
        try {
            return Float.parseFloat(str);
        } catch(NumberFormatException nfe) {
            return default_val;
        }
    }

    public void send_command() {
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
                    My_slider.this.setBackgroundColor(default_color);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    my_binder = null;
                    My_slider.this.setBackgroundColor(disconnected_color);
                }
            };
        }
        setBackgroundColor(disconnected_color);
        parent_context.bindService(new Intent(this.parent_context, Data_logging_service.class), conn, Context.BIND_DEBUG_UNBIND);

        IntentFilter filter=new IntentFilter(Data_logging_service.intent_filter_prefix+command);
        LocalBroadcastManager.getInstance(parent_context).registerReceiver(receiver, filter);

        send_command();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.parent_context.unbindService(conn);
        conn=null;
        LocalBroadcastManager.getInstance(parent_context).unregisterReceiver(receiver);
    }

    public void setup_listners(){
        seek_bar_change_listner=new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int prec_val=My_slider.try_parse_int(prec_text.getText().toString(), 0);
                int divider=(int)Math.pow(10,prec_val);

                cur_text.setText(fix_decimal(
                        (float) progress / divider + My_slider.try_parse_float(min_text.getText().toString(), 0)
                        , prec_val));

                send_command();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };


        min_max_focus_change_listner =new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    float max_val, min_val, cur_val;
                    int prec_val;
                    max_val = My_slider.try_parse_float(max_text.getText().toString(),0);
                    min_val = My_slider.try_parse_float(min_text.getText().toString(), 0);
                    cur_val = My_slider.try_parse_float(cur_text.getText().toString(), 0);
                    prec_val= My_slider.try_parse_int(prec_text.getText().toString(),0);

                    if(v.getId()==min_text_id){
                        min_val=Float.parseFloat(fix_decimal(min_val,prec_val));
                        min_text.setText(fix_decimal(min_val, prec_val));

                        if( min_val > cur_val){//even if its greater than max_val it will still be greater than cur_val
                            String default_val=fix_decimal(cur_val, prec_val);
                            min_val=Float.parseFloat(Data_store.get_attribute(
                                    parent_context, parameter_name + "^min", default_val));
                            min_text.setText(fix_decimal(min_val, prec_val));
                        }

                        update_tuner_data(parameter_name + "^min", fix_decimal(min_val, prec_val));
                    }else if(v.getId()==max_text_id){
                        max_val=Float.parseFloat(fix_decimal(max_val,prec_val));
                        max_text.setText(fix_decimal(max_val, prec_val));

                        if(max_val<cur_val){//even if its lesser than min_val it will still be lesser than cur_val
                            String default_val=fix_decimal(cur_val, prec_val);
                            max_val=Float.parseFloat(Data_store.get_attribute(
                                    parent_context, parameter_name + "^max", default_val));
                            max_text.setText(fix_decimal(max_val, prec_val));
                        }
                        update_tuner_data(parameter_name + "^max",fix_decimal(max_val, prec_val));
                    }

                    // no need to call the listner as it will again update cur_text
                    // Also seek_bar has discrete values so if it sets cur_text then its
                    // value will be approximate
                    // though this listner is not called again cause its onFocusChangeListner
                    seek_bar.setOnSeekBarChangeListener(null);
                    seek_bar.setMax((int) ((max_val - min_val) * Math.pow(10, prec_val)));
                    seek_bar.setProgress((int) ((cur_val - min_val) * Math.pow(10, prec_val)));//do this even if only max_val changed cause max-min has changed
                    seek_bar.setOnSeekBarChangeListener(seek_bar_change_listner);
                }
            }
        };

        cur_text_focus_change_listner = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    float max_val, min_val, cur_val;
                    int prec_val;
                    max_val = My_slider.try_parse_float(max_text.getText().toString(),0);
                    min_val = My_slider.try_parse_float(min_text.getText().toString(), 0);
                    cur_val = My_slider.try_parse_float(cur_text.getText().toString(), 0);
                    prec_val= My_slider.try_parse_int(prec_text.getText().toString(), 0);

                    cur_val=Float.parseFloat(fix_decimal(cur_val, prec_val));
                    cur_text.setText(fix_decimal(cur_val, prec_val));

                    if(cur_val < min_val || cur_val > max_val){//Since user is changing cur val so that should be corrected and not min or max val
                        String default_val;
                        if(cur_val<min_val) {
                            default_val = fix_decimal(min_val, prec_val);
                        }else {
                            default_val = fix_decimal(max_val, prec_val);
                        }

                        String cur_val_str =Data_store.get_attribute(
                                parent_context,parameter_name+"^cur",
                                default_val);//take the previous value of cur_val or if not exists take the default value
                        cur_text.setText(cur_val_str);
                        cur_val=Float.parseFloat(cur_val_str);
                    }

                    send_command();

                    // no need to call the listner as it will again update cur_text
                    // Also seek_bar has discrete values so if it sets cur_text then its
                    // value will be approximate
                    // though this listner is not called again cause its onFocusChangeListner
                    seek_bar.setOnSeekBarChangeListener(null);
                    seek_bar.setProgress((int) ((cur_val - min_val) * Math.pow(10, prec_val)));
                    seek_bar.setOnSeekBarChangeListener(seek_bar_change_listner);
                    update_tuner_data(parameter_name + "^cur", fix_decimal(cur_val, prec_val));
                }
            }
        };

        prec_text_focus_change_listner=new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    float max_val, min_val, cur_val;
                    int prec_val;
                    max_val = My_slider.try_parse_float(max_text.getText().toString(),0);
                    min_val = My_slider.try_parse_float(min_text.getText().toString(), 0);
                    cur_val = My_slider.try_parse_float(cur_text.getText().toString(), 0);
                    prec_val= My_slider.try_parse_int(prec_text.getText().toString(), 0);

                    if(prec_val>max_prec){
                        prec_val=max_prec;
                        prec_text.setText(Integer.toString(prec_val));
                        Toast.makeText(parent_context,"Max prec val is "+Integer.toString(max_prec),Toast.LENGTH_SHORT).show();
                    }

                    max_val=Float.parseFloat(fix_decimal(max_val, prec_val));
                    min_val=Float.parseFloat(fix_decimal(min_val, prec_val));
                    cur_val=Float.parseFloat(fix_decimal(cur_val, prec_val));


                    // no need to call the listner as it will again update cur_text
                    // Also seek_bar has discrete values so if it sets cur_text then its
                    // value will be approximate
                    // though this listner is not called again cause its onFocusChangeListner
                    seek_bar.setOnSeekBarChangeListener(null);
                    seek_bar.setMax((int) ((max_val - min_val) * Math.pow(10, prec_val)));
                    seek_bar.setProgress((int) ((cur_val - min_val) * Math.pow(10, prec_val)));
                    seek_bar.setOnSeekBarChangeListener(seek_bar_change_listner);

                    max_text.setText(fix_decimal(max_val, prec_val));
                    min_text.setText(fix_decimal(min_val, prec_val));
                    cur_text.setText(fix_decimal(cur_val, prec_val));
                    send_command();

                    update_tuner_data(parameter_name + "^prec", Integer.toString(prec_val));
                    update_tuner_data(parameter_name + "^max", fix_decimal(max_val, prec_val));
                    update_tuner_data(parameter_name + "^min", fix_decimal(min_val, prec_val));
                    update_tuner_data(parameter_name + "^cur", fix_decimal(cur_val, prec_val));
                }
            }
        };

        min_text.setOnFocusChangeListener(min_max_focus_change_listner);
        max_text.setOnFocusChangeListener(min_max_focus_change_listner);
        cur_text.setOnFocusChangeListener(cur_text_focus_change_listner);
        prec_text.setOnFocusChangeListener(prec_text_focus_change_listner);
        seek_bar.setOnSeekBarChangeListener(seek_bar_change_listner);
    }


    public void initialize_components(Direction dir){
        if(dir==Direction.HORIZONTAL) {
            setOrientation(LinearLayout.HORIZONTAL);
            seek_bar=new SeekBar(parent_context);
        }else if(dir==Direction.VERTICAL) {
            setOrientation(LinearLayout.VERTICAL);
            seek_bar=new VerticalSeekBar(parent_context);
        }

        min_text =new EditText(parent_context);
        min_text.setId(min_text_id);
        max_text =new EditText(parent_context);
        max_text.setId(max_text_id);
        cur_text =new EditText(parent_context);
        prec_text=new EditText(parent_context);
        act_text=new TextView(parent_context);
        min_label=new TextView(parent_context);
        max_label=new TextView(parent_context);
        cur_label=new TextView(parent_context);
        name_label=new TextView(parent_context);
        prec_label =new TextView(parent_context);

        max_layout=new LinearLayout(parent_context);
        min_layout=new LinearLayout(parent_context);
        cur_layout=new LinearLayout(parent_context);
        prec_layout=new LinearLayout(parent_context);
        act_layout=new LinearLayout(parent_context);


        max_text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        min_text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        cur_text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        prec_text.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    public void set_component_vals(){
        min_label.setText("min");
        max_label.setText("max");
        cur_label.setText("cur");
        name_label.setText(parameter_name);
        prec_label.setText("Prec");
        act_text.setText(garbage);


        float min=Float.parseFloat(Data_store.get_attribute(parent_context, parameter_name + "^min", "0"));
        float max=Float.parseFloat(Data_store.get_attribute(parent_context, parameter_name + "^max", "0"));
        float cur=Float.parseFloat(Data_store.get_attribute(parent_context, parameter_name + "^cur", "0"));
        int prec=Integer.parseInt(Data_store.get_attribute(parent_context, parameter_name + "^prec", "0"));

        min_text.setText(fix_decimal(min,prec));
        max_text.setText(fix_decimal(max, prec));
        cur_text.setText(fix_decimal(cur, prec));
        prec_text.setText(Integer.toString(prec));
        // no need to call the listner as it will again update cur_text
        // Also seek_bar has discrete values so if it sets cur_text then its
        // value will be approximate
        // though this listner is not called again cause its onFocusChangeListner
        seek_bar.setOnSeekBarChangeListener(null);
        seek_bar.setMax((int) ((max - min) * Math.pow(10, prec)));
        seek_bar.setProgress((int) ((cur - min) * Math.pow(10, prec)));
        //seek_bar_change_listner  also is null cause this func is called before listners are initialized
        //but just for security calling this
        seek_bar.setOnSeekBarChangeListener(seek_bar_change_listner);

        max_layout.addView(max_label);
        max_layout.addView(max_text);

        min_layout.addView(min_label);
        min_layout.addView(min_text);

        cur_layout.addView(cur_label);
        cur_layout.addView(cur_text);

        prec_layout.addView(prec_label);
        prec_layout.addView(prec_text);


        act_layout.addView(act_text);
    }

    public void setup_layout(){
        setClickable(true);
        setFocusableInTouchMode(true);
        setFocusable(true);

        LayoutParams temp_layout_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        temp_layout_params.gravity=Gravity.CENTER;

        seek_bar_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        seek_bar_params.weight=1;
        seek_bar_params.gravity= Gravity.CENTER;

        addView(name_label, temp_layout_params);
        addView(max_layout, temp_layout_params);
        addView(min_layout, temp_layout_params);
        addView(seek_bar, seek_bar_params);
        addView(cur_layout, temp_layout_params);
        addView(prec_layout, temp_layout_params);
        addView(act_layout,temp_layout_params);

        LayoutParams full_layout_params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        full_layout_params.leftMargin=margin;
        full_layout_params.rightMargin=margin;
        full_layout_params.topMargin=margin;
        full_layout_params.bottomMargin=margin;

        setLayoutParams(full_layout_params);
    }

    public String fix_decimal(float val,int decimal_place){
        NumberFormat formatter=NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(decimal_place);
        formatter.setMinimumFractionDigits(decimal_place);
        formatter.setGroupingUsed(false);
        return formatter.format(val);
    }

}


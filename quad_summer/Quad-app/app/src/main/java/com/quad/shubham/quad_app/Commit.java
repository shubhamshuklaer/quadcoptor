package com.quad.shubham.quad_app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import java.util.Map;

/**
 * Created by shubham on 12/6/15.
 */
public class Commit extends Activity {
    LinearLayout layout;
    Commit_widget commit_widget;
    ListView current_params;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout=new LinearLayout(Commit.this);
        commit_widget=new Commit_widget(Commit.this);
        commit_widget.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        current_params=new ListView(Commit.this);
        current_params.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.FILL));

        Map<String,String> tuner_data= Data_store.get_all(Commit.this, "tuner_data");

        current_params.setAdapter(new tuner_data_list_adapter(tuner_data));

        layout.addView(commit_widget);
        layout.addView(current_params);
        setContentView(layout);
    }
}

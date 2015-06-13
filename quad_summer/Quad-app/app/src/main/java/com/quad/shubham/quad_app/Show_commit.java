package com.quad.shubham.quad_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.TreeMap;

/**
 * Created by shubham on 13/6/15.
 */
public class Show_commit extends Activity {
    LinearLayout layout;
    Button btn;
    LayoutParams params;
    ListView commit_meta_list,commit_data_list;
    String _id,timestamp,commit_message;
    TextView meta_label,data_label;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        _id=intent.getStringExtra("_id");
        timestamp=intent.getStringExtra("timestamp");
        commit_message=intent.getStringExtra("commit_message");

        layout=new LinearLayout(Show_commit.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        params=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

        btn=new Button(Show_commit.this);
        btn.setText("Rollback");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data_store.load_version(Show_commit.this, Show_commit.this._id);
                Toast.makeText(Show_commit.this, "Rolled back", Toast.LENGTH_SHORT).show();
            }
        });

        commit_meta_list=new ListView(Show_commit.this);
        TreeMap<String,String> map=new TreeMap<String,String>();
        map.put("_id", _id);
        map.put("commit_message",commit_message);
        map.put("timestamp",timestamp);
        commit_meta_list.setAdapter(new Map_list_adapter(map));


        commit_data_list=new ListView(Show_commit.this);
        commit_data_list.setAdapter(new Map_list_adapter(Data_store.get_data_for_commit(Show_commit.this,_id)));


        meta_label=new TextView(Show_commit.this);
        meta_label.setText("Commit Metada");
        data_label=new TextView(Show_commit.this);
        data_label.setText("Commit data");

        layout.addView(btn, params);
        layout.addView(meta_label, params);
        layout.addView(commit_meta_list, params);
        layout.addView(data_label,params);
        layout.addView(commit_data_list, params);

        setContentView(layout);
    }
}

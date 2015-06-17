package com.quad.shubham.quad_app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

/**
 * Created by shubham on 5/6/15.
 */
public class Data_logs extends Activity {
    ListView l_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        l_view=new ListView(Data_logs.this);
        Db_helper db_helper=new Db_helper(Data_logs.this);
        l_view.setAdapter(new My_cursor_adapter(Data_logs.this,db_helper.get_data_logs_list(),new String[] {"_id","log_message","timestamp"}));
        setContentView(l_view);
    }
}

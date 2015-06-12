package com.quad.shubham.quad_app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;

//import android.view.ViewGroup.LayoutParams;

/**
 * Created by shubham on 5/6/15.
 */
public class Show_history extends Activity {
    Spinner select_branch;
    LinearLayout layout;
    ListView commits_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout=new LinearLayout(Show_history.this);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

        Db_helper db_helper=new Db_helper(Show_history.this);

        select_branch=new Spinner(Show_history.this);
        Branch_list_cursor_adapter branch_adapter=new Branch_list_cursor_adapter(Show_history.this,db_helper.get_all_branches());
        select_branch.setAdapter(branch_adapter);
        select_branch.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        layout.addView(select_branch);


        commits_list =new ListView(Show_history.this);
        commits_list.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.FILL));
        layout.addView(commits_list);

        setContentView(layout);
    }
}

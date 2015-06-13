package com.quad.shubham.quad_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
        layout.setOrientation(LinearLayout.VERTICAL);

        Db_helper db_helper=new Db_helper(Show_history.this);

        select_branch=new Spinner(Show_history.this);
        Text_list_cursor_adapter branch_adapter=new Text_list_cursor_adapter(Show_history.this,db_helper.get_all_branches());
        select_branch.setAdapter(branch_adapter);
        select_branch.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        String branch_name=Data_store.get_attribute(Show_history.this, Data_store.CUR_BRANCH_SETTING, Data_store.CUR_BRANCH_SETTING_DEFAULT);
        select_branch.setSelection(((Text_list_cursor_adapter) (select_branch.getAdapter())).getPosition(branch_name));




        commits_list =new ListView(Show_history.this);
        commits_list.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.FILL));

        commits_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout layout=(LinearLayout)view;

                String _id=((TextView)layout.findViewById(Commit_list_cursor_adapter._id_view_id)).getText().toString();
                String timestamp=((TextView)layout.findViewById(Commit_list_cursor_adapter.time_stamp_view_id)).getText().toString();
                String commit_message=((TextView)layout.findViewById(Commit_list_cursor_adapter.commit_message_view_id)).getText().toString();

                Intent intent=new Intent(Show_history.this,Show_commit.class);
                intent.putExtra("_id", _id);
                intent.putExtra("timestamp",timestamp);
                intent.putExtra("commit_message",commit_message);

                startActivity(intent);
            }
        });

        set_adapter(new Commit_list_cursor_adapter(Show_history.this, db_helper.get_commits_for_branch(branch_name)));

        layout.addView(select_branch);
        layout.addView(commits_list);

        select_branch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView t_v = (TextView) view;
                String branch_name = t_v.getText().toString();
                Db_helper db_helper = new Db_helper(Show_history.this);
                set_adapter(new Commit_list_cursor_adapter(Show_history.this, db_helper.get_commits_for_branch(branch_name)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                commits_list.setAdapter(null);
            }
        });


        setContentView(layout);
    }

    public void set_adapter(Commit_list_cursor_adapter adapter){
        this.commits_list.setAdapter(adapter);
    }
}

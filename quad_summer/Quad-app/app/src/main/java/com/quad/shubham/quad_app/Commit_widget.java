package com.quad.shubham.quad_app;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by shubham on 12/6/15.
 */
public class Commit_widget extends LinearLayout{
    int message_num_lines=5;
    Context context;
    EditText commit_message_area;
    Button commit_btn;
    EditText branch_name_text_area;
    int branch_text_view_id=19;
    public Commit_widget(Context _context){
        super(_context);
        context=_context.getApplicationContext();
        final Db_helper db_helper=new Db_helper(context);
        int num_commits=db_helper.get_num_commits();

        int parent_id=Integer.parseInt(Data_store.get_attribute(context, Data_store.PARENT_ID_SETTING, "0"));

        this.setOrientation(LinearLayout.VERTICAL);

        commit_message_area=new EditText(context);
        commit_message_area.setHint("Type commit message");
        commit_message_area.setSingleLine(false);
        commit_message_area.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        commit_message_area.setMinLines(message_num_lines);
        commit_message_area.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        commit_message_area.setTextColor(Color.BLACK);


        commit_btn=new Button(context);
        commit_btn.setText("Commit");
        commit_btn.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        commit_btn.setTextColor(Color.BLACK);


        if(parent_id==num_commits){
            this.commit_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String commit_message=commit_message_area.getText().toString();
                    if(commit_message.equals("")){
                        Toast.makeText(context,"Please write some message",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Data_store.commit(context, commit_message);
                    Toast.makeText(context,"Commited",Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            branch_name_text_area=new EditText(context);
            branch_name_text_area.setHint("Type branch name");
            branch_name_text_area.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            branch_name_text_area.setTextColor(Color.BLACK);
            this.addView(branch_name_text_area);

            this.commit_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String commit_message=commit_message_area.getText().toString().trim();
                    String branch_name=branch_name_text_area.getText().toString().trim();
                    if(commit_message.equals("")){
                        Toast.makeText(context,"Please write some put_commit message",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(branch_name.equals("")){
                        Toast.makeText(context,"Please write some branch name",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Data_store.set_attribute(context, Data_store.CUR_BRANCH_SETTING, branch_name);
                    Data_store.create_branch(context, branch_name, Integer.parseInt(Data_store.get_attribute(context, Data_store.PARENT_ID_SETTING, "0")));
                    Data_store.commit(context, commit_message);
                    Toast.makeText(context,"Commited",Toast.LENGTH_SHORT).show();
                }
            });
        }

        this.addView(commit_message_area);
        this.addView(commit_btn);
    }
}

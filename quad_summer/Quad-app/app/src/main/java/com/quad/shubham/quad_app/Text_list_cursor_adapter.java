package com.quad.shubham.quad_app;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by shubham on 12/6/15.
 */
public class Text_list_cursor_adapter extends CursorAdapter {
    public Text_list_cursor_adapter(Context context, Cursor cursor){
        super(context,cursor,0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        TextView t_view=new TextView(context);
        LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT,30);
        t_view.setLayoutParams(params);
        return t_view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView t_view=(TextView)view;
        t_view.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
    }

    public int getPosition(String branch_name){
        Cursor cur=this.getCursor();
        for(int i=0;i<this.getCount();i++){
            cur.moveToPosition(i);
            String temp=cur.getString(cur.getColumnIndexOrThrow("name"));
            if(temp.equals(branch_name))
                return i;
        }
        return -1;
    }
}

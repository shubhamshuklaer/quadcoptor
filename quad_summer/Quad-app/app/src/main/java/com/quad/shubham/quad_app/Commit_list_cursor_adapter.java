package com.quad.shubham.quad_app;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

/**
 * Created by shubham on 12/6/15.
 */
public class Commit_list_cursor_adapter extends CursorAdapter {
    public static int time_stamp_view_id =19;
    public static int commit_message_view_id =21;
    public static int _id_view_id =39;
    protected int m_h=30;
    protected int m_v=5;
    public Commit_list_cursor_adapter(Context context,Cursor cursor){
        super(context,cursor,0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LinearLayout layout=new LinearLayout(context);
        TextView timestamp=new TextView(context);
        timestamp.setId(time_stamp_view_id);
        TextView message=new TextView(context);
        message.setId(commit_message_view_id);
        TextView _id_view=new TextView(context);
        _id_view.setId(_id_view_id);
        LayoutParams params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.setMargins(m_h,m_v,m_h,m_v);
        layout.setPadding(m_h, m_v, m_h, m_v);
        layout.addView(_id_view,params);
        layout.addView(message, params);
        layout.addView(timestamp,params);
        return layout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView _id=(TextView)view.findViewById(_id_view_id);
        TextView timestamp=(TextView)view.findViewById(time_stamp_view_id);
        TextView message=(TextView)view.findViewById(commit_message_view_id);
        timestamp.setText(cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
        message.setText(cursor.getString(cursor.getColumnIndexOrThrow("commit_message")));
        _id.setText(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
    }
}

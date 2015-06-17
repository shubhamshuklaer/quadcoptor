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
public class My_cursor_adapter extends CursorAdapter {
    public static int time_stamp_view_id =19;
    public static int commit_message_view_id =21;
    public static int _id_view_id =39;
    private String[] argument_list;
    protected int m_h=30;
    protected int m_v=5;
    public My_cursor_adapter(Context context, Cursor cursor, String[] _argument_list){
        super(context,cursor,0);
        argument_list=_argument_list;
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        LayoutParams params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.setMargins(m_h, m_v, m_h, m_v);
        layout.setPadding(m_h, m_v, m_h, m_v);

        for(int i=0;i<argument_list.length;i++){
            TextView temp_view=new TextView(context);
            temp_view.setId(i+1);//id >0
            layout.addView(temp_view,params);
        }
        return layout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        for(int i=0;i<argument_list.length;i++){
            TextView temp_view=(TextView)view.findViewById(i+1);
            temp_view.setText(cursor.getString(cursor.getColumnIndexOrThrow(argument_list[i])));
        }
    }

    public String get_text(View view,int arg_list_pos){
        return ((TextView)view.findViewById(arg_list_pos+1)).getText().toString();
    }
}

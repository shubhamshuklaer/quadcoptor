package com.quad.shubham.quad_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Created by shubham on 10/6/15.
 */
public class global_tune_data {
    public static String get_attribute(Context context,String attr_name){
        SharedPreferences shared_pref= context.getSharedPreferences("tuner_data",Context.MODE_PRIVATE);
        return shared_pref.getString(attr_name,"0");
    }

    public static void set_attribute(Context context,String attr_name,String val){
        SharedPreferences shared_pref= context.getSharedPreferences("tuner_data",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared_pref.edit();
        editor.putString(attr_name,val);
        editor.commit();
    }

    public static void commit(Context context,String commit_message, Timestamp time_stamp){
        SharedPreferences shared_pref= context.getSharedPreferences("tuner_data",Context.MODE_PRIVATE);
        Map<String,String> tune_data=(Map<String,String>)shared_pref.getAll();
//        SQLiteDatabase my_database = SQLiteDatabase.openOrCreateDatabase("your database name", , null);
    }
}

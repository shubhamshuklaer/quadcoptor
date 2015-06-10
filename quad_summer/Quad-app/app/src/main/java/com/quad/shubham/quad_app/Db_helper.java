package com.quad.shubham.quad_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shubham on 10/6/15.
 */
public class Db_helper extends SQLiteOpenHelper {

    public static final String DB_NAME="tuner_database";
    public static final String TABLE_NAME="tuner_data";

    public Db_helper(Context context){
        super(context,DB_NAME,null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+" (id integer primary key, parent_id integer, timestamp timestamp,commit_message text)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public void commit(){

    }
}

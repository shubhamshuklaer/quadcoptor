package com.quad.shubham.quad_app;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shubham on 10/6/15.
 */
public class Db_helper extends SQLiteOpenHelper {

    public static final String DB_NAME="tuner_database";
    public static final String COMMIT_TBL_NAME ="tuner_data";
    public static final String BRANCHES_TBL_NAME ="branches";
    public static final String MASTER_BRANCH_NAME="master";
    public static final int VERSION=3;
    Context context;

    public Db_helper(Context _context){
        super(_context,DB_NAME,null,VERSION);
        this.context=_context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + COMMIT_TBL_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, parent_id INTEGER, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,commit_message TEXT,branch_name TEXT)");
        db.execSQL("CREATE TABLE " + BRANCHES_TBL_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, parent_branch TEXT DEFAULT null )");
        db.execSQL("INSERT INTO " + BRANCHES_TBL_NAME + "(name,parent_branch) VALUES ('"+MASTER_BRANCH_NAME+"','null')");
        SharedPreferences shared_pref=this.context.getApplicationContext().getSharedPreferences("history_meta",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=shared_pref.edit();
        editor.putString("cur_branch", MASTER_BRANCH_NAME);
        editor.commit();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ COMMIT_TBL_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BRANCHES_TBL_NAME);
        onCreate(db);
    }

    public void commit(Context context, String commit_message){
        context=context.getApplicationContext();
        SharedPreferences shared_pref=context.getSharedPreferences("history_meta", Context.MODE_PRIVATE);
        int parent_id=Integer.parseInt(shared_pref.getString("parent_id", "0"));

        int id=get_num_rows(COMMIT_TBL_NAME)+1;
        SharedPreferences.Editor editor=shared_pref.edit();
        editor.putString("parent_id",Integer.toString(id));
        editor.commit();


        ContentValues content_values=new ContentValues();
        content_values.put("commit_message", commit_message);
        content_values.put("parent_id", parent_id);
        content_values.put("branch_name", shared_pref.getString("cur_branch", "master"));

        SQLiteDatabase db= this.getWritableDatabase();
        db.insert(COMMIT_TBL_NAME, null, content_values);

    }

    public Cursor get_commits_for_branch(String name){
        SQLiteDatabase db=this.getReadableDatabase();
        return  db.rawQuery("SELECT * FROM "+COMMIT_TBL_NAME+" WHERE branch_name = ?",new String[] {name});
    }

    public Cursor get_all_branches(){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM "+ BRANCHES_TBL_NAME, null);
    }

    public int get_num_rows(String table_name){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db,table_name);
    }
}

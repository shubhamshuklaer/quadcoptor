package com.quad.shubham.quad_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shubham on 10/6/15.
 */
public class Db_helper extends SQLiteOpenHelper {

    public static final String DB_NAME="tuner_database";
    public static final String COMMIT_TBL_NAME ="data";
    public static final String BRANCHES_TBL_NAME ="branches";
    public static final String DATA_LOGS_TBL_NAME="data_logs";
    public static final String MASTER_BRANCH_NAME="master";
    public static final int VERSION=1;
    Context context;

    public Db_helper(Context _context){
        super(_context,DB_NAME,null,VERSION);
        this.context=_context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + COMMIT_TBL_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, parent_id INTEGER, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,commit_message TEXT,branch_name TEXT)");
        db.execSQL("CREATE TABLE " + BRANCHES_TBL_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, parent_commit_id INT DEFAULT -1 )");
        db.execSQL("CREATE TABLE "+DATA_LOGS_TBL_NAME+"(_id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, log_message TEXT DEFAULT '')");
        db.execSQL("INSERT INTO " + BRANCHES_TBL_NAME + "(name,parent_commit_id) VALUES ('" + MASTER_BRANCH_NAME + "','-1')");

        Data_store.set_attribute(this.context, Data_store.CUR_BRANCH_SETTING, MASTER_BRANCH_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ COMMIT_TBL_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BRANCHES_TBL_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DATA_LOGS_TBL_NAME);
        onCreate(db);
    }

    public void put_commit(Context context, String commit_message){
        int parent_id=Integer.parseInt(Data_store.get_attribute(context, Data_store.PARENT_ID_SETTING, "0"));
        int id=get_num_rows(COMMIT_TBL_NAME)+1;
        Data_store.set_attribute(context, Data_store.PARENT_ID_SETTING, Integer.toString(id));

        ContentValues content_values=new ContentValues();
        content_values.put("commit_message", commit_message);
        content_values.put("parent_id", parent_id);
        content_values.put("branch_name", Data_store.get_attribute(context, Data_store.CUR_BRANCH_SETTING, "0"));

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

    public int get_num_commits(){
        return this.get_num_rows(COMMIT_TBL_NAME);
    }

    public void create_branch(String branch_name,int parent_commit_id){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues content_values=new ContentValues();
        content_values.put("name",branch_name);
        content_values.put("parent_commit_id", parent_commit_id);

        db.insert(BRANCHES_TBL_NAME,null,content_values);
    }

    public String get_branch_name(String commit_id){
//        DatabaseUtils.
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cur=db.rawQuery("SELECT * FROM "+COMMIT_TBL_NAME+" WHERE _id=?",new String[]{commit_id});
        cur.moveToFirst();
        return cur.getString(cur.getColumnIndexOrThrow("branch_name"));
    }

    public void insert_data_log(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("INSERT INTO " + DATA_LOGS_TBL_NAME + " DEFAULT VALUES");
    }

    public Cursor get_data_logs_list(){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM "+ DATA_LOGS_TBL_NAME, null);
    }

    public void update_data_log(String log_message){
        int _id=this.get_num_rows(DATA_LOGS_TBL_NAME);
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues content_values=new ContentValues();
        content_values.put("log_message",log_message);
        db.update(DATA_LOGS_TBL_NAME,content_values,"_id=?",new String[] {Integer.toString(_id)});
    }
}

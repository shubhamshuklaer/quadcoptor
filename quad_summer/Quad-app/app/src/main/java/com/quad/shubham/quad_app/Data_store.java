package com.quad.shubham.quad_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Created by shubham on 10/6/15.
 */
public class Data_store {
    public static String TUNER_DATA_FILE="data";
    public static String APPLICATION_DATA_FILE="application_data";
    public static String HISTORY_META_FILE="history_meta";
    public static String CUR_BRANCH_SETTING="cur_branch";
    public static String PARENT_ID_SETTING="parent_id";
    public static String CONFIG_FILE_PATH_SETTING="config_file_path";
    public static String CUR_BRANCH_SETTING_DEFAULT="master";
    public static String USER_SETTING_PREFIX="user_setting_";
    public static String USER_SETTING_FILE="user_meta";

    public static String get_attribute(Context context,String attr_name,String default_val){
        context=context.getApplicationContext();
        SharedPreferences shared_pref= context.getSharedPreferences(get_file_name(attr_name),Context.MODE_PRIVATE);
        return shared_pref.getString(attr_name,default_val);
    }

    public static void set_attribute(Context context,String attr_name,String val){
        context=context.getApplicationContext();
        SharedPreferences shared_pref= context.getSharedPreferences(get_file_name(attr_name),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared_pref.edit();
        editor.putString(attr_name,val);
        editor.commit();
    }

    protected static String get_file_name(String attr_name){
        if(attr_name==PARENT_ID_SETTING)
            return HISTORY_META_FILE;
        else if(attr_name==CUR_BRANCH_SETTING)
            return HISTORY_META_FILE;
        else if(attr_name==CONFIG_FILE_PATH_SETTING)
            return APPLICATION_DATA_FILE;
        else if(attr_name.length()>=USER_SETTING_PREFIX.length() && attr_name.substring(0,USER_SETTING_PREFIX.length()).equals(USER_SETTING_PREFIX))
            return USER_SETTING_FILE;
        else
            return TUNER_DATA_FILE;
    }

    public static Map<String,String> get_all(Context context,String file_name){
        context=context.getApplicationContext();
        SharedPreferences shared_pref=context.getSharedPreferences(file_name,Context.MODE_PRIVATE);
        return (Map<String,String>)shared_pref.getAll();
    }

    public static void commit(Context context,String commit_message){
        context=context.getApplicationContext();
        Map<String,String> tune_data=Data_store.get_all(context, TUNER_DATA_FILE);
        Db_helper db_helper=new Db_helper(context);
        db_helper.put_commit(context, commit_message);
        int _id=db_helper.get_num_rows(db_helper.COMMIT_TBL_NAME);
        try {
            File output_file=new File(context.getExternalFilesDir(null),Integer.toString(_id)+"_data.txt");
            FileOutputStream fos = new FileOutputStream(output_file);
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(tune_data);
            oos.close();
            fos.close();
        }catch (FileNotFoundException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (IOException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public static void load_version(Context context,String _id){
        context= context.getApplicationContext();

        try {
            File input_file=new File(context.getExternalFilesDir(null),_id+"_data.txt");
            FileInputStream fis = new FileInputStream(input_file);
            ObjectInputStream ois= new ObjectInputStream(fis);
            Map<String,String> tune_data=(Map<String,String>) ois.readObject();
            ois.close();
            fis.close();

            Data_store.set_attribute(context, PARENT_ID_SETTING, _id);
            Db_helper db_helper=new Db_helper(context);
            Data_store.set_attribute(context, CUR_BRANCH_SETTING, db_helper.get_branch_name(_id));


            for(Map.Entry<String,String> entry: tune_data.entrySet())
                Data_store.set_attribute(context,entry.getKey(),entry.getValue());

        }catch (FileNotFoundException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (IOException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (ClassNotFoundException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    public static void create_branch(Context context,String branch_name,int parent_commit_id){
        Db_helper db_helper=new Db_helper(context);
        db_helper.create_branch(branch_name,parent_commit_id);
    }

    public static Map<String,String> get_data_for_commit(Context context,String _id){
        try{
            File input_file=new File(context.getExternalFilesDir(null),_id+"_data.txt");
            FileInputStream fis = new FileInputStream(input_file);
            ObjectInputStream ois= new ObjectInputStream(fis);
            Map<String,String> tune_data=(Map<String,String>) ois.readObject();
            ois.close();
            fis.close();
            return  tune_data;
        }catch (FileNotFoundException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (ClassNotFoundException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return null;
    }
}

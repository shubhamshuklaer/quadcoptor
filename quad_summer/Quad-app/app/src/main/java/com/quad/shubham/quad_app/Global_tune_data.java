package com.quad.shubham.quad_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Created by shubham on 10/6/15.
 */
public class Global_tune_data {
    public static String get_attribute(Context context,String attr_name){
        context=context.getApplicationContext();
        SharedPreferences shared_pref= context.getSharedPreferences("tuner_data",Context.MODE_PRIVATE);
        return shared_pref.getString(attr_name,"0");
    }

    public static void set_attribute(Context context,String attr_name,String val){
        context=context.getApplicationContext();
        SharedPreferences shared_pref= context.getSharedPreferences("tuner_data",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared_pref.edit();
        editor.putString(attr_name,val);
        editor.commit();
    }

    public static void commit(Context context,String commit_message){
        context=context.getApplicationContext();
        SharedPreferences shared_pref= context.getSharedPreferences("tuner_data",Context.MODE_PRIVATE);
        Map<String,String> tune_data=(Map<String,String>)shared_pref.getAll();
        Db_helper db_helper=new Db_helper(context);
        db_helper.commit(context, commit_message);
        int id=db_helper.get_num_rows(db_helper.COMMIT_TBL_NAME);
        try {
            FileOutputStream fos = context.openFileOutput(Integer.toString(id), Context.MODE_PRIVATE);
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

    public static void load_version(Context context,String id){
        context= context.getApplicationContext();

        try {
            FileInputStream fis = context.openFileInput(id);
            ObjectInputStream ois= new ObjectInputStream(fis);
            Map<String,String> tune_data=(Map<String,String>) ois.readObject();
            ois.close();
            fis.close();

            SharedPreferences shared_pref= context.getSharedPreferences("history_meta",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=shared_pref.edit();
            editor.putString("parent_id", id);
            editor.commit();

            shared_pref=context.getSharedPreferences("tuner_data",Context.MODE_PRIVATE);
            editor=shared_pref.edit();

            for(Map.Entry<String,String> entry: tune_data.entrySet())
                editor.putString(entry.getKey(),entry.getValue());

            editor.commit();
        }catch (FileNotFoundException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (IOException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (ClassNotFoundException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }
}

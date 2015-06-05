package com.quad.shubham.quad_app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by shubham on 5/6/15.
 */
public class Tuner extends AppCompatActivity{
    String config_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XmlPullParserFactory xmlFactoryObject=null;
        XmlPullParser myparser=null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            config_path = extras.getString("config_file_path");
            if(config_path==null){
                Toast toast=Toast.makeText(getApplicationContext(),"Empty config path", Toast.LENGTH_SHORT);
                toast.show();
                Tuner.this.finish();
                return;
            }
        }else{
            Toast toast=Toast.makeText(getApplicationContext(),"Config file path not sent", Toast.LENGTH_SHORT);
            toast.show();
            Tuner.this.finish();
            return;
        }
        Toast toast1=Toast.makeText(getApplicationContext(),"Hello", Toast.LENGTH_SHORT);
        toast1.show();

        try {
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            myparser = xmlFactoryObject.newPullParser();
            File config_file=new File(config_path);
            FileInputStream fis=new FileInputStream(config_file);

            myparser.setInput(new InputStreamReader(fis));
            int event_type=myparser.getEventType();
            while(event_type!=XmlPullParser.END_DOCUMENT){
                if(event_type==XmlPullParser.START_TAG){
                    System.out.print(myparser.getName());
                }
                event_type=myparser.next();
            }
        } catch(XmlPullParserException e){
            Toast toast=Toast.makeText(getApplicationContext(),"XmlPullParserException", Toast.LENGTH_SHORT);
            toast.show();
            Tuner.this.finish();
            return;
        } catch(IOException e){
            Toast toast=Toast.makeText(getApplicationContext(),"IOException", Toast.LENGTH_SHORT);
            toast.show();
            Tuner.this.finish();
            return;
        }



    }
}

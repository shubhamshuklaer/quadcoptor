package com.quad.shubham.quad_app;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.astuetz.PagerSlidingTabStrip;

import android.view.ViewGroup.LayoutParams;


/**
 * Created by shubham on 5/6/15.
 */
public class Tuner extends FragmentActivity{
    String config_path;
    Toast toast;
    ViewPager pager;
    My_pager_adapter pager_adapter;
    NodeList tab_list;
    PagerSlidingTabStrip tab_strip;
    LinearLayout layout;
    final int tab_strip_text_size=30;
    final int tab_strip_h_pad=10;
    final int pager_id=10;
    final int tab_strip_id=20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable ex) {
//                Log.e("normal","hello");
//                Log.e("normal",ex.getCause().toString());
//                Log.e("normal", ex.getMessage());
//                Log.e("normal",ex.getStackTrace().toString());
//            }
//        });

        DocumentBuilderFactory d_builder_factory_obj=null;
        DocumentBuilder d_builder=null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            config_path = extras.getString("config_file_path");
            if(config_path==null){
                toast=Toast.makeText(getApplicationContext(),"Empty config path", Toast.LENGTH_SHORT);
                toast.show();
                Tuner.this.finish();
                return;
            }
        }else{
            toast=Toast.makeText(getApplicationContext(),"Config file path not sent", Toast.LENGTH_SHORT);
            toast.show();
            Tuner.this.finish();
            return;
        }

        pager=new ViewPager(this);
        pager.setId(pager_id);
        pager_adapter=new My_pager_adapter(getSupportFragmentManager());
        layout=new LinearLayout(this);
        tab_strip=new PagerSlidingTabStrip(this);
        tab_strip.setId(tab_strip_id);
        tab_strip.setTabPaddingLeftRight(tab_strip_h_pad);
        tab_strip.setTextSize(tab_strip_text_size);
        try {
            d_builder_factory_obj = DocumentBuilderFactory.newInstance();
            d_builder = d_builder_factory_obj.newDocumentBuilder();
            File config_file=new File(config_path);
            FileInputStream fis=new FileInputStream(config_file);

            Document doc=d_builder.parse(fis);

            tab_list=doc.getElementsByTagName(getString(R.string.tab_tag));
            pager_adapter.set_fragments(tab_list);

        } catch(ParserConfigurationException e) {
            toast = Toast.makeText(getApplicationContext(), "ParserConfigurationException", Toast.LENGTH_SHORT);
            toast.show();
            Tuner.this.finish();
            return;
        } catch(SAXException e){
            toast = Toast.makeText(getApplicationContext(), "SAXException", Toast.LENGTH_SHORT);
            toast.show();
            Log.d(getString(R.string.default_tag_name),e.getMessage());
            Tuner.this.finish();
            return;
        } catch(IOException e){
            toast=Toast.makeText(getApplicationContext(),"IOException", Toast.LENGTH_SHORT);
            toast.show();
            Tuner.this.finish();
            return;
        }

        pager.setAdapter(pager_adapter);
        tab_strip.setViewPager(pager);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(tab_strip);
        layout.addView(pager);

        setContentView(layout);


    }

    Node get_tab_node(int position){
        return tab_list.item(position);
    }
}

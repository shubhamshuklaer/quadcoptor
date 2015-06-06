package com.quad.shubham.quad_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;


public class Home extends AppCompatActivity {

    String[] activity_list={"Tuner","Data Logs","Show History","Select Config"};
    ListView activity_list_view;
    String config_path;
    private static final int REQUEST_CHOOSER = 1234;
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config_path=null;
        activity_list_view = new ListView(this);
        ArrayAdapter<String> list_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, activity_list);
        activity_list_view.setAdapter(list_adapter);
        activity_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position) {
                    case 0:
                        intent = new Intent(Home.this, Tuner.class);
                        intent.putExtra("config_file_path", config_path);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(Home.this, Data_logs.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(Home.this, Show_history.class);
                        startActivity(intent);
                        break;
                    case 3:
                        // Create the ACTION_GET_CONTENT Intent
//                        Intent getContentIntent = FileUtils.createGetContentIntent();
//                        intent = Intent.createChooser(getContentIntent, "Select a file");
                        intent = new Intent(Home.this, FileChooserActivity.class);
                        startActivityForResult(intent, REQUEST_CHOOSER);
                        break;
                }
            }
        });
        sharedPref = Home.this.getPreferences(Context.MODE_PRIVATE);
        config_path=sharedPref.getString(getString(R.string.config_file_path_setting),null);
        setContentView(activity_list_view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {

                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    config_path = FileUtils.getPath(this, uri);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.config_file_path_setting),config_path);
                    editor.commit();
                    Toast toast=Toast.makeText(getApplicationContext(), config_path, Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
        }
    }
}

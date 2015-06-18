package com.quad.shubham.quad_app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;


public class Home extends Activity {

    String[] activity_list={"Tuner","Data Logs","Show History","Select Config","Commit","Bluetooth","test"};
    ListView activity_list_view;
    String config_path;
    private static final int REQUEST_CHOOSER = 1234;
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
                        intent = new Intent(Home.this, FileChooserActivity.class);
                        startActivityForResult(intent, REQUEST_CHOOSER);
                        break;
                    case 4:
                        intent = new Intent(Home.this, Commit.class);
                        startActivity(intent);
                        break;
                    case 5:
                        intent = new Intent(Home.this, Bluetooth.class);
                        startActivity(intent);
                        break;
                    case 6:
                        intent = new Intent(Home.this,Test.class);
                        startActivity(intent);
                }
            }
        });

        config_path= Data_store.get_attribute(Home.this, Data_store.CONFIG_FILE_PATH_SETTING, null);
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
                    Data_store.set_attribute(Home.this, Data_store.CONFIG_FILE_PATH_SETTING, config_path);
                    Toast toast=Toast.makeText(getApplicationContext(), config_path, Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
        }
    }
}

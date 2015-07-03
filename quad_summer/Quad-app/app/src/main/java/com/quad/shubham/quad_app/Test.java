package com.quad.shubham.quad_app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by shubham on 17/6/15.
 */
public class Test extends Activity {
    LinearLayout layout;
    EditText edit_text,pattern_text;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout=new LinearLayout(Test.this);
        edit_text=new EditText(Test.this);
        edit_text.setHint("Text");
        pattern_text=new EditText(Test.this);
        pattern_text.setHint("Pattern");
        btn=new Button(Test.this);
        btn.setText("Test text with pattern");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit_text.getText().toString().matches(pattern_text.getText().toString()))
                    Toast.makeText(Test.this,"Matches",Toast.LENGTH_SHORT).show();
            }
        });
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(edit_text);
        layout.addView(pattern_text);
        layout.addView(btn);
        setContentView(layout);
    }
}

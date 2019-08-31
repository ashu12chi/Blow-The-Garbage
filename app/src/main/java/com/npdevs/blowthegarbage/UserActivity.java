package com.npdevs.blowthegarbage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class UserActivity extends AppCompatActivity {
    private TextView text,text2,text3,text4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        text = findViewById(R.id.editText3);
        text2 = findViewById(R.id.textView6);
        text3 = findViewById(R.id.textView7);
        text4 = findViewById(R.id.textView8);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGarbageClassifyActivity();
            }
        });
        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUploadActivity();
            }
        });
    }

    private void openUploadActivity() {
        Intent intent = new Intent(this,UploadActivity.class);
        startActivity(intent);
    }

    private void openGarbageClassifyActivity() {
        Intent intent = new Intent(this,GarbageClassify.class);
        startActivity(intent);
    }
}

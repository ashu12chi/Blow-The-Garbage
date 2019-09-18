package com.npdevs.blowthegarbage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {
    private EditText feedback;
    private DatabaseReference databaseReference;
    private Button send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        feedback = findViewById(R.id.editText8);
        send = findViewById(R.id.send);
        databaseReference = FirebaseDatabase.getInstance().getReference("feedback");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long time = System.currentTimeMillis();
                String feed = feedback.getText().toString();
                if(feed.trim().equals(""))
                    Toast.makeText(getApplicationContext(),"Enter something",Toast.LENGTH_SHORT).show();
                else {
                    databaseReference.child(time + "").setValue(feed);
                    Toast.makeText(getApplicationContext(),"Sucess!!!",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}

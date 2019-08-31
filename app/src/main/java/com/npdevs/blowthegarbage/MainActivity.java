package com.npdevs.blowthegarbage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private Button about;
    private Button login;
    private EditText mobNumber;
    private EditText password;
    private Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        about = findViewById(R.id.about);
        login = findViewById(R.id.login);
        mobNumber = findViewById(R.id.mobNumber);
        password = findViewById(R.id.password);
        signup = findViewById(R.id.button3);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAboutActivity();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSignUpActivity();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUserActivity();
            }
        });
    }

    private void openUserActivity() {
        Intent intent = new Intent(this,UserActivity.class);
        startActivity(intent);
    }

    private void openSignUpActivity() {
        Intent intent = new Intent(this,SignUp.class);
        startActivity(intent);
    }

    private void openAboutActivity() {
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }
}

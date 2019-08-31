package com.npdevs.blowthegarbage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignUp extends AppCompatActivity {

    private Button signup;
    private EditText name,mobNumber,address,password1,password2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signup = findViewById(R.id.button);
        name = findViewById(R.id.editText);
        mobNumber = findViewById(R.id.editText2);
        address = findViewById(R.id.editText3);
        password1 = findViewById(R.id.editText4);
        password2 = findViewById(R.id.editText5);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });
    }
    private void openMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}

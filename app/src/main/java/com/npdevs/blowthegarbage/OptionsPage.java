package com.npdevs.blowthegarbage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class OptionsPage extends AppCompatActivity {
	private CardView text,text2,text3,text4;
	String MOB_NUMBER;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		text = findViewById(R.id.cardView);
		text3 = findViewById(R.id.cardView2);
		text2 = findViewById(R.id.cardView3);
		text4 = findViewById(R.id.cardView4);
		Intent intent = getIntent();
		MOB_NUMBER=intent.getStringExtra("MOB_NUMBER");
		text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openGarbageClassifyActivity();
			}
		});
		text3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openMapsSelectLocationActivity();
			}
		});
	}

	private void openGarbageClassifyActivity() {
		Intent intent = new Intent(this,GarbageClassify.class);
		intent.putExtra("MOB_NUMBER",MOB_NUMBER);
		startActivity(intent);
	}

	private void openMapsSelectLocationActivity() {
		Intent intent=new Intent(this,MapsSelectLocation.class);
		intent.putExtra("MOB_NUMBER",MOB_NUMBER);
		startActivity(intent);
	}
}

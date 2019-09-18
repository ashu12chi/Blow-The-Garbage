package com.npdevs.blowthegarbage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class OptionsPage extends AppCompatActivity {
	private CardView text,text2,text3,text4;
	Button btnLogout;
	String MOB_NUMBER;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		text = findViewById(R.id.cardView);
		text3 = findViewById(R.id.cardView2);
		text2 = findViewById(R.id.cardView3);
		text4 = findViewById(R.id.cardView4);
		btnLogout=findViewById(R.id.logout);

		Intent intent = getIntent();
		MOB_NUMBER=intent.getStringExtra("MOB_NUMBER");

		text.setOnClickListener(view -> openGarbageClassifyActivity());
		text3.setOnClickListener(view -> openMapsSelectLocationActivity());
		text4.setOnClickListener(view -> openComplaintActivity());
		text2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openFeedbackActivity();
			}
		});
	}

	private void openFeedbackActivity() {
		Intent intent = new Intent(this,FeedbackActivity.class);
		startActivity(intent);
	}

	private void openComplaintActivity() {
		Intent intent = new Intent(this,ComplaintActivity.class);
		startActivity(intent);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.driver_options, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		//Display menu to user
		switch (item.getItemId()) {
			case R.id.logout:
				clearTable();
				saveTable();
				finish();
			default:
				return super.onOptionsItemSelected(item);
		}
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

	private void clearTable()
	{
		SharedPreferences preferences = getSharedPreferences("usersave", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	private void saveTable()
	{
		SharedPreferences sharedPreferences=getSharedPreferences("usersave",MODE_PRIVATE);
		SharedPreferences.Editor editor=sharedPreferences.edit();
		editor.putString("User","no");
		editor.apply();
	}
}

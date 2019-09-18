package com.npdevs.blowthegarbage;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

public class push_notification extends IntentService {

	private int saved=0;
	private int count=0;
	private String loggedIn;

	public push_notification() {
		super("push_notification");
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onHandleIntent(Intent intent) {

        Log.e("Info","Reached Here by NSP");
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
		loadUser();
		if(loggedIn.charAt(0)=='A') {
			final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("garbage-request");
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					loadPreferences();
					count = 0;
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						if (!snapshot.child("verified").getValue(Boolean.class)) {
							count++;
						}
					}
					Log.e("NSP", "Saved: " + saved + " Count: " + count);
					if (saved < count) {
						Log.e("Info", "Reached Here too by NSP");
						createNotificationChannel();
						notification();
					}
					clearPreferences();
					savePreferences(count);
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
				}
			});
		}
	}

	private void createNotificationChannel() {

		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel("New Garbage", "New Garbage", importance);
			channel.enableVibration(true);
			channel.enableLights(true);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	private void notification() {
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pendingintent = PendingIntent.getActivity(this, 0, intent, 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "New Garbage")
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.setAutoCancel(true)
				.setContentTitle("New Garbage")
				.setContentText("New Unverified Garbage Added")
				.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setStyle(new NotificationCompat.BigTextStyle()
						.bigText("Unverified Garbage Added, tap to login..."))
				.setContentIntent(pendingintent);
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//		notificationId is a unique int for each notification that you must define
		notificationManager.notify(12, builder.build());
	}

	private void loadPreferences()
	{
		SharedPreferences sharedPreferences=getSharedPreferences("CheckNPAlarm",MODE_PRIVATE);
		saved=Integer.parseInt(sharedPreferences.getString("Number","0"));
	}

	private void savePreferences(int value)
	{
		SharedPreferences sharedPreferences=getSharedPreferences("CheckNPAlarm",MODE_PRIVATE);
		SharedPreferences.Editor editor=sharedPreferences.edit();
		editor.putString("Number", ""+value);
		editor.apply();
	}

	private void clearPreferences()
	{
		SharedPreferences preferences = getSharedPreferences("CheckNPAlarm", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	private void loadUser()
	{
		SharedPreferences sharedPreferences=getSharedPreferences("usersave",MODE_PRIVATE);
		loggedIn=sharedPreferences.getString("User","no");
		if(loggedIn.equals("") || loggedIn.isEmpty() || loggedIn.equals("no"))
			loggedIn="no";
	}
}

package com.npdevs.blowthegarbage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Admin extends AppCompatActivity {
	private RecyclerView recyclerView;
	private DatabaseReference databaseReference;
	private StorageReference storageReference;
	private Button approved;
	private Button disapproved;

	List<SampleItem> msampleItem = new ArrayList<>();

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.admin_options, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		//Display menu to user
		switch (item.getItemId()) {
			case R.id.register:
				Intent intent3 = new Intent(Admin.this,DriverRegisterActivity.class);
				startActivity(intent3);
				return true;
			case R.id.feedback:
				Intent intent4 = new Intent(Admin.this,AdminFeedback.class);
				startActivity(intent4);
				return true;
			case R.id.complaint:
				Intent intent5 = new Intent(Admin.this,AdminComplaint.class);
				startActivity(intent5);
				return true;
			case R.id.organic:
				Intent intent = new Intent(Admin.this,OrganicGraph.class);
				startActivity(intent);
				return true;
			case R.id.severe:
				Intent intent1 = new Intent(Admin.this,SevereGraph.class);
				startActivity(intent1);
				return true;
			case R.id.garbage:
				Intent intent2 = new Intent(Admin.this,BarGraph.class);
				startActivity(intent2);
				return true;
			case R.id.logout:
				clearTable();
				saveTable();
				finish();
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin);
		ProgressDialog progressDialog=new ProgressDialog(Admin.this);
		progressDialog.setMessage("Loading...");
		progressDialog.setCancelable(false);
		progressDialog.show();
		databaseReference = FirebaseDatabase.getInstance().getReference("garbage-request");
		storageReference = FirebaseStorage.getInstance().getReference("garbage-request");
		FirebaseApp.initializeApp(this);

		databaseReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				msampleItem.clear();
				progressDialog.show();
				for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
				{
					Garbage post = postSnapshot.getValue(Garbage.class);
					assert post != null;
					if(!post.getVerified())
					{
						msampleItem.add(new SampleItem(post.getDescription(),post.getOrganic(),
								post.getSevere(),post.getUpvotes(),post.getUrl(),approved,disapproved,postSnapshot.getKey()));
						Log.e("ashu", msampleItem.size()+"");
					}
				}
				recyclerView = findViewById(R.id.recycler_view);
				recyclerView.setHasFixedSize(true);
				RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Admin.this);
				recyclerView.setLayoutManager(layoutManager);
				RecyclerView.Adapter adapter = new MainAdapter(msampleItem);
				recyclerView.setAdapter(adapter);
				progressDialog.cancel();
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}
	private class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

		private List<SampleItem> samples;

		class ViewHolder extends RecyclerView.ViewHolder {

			private TextView nameView,organic,severe,upvote;
			private ImageView garbage;
			private Button approved,disapproved;

			ViewHolder(View view) {
				super(view);
				nameView = view.findViewById(R.id.nameView);
				garbage = view.findViewById(R.id.garbage);
				organic = view.findViewById(R.id.organic1);
				severe = view.findViewById(R.id.severe1);
				upvote = view.findViewById(R.id.upvote1);
				approved = view.findViewById(R.id.approved);
				disapproved = view.findViewById(R.id.disapproved);
			}
		}

		MainAdapter(List<SampleItem> samples) {
			this.samples = samples;
			Log.e("nsp",samples.size()+"");
		}

		@NonNull
		@Override
		public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater
					.from(parent.getContext())
					.inflate(R.layout.item_main_feature, parent, false);

			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, int position) {
			holder.nameView.setText(samples.get(position).getName());
			holder.organic.setText(samples.get(position).getOrganic() ? "Organic" : "Inorganic");
			holder.severe.setText(samples.get(position).getSevere() ? "Severe" : "Not Severe");
			holder.upvote.setText("Upvotes: "+samples.get(position).getUpvote());
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

			StrictMode.setThreadPolicy(policy);
			Bitmap bm = null;
			try {
				bm = BitmapFactory.decodeStream((new URL(samples.get(position).getGarbage())).openConnection().getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
//			Bitmap resized = Bitmap.createScaledBitmap(bm, bm.getWidth()*6, bm.getHeight()*6, true);
			holder.garbage.setImageBitmap(bm);
			System.err.println(samples.get(position).getGarbage());
			holder.approved.setOnClickListener(view -> {
				databaseReference.child(samples.get(position).getKey()).child("verified").setValue(true);
			});
			holder.disapproved.setOnClickListener(view -> {
				databaseReference.child(samples.get(position).getKey()).setValue(null);
			});
		}

		@Override
		public int getItemCount() {
			return samples.size();
		}
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

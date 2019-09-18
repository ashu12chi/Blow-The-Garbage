package com.npdevs.blowthegarbage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AdminFeedback extends AppCompatActivity {
	private RecyclerView recyclerView;
	private DatabaseReference databaseReference;
	List<FeedbackItem> msampleItem = new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_feedback);
		recyclerView = findViewById(R.id.recycler_view);
		databaseReference = FirebaseDatabase.getInstance().getReference("feedback");
		databaseReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				msampleItem.clear();
				for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
				{
					msampleItem.add(new FeedbackItem(postSnapshot.getValue().toString()));
				}
				recyclerView = findViewById(R.id.recycler_view);
				recyclerView.setHasFixedSize(true);
				RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AdminFeedback.this);
				recyclerView.setLayoutManager(layoutManager);
				RecyclerView.Adapter adapter = new AdminFeedback.MainAdapter(msampleItem);
				recyclerView.setAdapter(adapter);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}
	private class MainAdapter extends RecyclerView.Adapter<AdminFeedback.MainAdapter.ViewHolder> {

		private List<FeedbackItem> samples;

		class ViewHolder extends RecyclerView.ViewHolder {

			private TextView feedback;

			ViewHolder(View view) {
				super(view);
				feedback = view.findViewById(R.id.feedback);
			}
		}

		MainAdapter(List<FeedbackItem> samples) {
			this.samples = samples;
			Log.e("nsp",samples.size()+"");
		}

		@NonNull
		@Override
		public AdminFeedback.MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater
					.from(parent.getContext())
					.inflate(R.layout.item_main_features2, parent, false);

			return new AdminFeedback.MainAdapter.ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull AdminFeedback.MainAdapter.ViewHolder holder, int position) {
			holder.feedback.setText(position+1+". "+samples.get(position).getFeedback());
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

			StrictMode.setThreadPolicy(policy);
		}

		@Override
		public int getItemCount() {
			return samples.size();
		}
	}

}

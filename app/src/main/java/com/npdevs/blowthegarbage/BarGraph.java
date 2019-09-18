package com.npdevs.blowthegarbage;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class BarGraph extends AppCompatActivity {

    private BarChart barChart;
    private ArrayList<BarEntry> barEntries;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_graph);

        barChart = findViewById(R.id.bargraph);
        databaseReference = FirebaseDatabase.getInstance().getReference("garbage-cleaned");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,Integer> garbage = new HashMap<>();
                ArrayList<String> dates = new ArrayList<>();
                for(DataSnapshot post : dataSnapshot.getChildren())
                {
                    String date = post.getKey().toString();
                    date = date.substring(0,4)+'/'+date.substring(4,6)+'/'+date.substring(6);
                    garbage.put(date,Integer.parseInt(post.getValue().toString()));
                    dates.add(date);
                }
               // Log.e("ashu",garbage.get(dates.get(0))+"");
                createRandomBarGraph(garbage,dates);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void createRandomBarGraph(HashMap<String,Integer> garbage,ArrayList<String> dates){

            barEntries = new ArrayList<>();
            for(int j = 0; j< dates.size();j++){
                barEntries.add(new BarEntry(j,garbage.get(dates.get(j))));
               // Log.e("ashu",barEntries.get(0)+"");
            }


        BarDataSet barDataSet = new BarDataSet(barEntries,"Dates");
      //  Log.e("ashu",barEntries.get(0)+"");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.setDescription("Garbage Daily Count!");

    }

}


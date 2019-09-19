package com.npdevs.blowthegarbage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SevereGraph extends AppCompatActivity {

    private float[] yData = {0,0};
    private DatabaseReference databaseReference;
    private String[] xData = {"Severe","Non-Severe"};
    PieChart pieChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Severity pie");
        databaseReference = FirebaseDatabase.getInstance().getReference("graph-data");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int severe=0;
                int notsevere=0;
                if(dataSnapshot.child("severe").exists()){
                    severe=dataSnapshot.child("severe").getValue(Integer.class);
                }
                if(dataSnapshot.child("notsevere").exists()){
                    notsevere=dataSnapshot.child("notsevere").getValue(Integer.class);
                }
                yData[0]=severe;
                yData[1]=notsevere;
                setContentView(R.layout.activity_severe_graph);
                pieChart = (PieChart) findViewById(R.id.idPieChart);
                pieChart.setDescription("Severity");
                pieChart.setRotationEnabled(true);
                pieChart.setHoleRadius(25f);
                pieChart.setTransparentCircleAlpha(0);
                pieChart.setCenterText("Severe/Non-Severe");
                pieChart.setCenterTextSize(10);

                addDataSet();
                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {

                        int pos1 = e.toString().indexOf("(sum): ");
                        String sales = e.toString().substring(pos1 + 7);

                        for (int i = 0; i < yData.length; i++) {
                            if (yData[i] == Float.parseFloat(sales)) {
                                pos1 = i;
                                break;
                            }
                        }
                        String data = xData[(int)h.getX()];
                        Toast.makeText(SevereGraph.this, data + "\n" + "Count: " + sales , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected() {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void addDataSet() {
        // Log.d(TAG, "addDataSet started");
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , i));
        }

        for(int i = 1; i < xData.length; i++){
            xEntrys.add(xData[i]);
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Severe/Non-Severe Garbages");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.CYAN);

        pieDataSet.setColors(colors);

        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}

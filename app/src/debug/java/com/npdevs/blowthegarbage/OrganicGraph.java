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

public class OrganicGraph extends AppCompatActivity {

    private float[] yData = {2,2};
    private DatabaseReference databaseReference;
    private String[] xData = {"Organic","In-Organic"};
    PieChart pieChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference("graph-data");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int organic=0;
                int inorganic=0;
                if(dataSnapshot.child("organic").exists()){
                    organic=dataSnapshot.child("organic").getValue(Integer.class);
                }
                if(dataSnapshot.child("inorganic").exists()){
                    inorganic=dataSnapshot.child("inorganic").getValue(Integer.class);
                }
                yData[0]=organic;
                yData[1]=inorganic;
                setContentView(R.layout.activity_organic_graph);
                pieChart = (PieChart) findViewById(R.id.idPieChart);
                pieChart.setDescription("Organic/In-Organic ");
                pieChart.setRotationEnabled(true);
                //pieChart.setUsePercentValues(true);
                //pieChart.setHoleColor(Color.BLUE);
                //pieChart.setCenterTextColor(Color.BLACK);
                pieChart.setHoleRadius(25f);
                pieChart.setTransparentCircleAlpha(0);
                pieChart.setCenterText("Organic-Inorganic");
                pieChart.setCenterTextSize(10);
                //pieChart.setDrawEntryLabels(true);
                //pieChart.setEntryLabelTextSize(20);
                //More options just check out the documentation!

                addDataSet();
                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        String TAG="nsp";
//                             Log.e(TAG, "onValueSelected: Value select from chart.");
//                           Log.e(TAG, "onValueSelected: " + e.toString());
//                         Log.e(TAG, "onValueSelected: " + h.toString());

                        int pos1 = e.toString().indexOf("(sum): ");
                        String sales = e.toString().substring(pos1 + 7);

                        for (int i = 0; i < yData.length; i++) {
                            if (yData[i] == Float.parseFloat(sales)) {
                                pos1 = i;
                                break;
                            }
                        }
                        String data = xData[(int)h.getX()];
                        Toast.makeText(OrganicGraph.this, data + "\n" + "Count: " + sales , Toast.LENGTH_SHORT).show();
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
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Organic-Inorganic Garbages");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);

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

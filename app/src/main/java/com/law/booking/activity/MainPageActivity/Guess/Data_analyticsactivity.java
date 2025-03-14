package com.law.booking.activity.MainPageActivity.Guess;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Usermodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Data_analyticsactivity extends AppCompatActivity {
    private String key,username;
    private String TAG = "data_analytics";
    private ImageView back;
    private BarChart barGraph;
    private PieChart pieChart;
    private TextView complete_txt,cancel_txt,total_txt,title,content;
    private LinearLayout linear_graph;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_analyticsactivity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));
        barGraph = findViewById(R.id.barGraph);
        pieChart = findViewById(R.id.piechart);
        linear_graph = findViewById(R.id.linear_graph);
        back = findViewById(R.id.back);
        complete_txt = findViewById(R.id.complete_txt);
        title = findViewById(R.id.title);
        cancel_txt = findViewById(R.id.cancel_txt);
        content = findViewById(R.id.content);
        total_txt = findViewById(R.id.total_txt);
        back.setOnClickListener(view -> onBackPressed());
        key = getIntent().getStringExtra("id");
        username = getIntent().getStringExtra("username");
        Log.d(TAG,"key: "+key);
        title.setText(username+" "+"Data");
        linear_graph.setVisibility(View.VISIBLE);
        initFirebase();

    }

    private void initFirebase() {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Lawyer").child(key);

        DatabaseReference piechartref = FirebaseDatabase.getInstance().getReference("Lawname").child(key);

        piechartref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<PieEntry> entries = new ArrayList<>();

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String lawType = childSnapshot.getKey();
                        Float value = childSnapshot.getValue(Float.class);

                        if (lawType != null && value != null) {
                            entries.add(new PieEntry(value, lawType));
                        }
                    }

                    updatePieChart(entries);
                } else {
                    Log.d(TAG, "No data found for Lawname with key: " + key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load law type: " + databaseError.getMessage());
            }
        });
        


        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usermodel user = dataSnapshot.getValue(Usermodel.class);
                    if(user !=null){
                        int bookcount = user.getBookcount();
                        int bookcomplete = user.getBookcomplete();
                        int bookcancel = user.getBookcancel();
                        summary(bookcount, bookcomplete, bookcancel);
                        Log.d(TAG,"Book data: "+"bookcount: "+ bookcount +","+"bookcomplete: "+bookcomplete+","+"bookcancel: "+bookcancel);
                        setupBarChart(bookcount, bookcomplete, bookcancel);
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void updatePieChart(List<PieEntry> entries) {
        if (entries.isEmpty()) {
            Log.d(TAG, "No data available to update PieChart");
            return;
        }

        List<Integer> colors = getRandomColors(entries.size());

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        dataSet.setValueTextColors(getContrastingTextColors(colors));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private List<Integer> getRandomColors(int size) {
        List<Integer> colors = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            colors.add(Color.rgb(r, g, b));
        }
        return colors;
    }

    private List<Integer> getContrastingTextColors(List<Integer> colors) {
        List<Integer> textColors = new ArrayList<>();
        for (int color : colors) {
            if (isBrightColor(color)) {
                textColors.add(Color.BLACK);
            } else {
                textColors.add(Color.WHITE);
            }
        }
        return textColors;
    }

    private boolean isBrightColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        double luminance = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance > 150;
    }

    private void summary(int bookcount, int bookcomplete, int bookcancel) {
        total_txt.setText("The total book is: " + bookcount);
        complete_txt.setText("Complete booking is: " + bookcomplete);
        cancel_txt.setText("Cancel booking is: " + bookcancel);

        if (bookcount > 0) {
            double completePercentage = ((double) bookcomplete / bookcount) * 100;
            double cancelPercentage = ((double) bookcancel / bookcount) * 100;

            content.setText("This lawyer's data represents a success rate of " + String.format("%.2f", completePercentage) + "% and a cancel rate of " + String.format("%.2f", cancelPercentage) + "%");
        } else {
            content.setText("No booking data available for evaluation.");
        }
    }


    private void setupBarChart(int bookcount, int bookcomplete, int bookcancel) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, bookcount));
        entries.add(new BarEntry(1, bookcomplete));
        entries.add(new BarEntry(2, bookcancel));
        List<Integer> colors = getRandomColors(entries.size());
        BarDataSet dataSet = new BarDataSet(entries, "Booking Data");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barGraph.setData(data);
        barGraph.getXAxis().setGranularity(1f);
        barGraph.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String[] labels = {"Total book", "Completed", "Canceled"};
                if (value >= 0 && value < labels.length) {
                    return labels[(int) value];
                }
                return "";
            }
        });

        barGraph.getDescription().setEnabled(false);
        barGraph.animateY(1000);
        barGraph.invalidate();
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
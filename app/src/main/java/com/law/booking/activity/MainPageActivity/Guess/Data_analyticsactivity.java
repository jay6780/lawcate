package com.law.booking.activity.MainPageActivity.Guess;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Usermodel;

import java.util.ArrayList;

public class Data_analyticsactivity extends AppCompatActivity {
    private String key,username;
    private String TAG = "data_analytics";
    private ImageView back;
    private BarChart barGraph;
    private TextView complete_txt,cancel_txt,total_txt,title,content;
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
        initFirebase();

    }

    private void initFirebase() {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Lawyer").child(key);

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

        BarDataSet dataSet = new BarDataSet(entries, "Booking Data");
        dataSet.setColors(Color.parseColor("#A6F1E0"), Color.parseColor("#09122C"), Color.parseColor("#E52020"));
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
package com.law.booking.activity.Fragments.AdminFrag;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Usermodel;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Data_analytics_fragment extends Fragment implements OnRefreshListener {
    private String TAG = "data_analytics";
    private BarChart barGraph;
    private PieChart pieChart;
    private TextView complete_txt, cancel_txt, total_txt, content;
    private LinearLayout linear_graph;
    private SmartRefreshLayout refreshLayout;
    private boolean isRefresh = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_analytics_fragment, container, false);
        barGraph = view.findViewById(R.id.barGraph);
        pieChart = view.findViewById(R.id.piechart);
        linear_graph = view.findViewById(R.id.linear_graph);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        complete_txt = view.findViewById(R.id.complete_txt);
        cancel_txt = view.findViewById(R.id.cancel_txt);
        content = view.findViewById(R.id.content);
        total_txt = view.findViewById(R.id.total_txt);
        linear_graph.setVisibility(View.VISIBLE);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableRefresh(true);
        initFirebase();
        return view;
    }

    private void initFirebase() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.getLayout().postDelayed(() -> {
            isRefresh = true;
            if(isRefresh){
                refreshLayout.finishRefresh();
                initFirebase();
            }else{
                refreshLayout.finishRefresh(false);
            }
        }, 100);
    }

    @Override
    public void onResume() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onResume();
    }
}
package com.law.booking.activity.Fragments.AdminFrag;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.law.booking.R;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Data_analytics_fragment extends Fragment implements OnRefreshListener {
    private String TAG = "data_analytics";
    private BarChart barGraph;
    private PieChart pieChart;
    private TextView complete_txt, cancel_txt, total_txt, content;
    private LinearLayout linear_graph;
    private SmartRefreshLayout refreshLayout;
    private boolean isRefresh = false;
    private MaterialSpinner timeStampSpinner;
    private String timeStampValue;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_analytics_fragment, container, false);
        barGraph = view.findViewById(R.id.barGraph);
        pieChart = view.findViewById(R.id.piechart);
        timeStampSpinner = view.findViewById(R.id.date_spinner);
        linear_graph = view.findViewById(R.id.linear_graph);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        complete_txt = view.findViewById(R.id.complete_txt);
        cancel_txt = view.findViewById(R.id.cancel_txt);
        content = view.findViewById(R.id.content);
        total_txt = view.findViewById(R.id.total_txt);
        linear_graph.setVisibility(View.VISIBLE);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableRefresh(true);
        init_barChart();
        initFirebase();
        timeStampSpinner.setItems("All", "Day","Week","Month");

        timeStampSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                timeStampValue = item;
                initFirebase();
                init_barChart();
            }
        });
        return view;
    }


    private void init_barChart() {
        barGraph.clear();
        barGraph.invalidate();

        summary(0, 0, 0);

        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Lawyer_data").child(key);

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long totalBookCount = 0;
                    long totalBookCancel = 0;
                    long totalBookComplete = 0;
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Long bookCount = childSnapshot.child("bookcount").getValue(Long.class);
                        Long bookCancel = childSnapshot.child("bookcancel").getValue(Long.class);
                        Long bookComplete = childSnapshot.child("bookcomplete").getValue(Long.class);
                        String timeStampStr = childSnapshot.child("timeStamp").getValue(String.class);

                        totalBookCount += (bookCount != null) ? bookCount : 0;
                        totalBookCancel += (bookCancel != null) ? bookCancel : 0;
                        totalBookComplete += (bookComplete != null) ? bookComplete : 0;

                        if(timeStampStr !=null){
                            int bookingCount = (int) totalBookCount;
                            int bookingCancel = (int) totalBookCancel;
                            int bookingComplete = (int) totalBookComplete;

                            if (applyFlexibleFilter(timeStampStr)) {
                                summary(bookingCount, bookingCancel, bookingComplete);
                                setupBarChart(bookingCount, bookingComplete, bookingCancel);
                            }
                        }
                    }
//
//                    Log.d("FIREBASE_LOG", "Merged Data: " +
//                            "Total Book Count: " + totalBookCount + ", " +
//                            "Total Book Cancel: " + totalBookCancel + ", " +
//                            "Total Book Complete: " + totalBookComplete);

                } else {
                    Log.d("FIREBASE_LOG", "No data found in Lawyer_data.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FIREBASE_LOG", "Error: " + databaseError.getMessage());
            }
        });
    }



    private void initFirebase() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference piechartref = FirebaseDatabase.getInstance().getReference("Lawname").child(key);

        piechartref.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Integer> entryMap = new HashMap<>();
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String lawType = childSnapshot.child("serviceName").getValue(String.class);
                        Integer value = childSnapshot.child("count").getValue(Integer.class);
                        String timeStampStr = childSnapshot.child("timeStamp").getValue(String.class);
                        Log.d("TimeStamp","value: "+timeStampStr);

                        if (applyFlexibleFilter(timeStampStr)){
                            if (lawType != null && value != null) {
                                entryMap.put(lawType, entryMap.getOrDefault(lawType, 0) + value);
                            }
                        }
                    }

                    List<PieEntry> entries = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : entryMap.entrySet()) {
                        entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                    }

                    updatePieChart(entries);
                } else {
                    Log.d(TAG, "No data found for Lawname.");
                    updatePieChart(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load law type: " + databaseError.getMessage());
            }
        });
    }

    private boolean applyFlexibleFilter(String timeStampMillis) {
        if (timeStampValue == null || timeStampValue.equals("All")) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - Long.parseLong(timeStampMillis);

        switch (timeStampValue) {
            case "Day":
                return timeDiff <= 24 * 60 * 60 * 1000;
            case "Week":
                return timeDiff <= 21 * 24 * 60 * 60 * 1000 && timeDiff > 3 * 24 * 60 * 60 * 1000;
            case "Month":
                return timeDiff <= 30 * 24 * 60 * 60 * 1000 && timeDiff > 21 * 24 * 60 * 60 * 1000;
            default:
                return true;
        }
    }


    private void updatePieChart(List<PieEntry> entries) {
        pieChart.clear();
        if (entries.isEmpty()) {
            Log.d(TAG, "No data available to update PieChart");
            pieChart.invalidate();
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
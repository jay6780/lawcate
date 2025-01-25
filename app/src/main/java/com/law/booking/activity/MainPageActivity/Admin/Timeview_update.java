package com.law.booking.activity.MainPageActivity.Admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.TimeSlot;
import com.law.booking.activity.tools.adapter.Time_viewadapter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Timeview_update extends AppCompatActivity  implements OnRefreshListener {
    private RecyclerView time_viewrecycler;
    private String key,dateString,databasename,adminkey,nametitle;
    private List<TimeSlot> mytimeslot = new ArrayList<>();
    private TextView title,dateschedule;
    private ImageView back_btn,clear;
    private  Time_viewadapter timeAdapter;
    private SmartRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeview_update);
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        key = getIntent().getStringExtra("key");
        dateString = getIntent().getStringExtra("date");
        adminkey = getIntent().getStringExtra("userId");
        databasename = getIntent().getStringExtra("databasename");
        nametitle = getIntent().getStringExtra("title");
        time_viewrecycler = findViewById(R.id.time_viewrecycler);
        dateschedule = findViewById(R.id.dateschedule);
        back_btn = findViewById(R.id.back_btn);
        clear = findViewById(R.id.clear);
        title = findViewById(R.id.title);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableRefresh(true);
        title.setText(nametitle);
        title.setTextSize(16);
        initdateformat();
        clear.setVisibility(View.VISIBLE);
        clear.setOnClickListener(view -> clearalldialog());
        back_btn.setOnClickListener(view -> onBackPressed());
        show_timeframe(key,adminkey);
    }

    private void show_timeframe(String schedulekey, String adminkey) {
        Log.d("Keys", "schedulekey: " + schedulekey);
        Log.d("Keys", "adminkey: " + adminkey);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(databasename);
        reference.child(adminkey)
                .orderByChild("key")
                .equalTo(schedulekey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mytimeslot.clear();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                TimeSlot timeSlot = snapshot.getValue(TimeSlot.class);
                                if (timeSlot != null && timeSlot.getDate().equals(dateString)) {
                                    mytimeslot.add(timeSlot);
                                    Log.d("Keys", "timeSlot: " + timeSlot.getTime());
                                }
                            }
                            updateRecyclerView();
                        } else {
                            Log.d("TimeSlot", "No data found for the given schedule key and admin key.");
                            mytimeslot.clear();
                            updateRecyclerView();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("TimeSlot", "Error fetching data: " + databaseError.getMessage());
                    }
                });
    }

    private void updateRecyclerView() {
        if (timeAdapter == null) {
            timeAdapter = new Time_viewadapter(mytimeslot);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            time_viewrecycler.setLayoutManager(gridLayoutManager);
            time_viewrecycler.setAdapter(timeAdapter);
        } else {
            timeAdapter.updateSchedules(mytimeslot);
        }
    }

    private void clearalldialog() {
        new AlertDialog.Builder(Timeview_update.this)
                .setTitle("Clear Schedule")
                .setMessage("Are you sure you want to clear all time  time slot: "+nametitle)
                .setPositiveButton("Yes", (dialog, which) -> clearall())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearall() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasename).child(adminkey);
        databaseReference.removeValue()
         .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "All time slot cleared!", Toast.LENGTH_SHORT).show();
                timeAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to clear  time slot.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initdateformat() {
        String formattedDateString = "";

        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date = originalFormat.parse(dateString);
            SimpleDateFormat targetFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            formattedDateString = targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            formattedDateString = dateString;
        }
        dateschedule.setText("Date schedule: "+formattedDateString);
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

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.getLayout().postDelayed(() -> {
            boolean isRefreshSuccessful = fetchDataFromSource();
            if (isRefreshSuccessful) {
                refreshLayout.finishRefresh();
            } else {
                refreshLayout.finishRefresh(false);
            }
        }, 100);
    }

    private boolean fetchDataFromSource() {
        try {
            show_timeframe(key,adminkey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
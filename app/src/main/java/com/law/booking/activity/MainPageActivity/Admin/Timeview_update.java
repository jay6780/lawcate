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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.TimeSlot;
import com.law.booking.activity.tools.adapter.Time_viewadapter;
import com.law.booking.activity.tools.adapter.empty_schedule;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Timeview_update extends AppCompatActivity  implements OnRefreshListener {
    private RecyclerView time_recycler;
    private String key,dateString,adminkey;
    private List<TimeSlot> mytimeslot = new ArrayList<>();
    private TextView title,dateschedule;
    private ImageView back_btn,clear;
    private Time_viewadapter timeAdapter;
    private SmartRefreshLayout refreshLayout;
    private MaterialSpinner database_spinner;
    private String databasename;
    private String schedulename;
    private empty_schedule emptyAdapter;
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
        time_recycler = findViewById(R.id.time_recycler);
        dateschedule = findViewById(R.id.dateschedule);
        back_btn = findViewById(R.id.back_btn);
        clear = findViewById(R.id.clear);
        title = findViewById(R.id.title);
        title.setText("Schedule view");
        database_spinner = findViewById(R.id.database_spinner);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableRefresh(true);
        title.setTextSize(16);
        initdateformat();
        clear.setVisibility(View.VISIBLE);
        clear.setOnClickListener(view -> clearalldialog());
        back_btn.setOnClickListener(view -> onBackPressed());
        initTimeSpinner();
    }

    private void initTimeSpinner() {
        database_spinner.setItems("Select schedule","Morning schedule", "Afternoon schedule", "Evening schedule");
        database_spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                switch (position) {
                    case 1:
                        databasename = "Morning_slot";
                        show_timeframe(key,adminkey);
                        schedulename = item;
                        time_recycler.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        databasename = "Afternoon_slot";
                        schedulename = item;
                        show_timeframe(key,adminkey);
                        time_recycler.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        databasename = "Evening_slot";
                        show_timeframe(key,adminkey);
                        schedulename = item;
                        time_recycler.setVisibility(View.VISIBLE);
                        break;
                    default:
                        time_recycler.setVisibility(View.GONE);
                        databasename = null;
                }
            }
        });
    }


    private void show_timeframe(String schedulekey, String adminkey) {
        Log.d("Keys", "schedulekey: " + schedulekey);
        Log.d("Keys", "adminkey: " + adminkey);

        if(databasename == null){
            databasename = "Morning_slot";
        }
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
                        }
                        if (mytimeslot.isEmpty()) {
                            emptyAdapter = new empty_schedule(Timeview_update.this);
                            time_recycler.setLayoutManager(new LinearLayoutManager(Timeview_update.this, LinearLayoutManager.HORIZONTAL, false));
                            time_recycler.setAdapter(emptyAdapter);
                        } else {
                            timeAdapter = new Time_viewadapter(mytimeslot);
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(Timeview_update.this, 3);
                            time_recycler.setLayoutManager(gridLayoutManager);
                            time_recycler.setAdapter(timeAdapter);
                            timeAdapter.updateSchedules(mytimeslot);
                        }

                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("TimeSlot", "Error fetching data: " + databaseError.getMessage());
                    }
                });
    }


    private void clearalldialog() {
        new AlertDialog.Builder(Timeview_update.this)
                .setTitle("Clear Schedule")
                .setMessage("Are you sure you want to clear: "+schedulename)
                .setPositiveButton("Yes", (dialog, which) -> clearAll())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearAll() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.child("Mysched").child(userId).child(key).child("timeframe").removeValue();
        List<String> slots = Arrays.asList(databasename);

        // Loop through each slot and remove data
        for (String slot : slots) {
            databaseRef.child(slot).child(adminkey).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "All time cleared!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to clear time.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
package com.law.booking.activity.MainPageActivity.Admin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.TimeSlot;
import com.law.booking.activity.tools.Model.TimeSlotClickListener;
import com.law.booking.activity.tools.adapter.TimeSlotAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Timeframeactivity extends AppCompatActivity implements TimeSlotClickListener {
    ImageView back_btn;
    String userId, key;
    String TAG = "SetTimeFrame";
    Spinner spinner;
    RecyclerView timeSlotRecyclerView;
    TimeSlotAdapter timeSlotAdapter;
    TextView title;
    CharSequence[] timeSlots;
    private String timevalue;
    private AppCompatButton saved;
    private String databasename;
    private String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeframeactivity);
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        userId = getIntent().getStringExtra("userId");
        key = getIntent().getStringExtra("key");
        date = getIntent().getStringExtra("date");
        Log.d(TAG, "Id: " + userId);
        Log.d(TAG, "Key: " + key);

        back_btn = findViewById(R.id.back_btn);
        spinner = findViewById(R.id.slots);
        title = findViewById(R.id.title);
        saved = findViewById(R.id.saved);
        timeSlotRecyclerView = findViewById(R.id.time_slot_recycler_view);
        back_btn.setOnClickListener(view -> onBackPressed());

        timeSlots = new CharSequence[]{"Select slot", "Morning slot", "Afternoon slot", "Evening slot"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        timeSlotRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                List<String> timeSlotsList;
                if (position == 1) {
                    timevalue = "Morning slot";
                    databasename = "Morning_slot";
                    timeSlotsList = generateTimeSlots("AM", 6, 11);
                } else if (position == 2) {
                    databasename = "Afternoon_slot";
                    timevalue = "Afternoon slot";
                    timeSlotsList = generateTimeSlots("PM", 12, 17);
                } else if (position == 3) {
                    databasename = "Evening_slot";
                    timevalue = "Evening slot";
                    timeSlotsList = generateTimeSlots("PM", 6, 11);
                } else {
                    timeSlotsList = new ArrayList<>();
                }
                if (timeSlotAdapter != null) {
                    timeSlotAdapter.updateData(timeSlotsList);
                } else {
                    timeSlotAdapter = new TimeSlotAdapter(timeSlotsList, Timeframeactivity.this);
                    timeSlotRecyclerView.setAdapter(timeSlotAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private List<String> generateTimeSlots(String amPm, int startHour, int endHour) {
        List<String> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int hour = startHour; hour <= endHour; hour++) {
            for (int minute = 0; minute <= 59; minute++) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                String time = String.format("%d:%02d %s",
                        (hour > 12 ? hour - 12 : hour), minute, amPm);
                timeList.add(time);
            }
        }

        return timeList;
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
    public void onTimeSlotSelected(String time, boolean isSelected) {
        String timeslot = timevalue + ": " + time;
        title.setText(timeslot);
        if (isSelected) {
            saved.setVisibility(View.VISIBLE);
            saved.setOnClickListener(view -> showConfirmationDialog(time));
            Log.d("TimeSlot", "Selected time: " + time);
        } else {
            saved.setVisibility(View.GONE);
            Log.d("TimeSlot", "Deselected time: " + time);
        }
    }

    private void showConfirmationDialog(String time) {
        new AlertDialog.Builder(Timeframeactivity.this)
                .setTitle("Confirm Time Slot")
                .setMessage("Are you sure you want to save this time slot?")
                .setPositiveButton("Yes", (dialog, which) -> saveSelectedTimeSlotToFirebase(time))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void saveSelectedTimeSlotToFirebase(String time) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(databasename)
                .child(userId);
        String pushkey = myRef.push().getKey();
        TimeSlot timeSlot = new TimeSlot(time, userId, timevalue, key,date);
        myRef.child(pushkey).setValue(timeSlot)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(),"Time slot saved successfully",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Time slot saved successfully");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(),"Failed to save time slot: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed to save time slot: " + e.getMessage());
                });
    }
}

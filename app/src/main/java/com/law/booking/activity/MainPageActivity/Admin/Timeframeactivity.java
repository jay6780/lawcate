package com.law.booking.activity.MainPageActivity.Admin;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.TimeSlot;


public class Timeframeactivity extends AppCompatActivity {
    ImageView back_btn;
    String userId, key, TAG = "SetTimeFrame", timevalue, databasename, date;
    Spinner scheduleSpinner;
    TextView title;
    AppCompatButton saved;
    TimePicker timePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeframeactivity);
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        userId = getIntent().getStringExtra("userId");
        key = getIntent().getStringExtra("key");
        date = getIntent().getStringExtra("date");

        Log.d(TAG, "Id: " + userId);
        Log.d(TAG, "Key: " + key);
        timePicker = findViewById(R.id.time_picker);
        back_btn = findViewById(R.id.back_btn);
        scheduleSpinner = findViewById(R.id.slots);
        title = findViewById(R.id.title);
        saved = findViewById(R.id.saved);

        back_btn.setOnClickListener(view -> onBackPressed());

        // First Spinner - Select schedule
        CharSequence[] schedules = {"Select schedule", "Morning schedule", "Afternoon schedule", "Evening schedule"};
        ArrayAdapter<CharSequence> scheduleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schedules);
        scheduleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(scheduleAdapter);

        scheduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        timevalue = "Morning schedule";
                        databasename = "Morning_slot";
                        break;
                    case 2:
                        timevalue = "Afternoon schedule";
                        databasename = "Afternoon_slot";
                        break;
                    case 3:
                        timevalue = "Evening schedule";
                        databasename = "Evening_slot";
                        break;
                    default:
                        timevalue = null;
                        databasename = null;
                        saved.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        timePicker.setIs24HourView(false); // Use 12-hour format

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            String amPm = (hourOfDay >= 12) ? "PM" : "AM";
            int displayHour = (hourOfDay > 12) ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
            String selectedTime = String.format("%d:%02d %s", displayHour, minute, amPm);

            if (timevalue != null) {
                title.setText(timevalue + ": " + selectedTime);
                saved.setVisibility(View.VISIBLE);
                saved.setOnClickListener(view1 -> showConfirmationDialog(selectedTime));
            }
        });

    }


    private void showConfirmationDialog(String time) {
        new AlertDialog.Builder(Timeframeactivity.this)
                .setTitle("Save data")
                .setMessage("Are you sure you want to save this time schedule?")
                .setPositiveButton("Yes", (dialog, which) -> saveSelectedTimeSlotToFirebase(time))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void saveSelectedTimeSlotToFirebase(String time) {
        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference(databasename)
                .child(userId);

        String pushKey = myRef.push().getKey();
        DatabaseReference hmuaref = FirebaseDatabase.getInstance()
                .getReference()
                .child("Mysched")
                .child(userId)
                .child(key)
                .child("timeframe");
        hmuaref.setValue(date);

        TimeSlot timeSlot = new TimeSlot(time, userId, timevalue, key, date, pushKey);
        myRef.child(pushKey).setValue(timeSlot)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Time slot saved successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Time slot saved successfully");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to save time slot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed to save time slot: " + e.getMessage());
                });
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

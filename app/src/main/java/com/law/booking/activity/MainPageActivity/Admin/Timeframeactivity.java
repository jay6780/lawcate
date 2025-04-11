package com.law.booking.activity.MainPageActivity.Admin;


import android.os.Build;
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

import androidx.annotation.RequiresApi;
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
    @RequiresApi(api = Build.VERSION_CODES.M)
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
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        timevalue = "Morning schedule";
                        databasename = "Morning_slot";
                        timePicker.setHour(7);
                        timePicker.setMinute(0);
                        break;
                    case 2:
                        timevalue = "Afternoon schedule";
                        databasename = "Afternoon_slot";
                        timePicker.setHour(12);
                        timePicker.setMinute(0);
                        break;
                    case 3:
                        timevalue = "Evening schedule";
                        databasename = "Evening_slot";
                        timePicker.setHour(18);
                        timePicker.setMinute(0);
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


        timePicker.setIs24HourView(true);

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            boolean isValid = false;
            String amPm = (hourOfDay >= 12) ? "PM" : "AM";
            int displayHour = (hourOfDay > 12) ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
            String selectedTime = String.format("%d:%02d %s", displayHour, minute, amPm);

            if (timevalue != null) {
                int correctedHour = hourOfDay;
                int correctedMinute = minute;

                switch (timevalue) {
                    case "Morning schedule":
                        if (hourOfDay < 7) {
                            correctedHour = 7;
                            correctedMinute = 0;
                        } else if (hourOfDay >= 12) {
                            correctedHour = 11;
                            correctedMinute = 0;
                        }
                        isValid = (correctedHour >= 6 && correctedHour < 12);
                        break;

                    case "Afternoon schedule":
                        if (hourOfDay < 12) {
                            correctedHour = 12;
                            correctedMinute = 0;
                        } else if (hourOfDay >= 18) {
                            correctedHour = 18;
                            correctedMinute = 0;
                        }
                        isValid = (correctedHour >= 12 && correctedHour < 18);
                        break;

                    case "Evening schedule":
                        if (hourOfDay < 18) {
                            correctedHour = 18;
                            correctedMinute = 0;
                        } else if (hourOfDay > 23) {
                            correctedHour = 23;
                            correctedMinute = 0;
                        }
                        isValid = (correctedHour >= 18 && correctedHour <= 23);
                        break;

                }

                if (!isValid || hourOfDay != correctedHour || minute != correctedMinute) {
                    timePicker.setHour(correctedHour);
                    timePicker.setMinute(correctedMinute);
                    displayHour = (correctedHour > 12) ? correctedHour - 12 : (correctedHour == 0 ? 12 : correctedHour);
                    amPm = (correctedHour >= 12) ? "PM" : "AM";
                    selectedTime = String.format("%d:%02d %s", displayHour, correctedMinute, amPm);
                }

                title.setText(timevalue + ": " + selectedTime);
                saved.setVisibility(View.VISIBLE);
                String finalSelectedTime = selectedTime;
                saved.setOnClickListener(view1 -> showConfirmationDialog(finalSelectedTime));
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

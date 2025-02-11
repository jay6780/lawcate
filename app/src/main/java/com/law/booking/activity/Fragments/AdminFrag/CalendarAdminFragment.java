package com.law.booking.activity.Fragments.AdminFrag;


import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ak.ColoredDate;
import com.ak.KalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.Model.OnScheduleLongClickListener;
import com.law.booking.activity.tools.Model.Schedule;
import com.law.booking.activity.tools.Model.Schedule2;
import com.law.booking.activity.tools.Model.Schedule4;
import com.law.booking.activity.tools.adapter.ScheduleAdapter;
import com.law.booking.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarAdminFragment extends Fragment implements OnScheduleLongClickListener {
    private RecyclerView myschedule;
    private DatabaseReference adminRef, mySched;
    private List<Schedule2> scheduleList = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String userName, userImageUrl;
    private Set<Date> selectedDates = new HashSet<>();
    private List<ColoredDate> highlightedDates = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;
    private ImageView clear;
    private KalendarView mKalendarView;
    private AppCompatButton saved;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_calendar_admin, container, false);

        initUI(view);
        initFirebase();

        List<ColoredDate> datesColors = new ArrayList<>();
        datesColors.add(new ColoredDate(new Date(), getResources().getColor(R.color.red_holiday)));
        mKalendarView.setColoredDates(datesColors);

        fetchSchedules();

        return view;
    }

    private void initUI(View view) {
        saved = view.findViewById(R.id.saved);
        mKalendarView = view.findViewById(R.id.kalendar);
        myschedule = view.findViewById(R.id.myschedule);
        clear = view.findViewById(R.id.clear);
        clear.setVisibility(View.VISIBLE);
        scheduleAdapter = new ScheduleAdapter(scheduleList, this,getContext());
        myschedule.setLayoutManager(new LinearLayoutManager(getContext()));
        myschedule.setAdapter(scheduleAdapter);
        mKalendarView.setDateSelector(selectedDate -> handleDateSelection(selectedDate));
        clear.setOnClickListener(v -> showClearScheduleDialog());
        saved.setOnClickListener(click ->showSaveScheduleDialog());
    }

    private void fetchSchedules() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            mySched = FirebaseDatabase.getInstance().getReference("Mysched");
            mySched.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    scheduleList.clear();
                    highlightedDates.clear();
                    List<Schedule2> tempScheduleList = new ArrayList<>();
                    for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                        Schedule schedule = scheduleSnapshot.getValue(Schedule.class);
                        if (schedule != null) {
                            String key = scheduleSnapshot.getKey();
                            tempScheduleList.add(new Schedule2(schedule.getName(), schedule.getImageUrl(), schedule.getDate(), key, schedule.getTime()));
                        }
                    }
                    Collections.sort(tempScheduleList, new Comparator<Schedule2>() {
                        @Override
                        public int compare(Schedule2 s1, Schedule2 s2) {
                            return s1.getDate().compareTo(s2.getDate());
                        }
                    });
                    scheduleList.addAll(tempScheduleList);
                    for (Schedule2 schedule : scheduleList) {
                        highlightedDates.add(new ColoredDate(schedule.getDate(), getResources().getColor(R.color.red_holiday)));
                    }
                    mKalendarView.setColoredDates(highlightedDates);
                    scheduleAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("FirebaseData", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }


    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            adminRef = FirebaseDatabase.getInstance().getReference("Lawyer").child(userId);
            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userName = dataSnapshot.child("name").getValue(String.class);
                        userImageUrl = dataSnapshot.child("image").getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("FirebaseData", "Database error: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("FirebaseAuth", "No user is signed in.");
        }
    }

    private void handleDateSelection(Date selectedDate) {
        if (selectedDates.contains(selectedDate)) {
            selectedDates.remove(selectedDate);
            removeHighlightForDate(selectedDate);
        } else {
                selectedDates.add(selectedDate);
                addHighlightForDate(selectedDate);
                saved.setVisibility(View.VISIBLE);

        }

        updateHighlightedDates();
    }

    private void updateHighlightedDates() {
        highlightedDates.clear();
        for (Date date : selectedDates) {
            highlightedDates.add(new ColoredDate(date, getResources().getColor(R.color.red_holiday)));
        }
        mKalendarView.setColoredDates(highlightedDates);
    }

    private void addHighlightForDate(Date date) {
        highlightedDates.add(new ColoredDate(date, getResources().getColor(R.color.red_holiday)));
        mKalendarView.setColoredDates(highlightedDates);
    }

    private void removeHighlightForDate(Date date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            highlightedDates.removeIf(highlightedDate -> highlightedDate.getDate().equals(date));
        }
        mKalendarView.setColoredDates(highlightedDates);
    }

    private void showSaveScheduleDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Save Schedule")
                .setMessage("Do you want to save your schedule?")
                .setPositiveButton("Yes", (dialog, which) ->{
                    checkAndSaveSchedule();
                    saved.setVisibility(View.GONE);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    saved.setVisibility(View.GONE);
                    highlightedDates.clear();
                    selectedDates.clear();
                    mKalendarView.setColoredDates(highlightedDates);
                    fetchSchedules();
                })
                .show();
    }



    private void checkAndSaveSchedule() {
        saveScheduleToFirebase();
    }

    private void saveScheduleToFirebase() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference scheduleRef = FirebaseDatabase.getInstance().getReference("Mysched").child(userId);

            for (Date date : selectedDates) {
                String scheduleId = scheduleRef.push().getKey();
                if (scheduleId != null) {
                    Schedule4 schedule = new Schedule4(userName, userImageUrl, date,scheduleId,userId);
                    scheduleRef.child(scheduleId).setValue(schedule)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Schedule saved successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to save schedule.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }
    }

    private void showClearScheduleDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Clear Schedule")
                .setMessage("Are you sure you want to clear all scheduled dates?")
                .setPositiveButton("Yes", (dialog, which) -> clearSchedulesFromFirebase())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearSchedulesFromFirebase() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference scheduleRef = FirebaseDatabase.getInstance().getReference("Mysched").child(userId);
            DatabaseReference Morning_slot = FirebaseDatabase.getInstance().getReference("Morning_slot").child(userId);
            DatabaseReference Afternoon_slot = FirebaseDatabase.getInstance().getReference("Afternoon_slot").child(userId);
            DatabaseReference Evening_slot = FirebaseDatabase.getInstance().getReference("Afternoon_slot").child(userId);
            Morning_slot.removeValue();
            Afternoon_slot.removeValue();
            Evening_slot.removeValue();
            scheduleRef.removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "All scheduled dates cleared!", Toast.LENGTH_SHORT).show();
                            scheduleList.clear();
                            highlightedDates.clear();
                            selectedDates.clear();
                            mKalendarView.setColoredDates(highlightedDates);
                            scheduleAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Failed to clear scheduled dates.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onScheduleLongClick(Schedule2 schedule) {
        Dialog timepick = new Dialog();
        String key = schedule.getKey();
        timepick.timepicker(getActivity(), schedule.getTime(), userName, userImageUrl, key);
    }
}

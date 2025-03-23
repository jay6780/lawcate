package com.law.booking.activity.Fragments.UserFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ak.ColoredDate;
import com.ak.EventObjects;
import com.ak.KalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Schedule;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.ScheduleAdapterUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarUserFragment extends Fragment {
    private RecyclerView myschedule;
    private List<Schedule> scheduleList = new ArrayList<>();
    private ScheduleAdapterUser scheduleAdapter;
    private String key;
    KalendarView mKalendarView;
    private TextView artist_available;
    private FirebaseAuth auth;
    private ImageView dots;
    private Date dateselected;
    private String formattedDate;
    private String TAG = "CalendarUserFragment";
    private TextView provider_name, date_text, time_text, duration;
    int booknumber = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_scheduleview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        key = getArguments() != null ? getArguments().getString("key") : null;
        mKalendarView = view.findViewById(R.id.kalendar);
        myschedule = view.findViewById(R.id.myschedule);
        provider_name = view.findViewById(R.id.provider_name);
        date_text = view.findViewById(R.id.date);
        time_text = view.findViewById(R.id.time);
        artist_available = view.findViewById(R.id.artist_available);
        artist_available.setVisibility(View.VISIBLE);
        //initialized
        dots = view.findViewById(R.id.dots);
        dots.setVisibility(View.GONE);
        myschedule.setVisibility(View.VISIBLE);
        auth = FirebaseAuth.getInstance();
        List<ColoredDate> datesColors = new ArrayList<>();
        datesColors.add(new ColoredDate(new Date(), getResources().getColor(R.color.red_holiday)));
        mKalendarView.setColoredDates(datesColors);
        scheduleAdapter = new ScheduleAdapterUser(scheduleList, getContext());
        myschedule.setLayoutManager(new LinearLayoutManager(getContext()));
        myschedule.setAdapter(scheduleAdapter);
        List<EventObjects> events = new ArrayList<>();
        events.add(new EventObjects("meeting", new Date()));
        mKalendarView.setEvents(events);
        mKalendarView.setMonthChanger(changedMonth -> Log.d("Changed", "month changed " + changedMonth));
        mKalendarView.setDateSelector(new KalendarView.DateSelector() {
            @Override
            public void onDateClicked(Date selectedDate) {
                dateselected = selectedDate;
                SPUtils.getInstance().put(AppConstans.selectdate, String.valueOf(dateselected));
                fetchSchedules(selectedDate);
                scheduleAdapter.notifyDataSetChanged();
            }
        });
        initcalendarposition();


    }


    private void initcalendarposition() {
        Date firstDate = null;
        Date currentMonth = mKalendarView.getShowingMonth();
        if (firstDate != null && !currentMonth.equals(firstDate)) {
            mKalendarView.setMonth(currentMonth);
        }
    }

    private void fetchSchedules(Date dateselected) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(String.valueOf(dateselected));
            formattedDate = outputFormat.format(date);
            Log.d("Formatted Date", formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        FirebaseDatabase.getInstance().getReference("Mysched")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        scheduleList.clear();
                        for (DataSnapshot providerSnapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot scheduleSnapshot : providerSnapshot.getChildren()) {
                                Schedule schedule = scheduleSnapshot.getValue(Schedule.class);
                                if (schedule != null) {
                                    if (isSameDay(schedule.getDate(), dateselected)) {
                                        scheduleList.add(schedule);
                                    }
                                }
                            }
                        }
                        if (scheduleList.isEmpty()) {
                            scheduleAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "No schedules available for " + formattedDate, Toast.LENGTH_SHORT).show();
                        } else {
                            Collections.sort(scheduleList, new Comparator<Schedule>() {
                                @Override
                                public int compare(Schedule s1, Schedule s2) {
                                    return s1.getDate().compareTo(s2.getDate());
                                }
                            });
                            scheduleAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseData", "Database error: " + databaseError.getMessage());
                    }
                });
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

}
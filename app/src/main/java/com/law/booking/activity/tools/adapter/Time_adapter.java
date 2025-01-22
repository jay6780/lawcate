package com.law.booking.activity.tools.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.law.booking.R;
import com.law.booking.activity.tools.Model.TimeSlot;
import com.law.booking.activity.tools.Model.TimeSlotClickListener;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;

import java.util.List;

public class Time_adapter extends RecyclerView.Adapter<Time_adapter.ScheduleViewHolder> {
    private List<TimeSlot> timeSlots;
    private int selectedPosition = -1;
    private TimeSlotClickListener timeSlotClickListener;
    public Time_adapter(List<TimeSlot> timeSlots, TimeSlotClickListener timeSlotClickListener) {
        this.timeSlots = timeSlots;
        this.timeSlotClickListener = timeSlotClickListener;
    }

    // Method to update the schedule list
    public void updateSchedules(List<TimeSlot> newSchedules) {
        this.timeSlots = newSchedules;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_layout, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        TimeSlot schedule = timeSlots.get(position);
        holder.bind(schedule);
        String time = schedule.getTime();
        if (position == selectedPosition) {
            holder.linearlay.setBackgroundResource(R.drawable.circle_btn);
            holder.timeTextView.setTextColor(Color.parseColor("#FFFFFF"));
            if (timeSlotClickListener != null) {
                timeSlotClickListener.onTimeSlotSelected(time, true);
            }
        } else {
            holder.timeTextView.setTextColor(Color.parseColor("#000000"));
            holder.linearlay.setBackgroundResource(0);
            holder.linearlay.setBackgroundResource(0);
            if (timeSlotClickListener != null) {
                timeSlotClickListener.onTimeSlotSelected(time, false);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = (selectedPosition == position) ? -1 : position;
            Log.d("TimeOnclick","Click: "+time);
            SPUtils.getInstance().put(AppConstans.time,time);
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeTextView;
        private final LinearLayout linearlay;
        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            linearlay = itemView.findViewById(R.id.linearlay);
            timeTextView = itemView.findViewById(R.id.time);
        }

        public void bind(TimeSlot timeSlot) {
            timeTextView.setText(timeSlot.getTime());
        }
    }
}

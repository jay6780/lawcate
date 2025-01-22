package com.law.booking.activity.tools.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.law.booking.R;
import com.law.booking.activity.tools.Model.TimeSlotClickListener;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
    private List<String> timeSlotList;
    private int selectedPosition = -1;
    private TimeSlotClickListener timeSlotClickListener;

    public TimeSlotAdapter(List<String> timeSlotList, TimeSlotClickListener timeSlotClickListener) {
        this.timeSlotList = timeSlotList;
        this.timeSlotClickListener = timeSlotClickListener;
    }

    @Override
    public TimeSlotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimeSlotViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String time = timeSlotList.get(position);
        holder.timeTextView.setText(time);

        if (position == selectedPosition) {
            holder.linearlay.setBackgroundResource(R.drawable.circle_btn);
            holder.timeTextView.setTextColor(Color.parseColor("#FFFFFF"));
            if (timeSlotClickListener != null) {
                timeSlotClickListener.onTimeSlotSelected(time, true);
            }
        } else {
            holder.timeTextView.setTextColor(Color.parseColor("#000000"));
            holder.linearlay.setBackgroundResource(0);
            if (timeSlotClickListener != null) {
                timeSlotClickListener.onTimeSlotSelected(time, false);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = (selectedPosition == position) ? -1 : position;
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(position);
            if (timeSlotClickListener != null) {
                timeSlotClickListener.onTimeSlotSelected(time, selectedPosition == position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    public void updateData(List<String> newTimeSlotList) {
        this.timeSlotList = newTimeSlotList;
        notifyDataSetChanged();
    }

    public static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        LinearLayout linearlay;

        public TimeSlotViewHolder(View itemView) {
            super(itemView);
            linearlay = itemView.findViewById(R.id.linearlay);
            timeTextView = itemView.findViewById(R.id.time_text);
        }
    }
}

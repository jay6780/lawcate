package com.law.booking.activity.tools.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.law.booking.R;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.Model.OnScheduleLongClickListener;
import com.law.booking.activity.tools.Model.Schedule2;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private final List<Schedule2> schedules;
    private final OnScheduleLongClickListener longClickListener;
    private Context context;
    public ScheduleAdapter(List<Schedule2> schedules, OnScheduleLongClickListener longClickListener,Context context) {
        this.schedules = schedules;
        this.longClickListener = longClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule2 schedule = schedules.get(position);
        holder.bind(schedule);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showdialog(context,schedule);
            }
        });

        // Set the long click listener
//        holder.itemView.setOnLongClickListener(v -> {
//            if (longClickListener != null) {
//                longClickListener.onScheduleLongClick(schedule);
//                return true;
//            }
//            return false;
//        });
    }

    private void showdialog(Context context,Schedule2 schedule2) {
        new AlertDialog.Builder(context)
                .setTitle("Choose an action")
                .setItems(new String[]{"Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        deleteschedule(schedule2,context);
//                    } else if (which == 1) {
//                        String key = schedule2.getKey();
//                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                        Intent gototime = new Intent(context, Timeframeactivity.class);
//                        gototime.putExtra("key",key);
//                        gototime.putExtra("date",String.valueOf(schedule2.getDate()));
//                        gototime.putExtra("userId",userId);
//                        context.startActivity(gototime);
//                       if (context instanceof Activity){
//                           ((Activity) context).overridePendingTransition(0,0);
//                       }
//
//                    } else if (which == 2) {
//                        String key = schedule2.getKey();
//                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                        Intent gototime = new Intent(context, Timeview_update.class);
//                        gototime.putExtra("key",key);
//                        gototime.putExtra("date",String.valueOf(schedule2.getDate()));
//                        gototime.putExtra("userId",userId);
//                        context.startActivity(gototime);
//                        if (context instanceof Activity){
//                            ((Activity) context).overridePendingTransition(0,0);
//                        }
                    }



                })
                .show();
    }

    private void deleteschedule(Schedule2 schedule2,Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String key = schedule2.getKey();
        Log.d("deletekey","Deletekey: "+key);
        Dialog deletetime = new Dialog();
        deletetime.deletetime(context,userId,key);
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView dateTextView;
        private final ImageView myprofile;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.schedule_name);
            dateTextView = itemView.findViewById(R.id.schedule_date);
            myprofile = itemView.findViewById(R.id.myimage);
        }

        public void bind(Schedule2 schedule) {
            nameTextView.setText(schedule.getName());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(schedule.getDate());
            dateTextView.setText(formattedDate);
            Glide.with(itemView.getContext())
                    .load(schedule.getImageUrl())
                    .transform(new CircleCrop())
                    .into(myprofile);
        }
    }
}

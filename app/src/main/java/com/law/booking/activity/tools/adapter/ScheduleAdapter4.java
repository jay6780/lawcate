package com.law.booking.activity.tools.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.law.booking.activity.MainPageActivity.bookingUi.booknow;
import com.law.booking.activity.tools.Model.Schedule3;
import com.law.booking.R;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter4 extends RecyclerView.Adapter<ScheduleAdapter4.ScheduleViewHolder> {

    private final List<Schedule3> scheduleList;
    private final Context context;

    public ScheduleAdapter4(List<Schedule3> scheduleList, Context context) {
        this.scheduleList = scheduleList;
        this.context = context;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule3, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule3 schedule = scheduleList.get(position);
        holder.timeTextView.setText("Book now");
        String schedulekey = schedule.getKey();
        String date = String.valueOf(schedule.getDate());
        String userId = schedule.getUserId();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(schedule.getDate());
        holder.date.setText(formattedDate);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String price = SPUtils.getInstance().getString(AppConstans.pricetag);
                String heads = SPUtils.getInstance().getString(AppConstans.heads);
                String email = SPUtils.getInstance().getString(AppConstans.Adminemail);
                String image = SPUtils.getInstance().getString(AppConstans.AdminImage);
                String username = SPUtils.getInstance().getString(AppConstans.AdminUsername);
                String address = SPUtils.getInstance().getString(AppConstans.AdminAdress);
                String age = SPUtils.getInstance().getString(AppConstans.AdminAge);
                String phonenumber = SPUtils.getInstance().getString(AppConstans.AdminPhone);
                Intent intent = new Intent(context, booknow.class);
                intent.putExtra("email", email);
                intent.putExtra("username", username);
                intent.putExtra("image", image);
                intent.putExtra("address", address);
                intent.putExtra("age", age);
                intent.putExtra("lengthOfservice", "");
                intent.putExtra("key", userId);
                intent.putExtra("isOnline", false);
                intent.putExtra("serviceName", "");
                intent.putExtra("price", price);
                intent.putExtra("heads", heads);
                intent.putExtra("date", date);
                intent.putExtra("schedulekey",schedulekey);
                intent.putExtra("phonenumber", phonenumber);
                context.startActivity(intent);
                if(context instanceof Activity){
                    ((Activity)context).overridePendingTransition(0,0);
                    ((Activity)context).finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeTextView,date;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}

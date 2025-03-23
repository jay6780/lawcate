package com.law.booking.activity.tools.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.profile.providerProfile2;
import com.law.booking.activity.tools.Model.Schedule;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapterUser extends RecyclerView.Adapter<ScheduleAdapterUser.ScheduleViewHolder> {
    private final List<Schedule> schedules;
    private Context context;

    public ScheduleAdapterUser(List<Schedule> schedules, Context context) {
        this.context = context;
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_userview, parent, false);
        return new ScheduleViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule schedule = schedules.get(position);
        holder.bind(schedule);
        String userid = schedule.getUserId();
        Log.d("Timeframes", "userId: " + userid);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference hmua = FirebaseDatabase.getInstance().getReference("Lawyer").child(schedule.getUserId());
                hmua.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String email = snapshot.child("email").getValue(String.class);
                            String username = snapshot.child("username").getValue(String.class);
                            String image = snapshot.child("image").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);
                            String age = snapshot.child("age").getValue(String.class);
                            String lengthOfService = snapshot.child("lengthOfService").getValue(String.class);
                            Boolean isOnline = snapshot.child("online").getValue(boolean.class);
                            String description = snapshot.child("description").getValue(String.class);
                            Log.d("emails", "Email: " + email);
                            Intent intent = new Intent(context, providerProfile2.class);
                            intent.putExtra("email", email);
                            intent.putExtra("username", username);
                            intent.putExtra("image", image);
                            intent.putExtra("address", address);
                            intent.putExtra("age", age);
                            intent.putExtra("lengthOfservice", lengthOfService);
                            intent.putExtra("isOnline", isOnline);
                            intent.putExtra("description", description);
                            intent.putExtra("key", schedule.getUserId());
                            context.startActivity(intent);
                            return;

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }
        });




        if (schedule.getUserId() != null) {
            DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews").child(schedule.getUserId());
            reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalRating = 0;
                    int reviewCount = 0;
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        Integer rating = reviewSnapshot.child("rating").getValue(Integer.class);
                        if (rating != null) {
                            totalRating += rating;
                            reviewCount++;
                        }
                    }
                    if (reviewCount > 0) {
                        float averageRating = (float) totalRating / reviewCount;
                        holder.ratevalue.setText(String.format("%.1f", averageRating));
                    } else {
                        holder.ratevalue.setText("0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ProviderAdapter", "Error fetching reviews: " + error.getMessage());
                    holder.ratevalue.setText("0");
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void updateList(List<Schedule> newList) {
        schedules.clear();
        schedules.addAll(newList);
        notifyDataSetChanged();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final ImageView myprofile;
        private final TextView ratevalue,nameTextView;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            ratevalue = itemView.findViewById(R.id.ratevalue);
            nameTextView = itemView.findViewById(R.id.schedule_name);
            myprofile = itemView.findViewById(R.id.myimage);
        }

        public void bind(Schedule schedule) {
            nameTextView.setText(schedule.getName());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(schedule.getDate());
            Glide.with(itemView.getContext())
                    .load(schedule.getImageUrl())
                    .transform(new CircleCrop())
                    .into(myprofile);
        }
    }
}

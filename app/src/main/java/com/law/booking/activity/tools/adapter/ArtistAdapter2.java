package com.law.booking.activity.tools.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.profile.providerProfile3;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.beppi.tristatetogglebutton_library.TriStateToggleButton;

public class ArtistAdapter2 extends RecyclerView.Adapter<ArtistAdapter2.ProviderViewHolder> {
    private ArrayList<Usermodel> providerList;
    private Context context;
    private DatabaseReference userFavoritesRef;
    private FirebaseAuth mAuth;

    String currentUserId;
    public ArtistAdapter2(ArrayList<Usermodel> providerList, Context context) {
        this.providerList = providerList;
        this.context = context;
        this.userFavoritesRef = FirebaseDatabase.getInstance().getReference("favorites");
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_layout, parent, false);
        return new ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        Usermodel provider = providerList.get(position);
        String address = context.getString(R.string.address);
        String years = context.getString(R.string.years);
        holder.nameTextView.setText(provider.getUsername());


        if(SPUtils.getInstance().getBoolean(AppConstans.Administrator)){
            holder.appointment.setVisibility(View.GONE);
            holder.book_complete.setVisibility(View.GONE);
            holder.data.setVisibility(View.GONE);
            holder.star.setVisibility(View.GONE);
            holder.ratevalue.setVisibility(View.GONE);
            holder.heart.setVisibility(View.GONE);
            holder.verify_view.setVisibility(View.VISIBLE);
        }else{
            holder.book_complete.setVisibility(View.VISIBLE);
            holder.data.setVisibility(View.VISIBLE);
            holder.appointment.setVisibility(View.VISIBLE);
            holder.star.setVisibility(View.VISIBLE);
            holder.ratevalue.setVisibility(View.VISIBLE);
            holder.heart.setVisibility(View.VISIBLE);
            holder.verify_view.setVisibility(View.GONE);
        }


        Map<String, Object> updates = new HashMap<>();


        FirebaseDatabase.getInstance().getReference("Lawyer").child(provider.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isVerify = dataSnapshot.child("isVerify").getValue(Boolean.class);

                    if (isVerify != null && isVerify) {
                            Log.d("VerifyLaw", "isVerify" + isVerify);
                            holder.verifyToggle.setToggleStatus(isVerify);
                            holder.verify_value.setText("Verified");
                        }else{
                        Log.d("VerifyLaw", "isVerify" + isVerify);
                            holder.verify_value.setText("Not Verified");
                            holder.verifyToggle.setToggleStatus(isVerify);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });


        holder.verifyToggle.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {

                switch (toggleStatus) {
                    case off:
                    case on:
                        updates.put("isVerify", booleanToggleStatus);
                        savedLawsettings(updates,provider.getKey());
                        break;
                }
            }
        });


        if(provider.getLengthOfService() == null){
            holder.experience.setText("N/A");
        }else{
            holder.experience.setText(years+": "+provider.getLengthOfService());
        }
        String userId = provider.getKey();
        Log.d("userId", "UserId: " + userId);

        if (userId != null) {
            DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews").child(userId);
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

        if(provider.getAddress() == null){
            holder.address.setText("N/A");
        }else{
            holder.address.setText(address+": "+provider.getAddress());
        }

        currentUserId = mAuth.getCurrentUser().getUid();
        userFavoritesRef.child(currentUserId).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.heart.setImageResource(R.mipmap.heart_fullred);
                } else {
                    holder.heart.setImageResource(R.mipmap.heart_nobg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ArtistAdapter", "Error checking favorite status: " + error.getMessage());
            }
        });

        // Set heart icon click listener to toggle favorite status
        holder.heart.setOnClickListener(view -> {
            currentUserId = mAuth.getCurrentUser().getUid();
            userFavoritesRef.child(currentUserId).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Remove from favorites
                        userFavoritesRef.child(currentUserId).child(userId).removeValue();
                        holder.heart.setImageResource(R.mipmap.heart_nobg);
                    } else {
                        // Add to favorites
                        userFavoritesRef.child(currentUserId).child(userId).setValue(true);
                        holder.heart.setImageResource(R.mipmap.heart_fullred);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ArtistAdapter", "Error updating favorite status: " + error.getMessage());
                }
            });
        });


        holder.address.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(provider.getImage())
                .placeholder(R.mipmap.man)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);
        holder.appointment.setOnClickListener(view -> {
            openProfile(provider.getEmail(), provider.getUsername(), provider.getImage(),
                    provider.getAddress(), provider.getAge(), provider.getLengthOfService(),
                    provider.getKey(),provider.isOnline());
        });
    }

    private void savedLawsettings(Map<String, Object> updates,String key) {
        if (key != null) {
            DatabaseReference adminRef = FirebaseDatabase.getInstance()
                    .getReference("Lawyer")
                    .child(key);

            adminRef.updateChildren(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Data update!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Saveddatafromservice", "Failed to save data: " + e.getMessage());
                    });
        }
        notifyDataSetChanged();
    }


    public void updateList(ArrayList<Usermodel> newList) {
        providerList = newList;
        notifyDataSetChanged();
    }

    private void openProfile(String email, String username, String image, String address, String age, String lengthOfservice,
                             String key,boolean isOnline) {
        Intent intent = new Intent(context, providerProfile3.class);
        intent.putExtra("email", email);
        intent.putExtra("username", username);
        intent.putExtra("image", image);
        intent.putExtra("address", address);
        intent.putExtra("age", age);
        intent.putExtra("lengthOfservice", lengthOfservice);
        intent.putExtra("key", key);
        intent.putExtra("isOnline", isOnline);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    public class ProviderViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView,address,experience,ratevalue,verify_value,book_complete;
        ImageView imageView,heart,star;
        AppCompatButton appointment;
        LinearLayout verify_view;
        TriStateToggleButton verifyToggle;
        AppCompatButton data;
        public ProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            data = itemView.findViewById(R.id.data);
            book_complete = itemView.findViewById(R.id.book_complete);
            ratevalue = itemView.findViewById(R.id.ratevalue);
            verify_value = itemView.findViewById(R.id.verify_value);
            verify_view = itemView.findViewById(R.id.verify_view);
            verifyToggle = itemView.findViewById(R.id.verifyToggle);
            heart = itemView.findViewById(R.id.heart);
            star = itemView.findViewById(R.id.star);
            experience = itemView.findViewById(R.id.experience);
            address = itemView.findViewById(R.id.address);
            appointment = itemView.findViewById(R.id.appointment);
            nameTextView = itemView.findViewById(R.id.provider_name);
            imageView = itemView.findViewById(R.id.provider_image);
        }
    }
}

package com.law.booking.activity.tools.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.Provider.FullScreenImage_cover_user;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.youth.banner.adapter.BannerAdapter;

import java.util.ArrayList;
import java.util.List;

public class coverAdapter_user extends BannerAdapter<Service, coverAdapter_user.ServiceViewHolder> {
    private Context context;

    // Constructor
    public coverAdapter_user(List<Service> serviceList, Context context) {
        super(serviceList);
        this.context = context;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_profileview, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindView(@NonNull ServiceViewHolder holder, Service data, int position, int size) {
        String imageUrl = data.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageFromUrl(holder.imageView, imageUrl);

        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = SPUtils.getInstance().getString(AppConstans.KEY);
                DatabaseReference pictureref = FirebaseDatabase.getInstance().getReference("Cover_photo").child(key);
                pictureref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> imageUrls = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                            if (imageUrl != null) {
                                imageUrls.add(imageUrl);
                            }
                        }

                        if (!imageUrls.isEmpty()) {
                            Intent intent = new Intent(view.getContext(), FullScreenImage_cover_user.class);
                            intent.putStringArrayListExtra("image_list", new ArrayList<>(imageUrls));
                            intent.putExtra("position", 0);
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Error: " + error.getMessage());
                    }
                });
            }
        });




    }

    private void loadImageFromUrl(ImageView imageView, String imageUrl) {
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .into(imageView);
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.serviceImg);
        }
    }
}

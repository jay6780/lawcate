package com.law.booking.activity.tools.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.FullScreenImageActivity;
import com.law.booking.activity.tools.Model.Service;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class coverAdapter extends BannerAdapter<Service, coverAdapter.ServiceViewHolder> {
    private Context context;

    // Constructor
    public coverAdapter(List<Service> serviceList, Context context) {
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
        int drawableResId = 0;

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageFromUrl(holder.imageView, imageUrl);
        } else {
            drawableResId = loadDrawable(holder.imageView, data.getName());
        }

        holder.imageView.setOnClickListener(view -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("image_url", imageUrl);
                context.startActivity(intent);
            }
        });



    }

    private void loadImageFromUrl(ImageView imageView, String imageUrl) {
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .into(imageView);
    }

    private int loadDrawable(ImageView imageView, String serviceName) {
        int drawableResId = 0;
        if (serviceName.contains("Light Makeup")) {
            drawableResId = R.mipmap.imgesample1;
        } else if (serviceName.contains("Smokey-eye Makeup")) {
            drawableResId = R.mipmap.imagesample3;
        } else if (serviceName.contains("Wedding makeup")) {
            drawableResId = R.mipmap.imagesample4;
        } else if (serviceName.contains("Graduation light makeup look")) {
            drawableResId = R.mipmap.imagesample7;
        } else if (serviceName.contains("Service and events")) {
            drawableResId = R.mipmap.makeup;
        }
        Glide.with(imageView.getContext())
                .load(drawableResId)
                .into(imageView);

        return drawableResId; // Return the drawable resource ID
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.serviceImg);
        }
    }
}

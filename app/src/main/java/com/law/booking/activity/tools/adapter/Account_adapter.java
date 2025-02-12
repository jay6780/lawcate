package com.law.booking.activity.tools.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.law.booking.R;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.Model.Usermodel;

import java.util.ArrayList;

public class Account_adapter extends RecyclerView.Adapter<Account_adapter.ProviderViewHolder> {
    private ArrayList<Usermodel> providerList;
    private Context context;
    public Account_adapter(ArrayList<Usermodel> providerList, Context context) {
        this.providerList = providerList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_layout, parent, false);
        return new ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        Usermodel provider = providerList.get(position);
        holder.nameTextView.setText(provider.getUsername());
        Glide.with(context)
                .load(provider.getImage())
                .placeholder(R.mipmap.man)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Showdialog(context,provider.getImage(),provider.getKey(),provider.getEmail(),provider.getUsername());
            }
        });

    }

    private void Showdialog(Context context,String image,String key,String email,String username) {
        Dialog account_delete = new Dialog();
        account_delete.delete_user(context,image,key,email,username,"Lawyer");
    }

    public void updateList(ArrayList<Usermodel> newList) {
        providerList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    public class ProviderViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView imageView,delete;

        public ProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.delete);
            nameTextView = itemView.findViewById(R.id.provider_name);
            imageView = itemView.findViewById(R.id.provider_image);
        }
    }
}

package com.law.booking.activity.tools.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.R;

import java.util.ArrayList;

public class SetAdminadapter extends RecyclerView.Adapter<SetAdminadapter.ProviderViewHolder> {
    private ArrayList<Usermodel> providerList;
    private Context context;
    public SetAdminadapter(ArrayList<Usermodel> providerList, Context context) {
        this.providerList = providerList;
        this.context = context;
    }

    @NonNull
    @Override
    public SetAdminadapter.ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_setlayout, parent, false);
        return new SetAdminadapter.ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetAdminadapter.ProviderViewHolder holder, int position) {
        Usermodel provider = providerList.get(position);
        holder.provider_name.setText(provider.getName());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId.equals(provider.getKey())) {
            holder.cancel.setAlpha(0.5f);
            holder.cancel.setEnabled(false);
        }else{
            holder.cancel.setEnabled(true);
            holder.cancel.setAlpha(1f);
        }
        Glide.with(context)
                .load(provider.getImage())
                .placeholder(R.mipmap.man)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);
        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference("Lawyer")
                .child(provider.getKey());
        adminRef.child("isSuperAdmin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isSuperAdmin = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));

                if (isSuperAdmin) {
                    holder.setasAdmin.setVisibility(View.GONE);
                    holder.cancel.setVisibility(View.VISIBLE);
                } else {
                    holder.setasAdmin.setVisibility(View.VISIBLE);
                    holder.cancel.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SetAdminAdapter", "Failed to check admin status: " + error.getMessage());
            }
        });

        holder.setasAdmin.setOnClickListener(view -> {
            adminRef.child("isSuperAdmin").setValue(true)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Success set as Super Admin: " + provider.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SetAdminAdapter", "Failed to set as SuperAdmin: " + e.getMessage());
                    });


        });

        holder.cancel.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Removal");
                builder.setMessage("Are you sure you want to remove  Admin status?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    adminRef.child("isSuperAdmin").removeValue()
                            .addOnSuccessListener(unused -> {
                                Log.d("SetAdminAdapter", "Successfully removed SuperAdmin");
                                Toast.makeText(context, "Removed as Super Admin: " + provider.getName(), Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("SetAdminAdapter", "Failed to remove SuperAdmin: " + e.getMessage());
                                    });
                        });
                        builder.setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss(); // Close the dialog if the user cancels
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
        });

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
        TextView provider_name;
        ImageView imageView;
        AppCompatButton cancel,setasAdmin;

        public ProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            setasAdmin = itemView.findViewById(R.id.setAdmin);
            cancel = itemView.findViewById(R.id.remove_admin);
            provider_name = itemView.findViewById(R.id.provider_name);
            imageView = itemView.findViewById(R.id.provider_image);
        }
    }
}

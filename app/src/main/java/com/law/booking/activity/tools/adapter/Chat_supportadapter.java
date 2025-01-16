package com.law.booking.activity.tools.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.chat.chatActivity2;
import com.law.booking.activity.MainPageActivity.chat.chatActivity5;
import com.law.booking.activity.tools.Model.ChatRoom;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Chat_supportadapter extends RecyclerView.Adapter<Chat_supportadapter.ProviderViewHolder> {
    private ArrayList<Usermodel> providerList;
    private Context context;
    public Chat_supportadapter(ArrayList<Usermodel> providerList, Context context) {
        this.providerList = providerList;
        this.context = context;
    }

    @NonNull
    @Override
    public Chat_supportadapter.ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatsupport_item, parent, false);
        return new Chat_supportadapter.ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Chat_supportadapter.ProviderViewHolder holder, int position) {
        Usermodel provider = providerList.get(position);
        holder.provider_name.setText(provider.getUsername());

        holder.setasAdmin.setOnClickListener(view -> {
            checkAndCreateChatRoom(provider.getEmail(), provider.getUsername(), provider.getImage(), provider.isOnline(), provider.getKey(), provider.getAddress());
        });

        Glide.with(context)
                .load(provider.getImage())
                .placeholder(R.mipmap.man)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);


    }

    private void checkAndCreateChatRoom(String providerEmail, String providerName,String image,boolean isOnline,String key,String address) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();;
        String chatRoomId = createChatRoomId(currentUserEmail, providerEmail);

        FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Chat room does not exist, create it
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setUsers(Arrays.asList(currentUserEmail, providerEmail));
                    FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId).setValue(chatRoom)
                            .addOnSuccessListener(aVoid -> {
                                SPUtils.getInstance().put(AppConstans.address, address);
                                SPUtils.getInstance().put(AppConstans.providerName, providerName);
                                SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);
                                Intent intent = new Intent(context, chatActivity5.class);
                                intent.putExtra("chatRoomId", chatRoomId);
                                intent.putExtra("providerName", providerName);
                                intent.putExtra("providerEmail", providerEmail);
                                intent.putExtra("image",image);
                                intent.putExtra("isOnline",isOnline);
                                intent.putExtra("key",key);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                context.startActivity(intent);
                                if (context instanceof Activity) {
                                    ((Activity) context).finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to create chat room", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    SPUtils.getInstance().put(AppConstans.address, address);
                    SPUtils.getInstance().put(AppConstans.providerName, providerName);
                    SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);
                    Intent intent = new Intent(context, chatActivity5.class);
                    intent.putExtra("chatRoomId", chatRoomId);
                    intent.putExtra("providerName", providerName);
                    intent.putExtra("providerEmail", providerEmail);
                    intent.putExtra("image",image);
                    intent.putExtra("isOnline",isOnline);
                    intent.putExtra("key",key);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to check chat room", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String createChatRoomId(String email1, String email2) {
        String chatRoomId = email1.compareTo(email2) < 0
                ? email1.replace(".", "_") + "_" + email2.replace(".", "_")
                : email2.replace(".", "_") + "_" + email1.replace(".", "_");

        SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);

        return chatRoomId;
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

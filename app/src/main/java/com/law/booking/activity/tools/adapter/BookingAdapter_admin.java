package com.law.booking.activity.tools.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.Admin.Bookingmap_Admin;
import com.law.booking.activity.MainPageActivity.chat.chatActivity2;
import com.law.booking.activity.tools.Model.Booking;
import com.law.booking.activity.tools.Model.Booking2;
import com.law.booking.activity.tools.Model.ChatRoom;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BookingAdapter_admin extends RecyclerView.Adapter<BookingAdapter_admin.BookingViewHolder> {

    private List<Booking2> bookingList;
    private Context context;
    private String TAG ="BookAdapter";
    private String chatRoomId;
    private boolean isConfirmed;
    private boolean isCompleted;
    private DatabaseReference databaseReference,chatroomIds,book,
            Mybook,transactionMap,confirmed_client,confirmed_lawyer;
    private List<String> serviceNamesList;
    private  String adminUsername = SPUtils.getInstance().getString(AppConstans.AdminUsername);
    private  String adminImage = SPUtils.getInstance().getString(AppConstans.AdminImage);
    private  String adminPhone = SPUtils.getInstance().getString(AppConstans.AdminPhone);
    private  String adminEmail = SPUtils.getInstance().getString(AppConstans.Adminemail);
    private  String adminAge = SPUtils.getInstance().getString(AppConstans.AdminAge);
    private  String adminAddress = SPUtils.getInstance().getString(AppConstans.AdminAdress);
    public BookingAdapter_admin(List<Booking2> bookingList , Context context) {
        this.bookingList = bookingList;
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.chatroomIds = FirebaseDatabase.getInstance().getReference();
        this.confirmed_client = FirebaseDatabase.getInstance().getReference("Confirm_client");
        this.confirmed_lawyer = FirebaseDatabase.getInstance().getReference("Confirm_lawyer");
        this.book = FirebaseDatabase.getInstance().getReference("MybookUser");
        this.Mybook = FirebaseDatabase.getInstance().getReference("Mybook");
        this.transactionMap = FirebaseDatabase.getInstance().getReference("transactionMap");
        this.serviceNamesList = new ArrayList<>();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_adapter_admin, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking2 booking = bookingList.get(position);
        holder.nameTextView.setText(booking.getProviderName());
        holder.time.setText(booking.getTime());
        holder.date.setText(booking.getDate());

        holder.servicename.setText(booking.getServiceName());
        holder.price.setText(context.getString(R.string.price)+": " + booking.getPrice() + " php");

        if(booking.getLawType()!= null){
            holder.lawyer_typetxt.setTextColor(ContextCompat.getColor(context, R.color.light_blue));
            holder.lawyer_typetxt.setText(booking.getLawType());
        }else{
            holder.lawyer_typetxt.setVisibility(View.GONE);
        }
        Glide.with(holder.itemView.getContext())
                .load(booking.getImage())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.baseline_person_24)
                .into(holder.avatar);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap(booking.getEmail(),booking.getProviderName(),
                        booking.getImage(),booking.getKey());
            }
        });


        Log.d("COnfirmedkey","COnfirmedkey: "+booking.getKey());

        if(isCompleted){
            holder.confirmed.setVisibility(View.VISIBLE);
        }else{
            holder.cancel.setVisibility(View.VISIBLE);
        }


        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Complete book?")
                        .setMessage("Are you sure want to complete this book?")
                        .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isConfirmed = false;
                                ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setMessage("Complete booking....");
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String currentUserId = currentUser.getUid();
                                    String currentUserEmail = currentUser.getEmail();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Then, call checkIfUserIsGuess
                                            checkIfUserIsGuess(
                                                    currentUserId,
                                                    booking.getTime(),
                                                    booking.getHeads(),
                                                    booking.getPaymentMethod(),
                                                    booking.getServiceName(),
                                                    booking.getPrice(),
                                                    booking.getDate(),
                                                    booking.getAddress(),
                                                    booking.getEmail(),
                                                    currentUserEmail,
                                                    booking.getProviderName(),
                                                    booking.getImage(),
                                                    context,
                                                    booking.getKey(),
                                                    booking.getAge(),
                                                    booking.getLengthOfservice(),
                                                    booking.getPhonenumber(),
                                                    booking.getSnapshotkey(),
                                                    booking.getLawType()
                                            );
                                            progressDialog.dismiss();
                                        }
                                    }, 500); // Delay of 500 milliseconds to simulate processing time and allow UI to update
                                }
                            }
                        })
                        .setNegativeButton(context.getString(R.string.no), null)
                        .show();
            }
        });


        holder.confirmed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Cofirmed book?")
                        .setMessage("Are you sure want to confirm this book?")
                        .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isConfirmed = true;
                                ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setMessage("Confirm booking....");
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String currentUserId = currentUser.getUid();
                                    savedbookcounting(currentUserId);
                                    String currentUserEmail = currentUser.getEmail();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Then, call checkIfUserIsGuess
                                            checkIfUserIsGuess(
                                                    currentUserId,
                                                    booking.getTime(),
                                                    booking.getHeads(),
                                                    booking.getPaymentMethod(),
                                                    booking.getServiceName(),
                                                    booking.getPrice(),
                                                    booking.getDate(),
                                                    booking.getAddress(),
                                                    booking.getEmail(),
                                                    currentUserEmail,
                                                    booking.getProviderName(),
                                                    booking.getImage(),
                                                    context,
                                                    booking.getKey(),
                                                    booking.getAge(),
                                                    booking.getLengthOfservice(),
                                                    booking.getPhonenumber(),
                                                    booking.getSnapshotkey(),
                                                    booking.getLawType()
                                            );
                                            progressDialog.dismiss();
                                        }
                                    }, 500); // Delay of 500 milliseconds to simulate processing time and allow UI to update
                                }
                            }
                        })
                        .setNegativeButton(context.getString(R.string.no), null)
                        .show();
            }
        });
    }

    private void savedbookcounting(String key) {
        DatabaseReference hmuaref = FirebaseDatabase.getInstance()
                .getReference()
                .child("Lawyer")
                .child(key)
                .child("bookcomplete");
        hmuaref.get().addOnSuccessListener(snapshot -> {
            int currentCount = 0;
            if (snapshot.exists()) {
                currentCount = snapshot.getValue(Integer.class);
            }
            int newCount = currentCount + 1;
            hmuaref.setValue(newCount)
                    .addOnSuccessListener(aVoid ->
                            Log.d("FirebaseDB", "Count updated successfully to " + newCount))
                    .addOnFailureListener(e ->
                            Log.e("FirebaseDB", "Error updating count", e));
        }).addOnFailureListener(e ->
                Log.e("FirebaseDB", "Error fetching count", e));
    }

    private void openMap(String email,String name,String image,String key) {
        Log.d(TAG, "keyUser: " + key);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("maplink").child(key);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String mapUrl = dataSnapshot.child("mapurl").getValue(String.class);
                    if (mapUrl != null) {
                        Log.d(TAG, "Fetched map URL: " + mapUrl);
                        startMap(mapUrl,email,name,image,key);

                    } else {
                        Log.e(TAG, "mapurl is null in the snapshot.");
                    }
                } else {
                    Log.e(TAG, "No data found for the provided key.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }


    private void startMap(String mapurl,String email,String name,String image,String key) {
        if (mapurl != null && mapurl.startsWith("https://www.google.com/maps/?q=")) {
            String[] parts = mapurl.split("q=");
            if (parts.length > 1) {
                String latLng = parts[1];
                String[] latLngParts = latLng.split(",");
                if (latLngParts.length >= 2) {
                    try {
                        double latitude = Double.parseDouble(latLngParts[0]);
                        double longitude = Double.parseDouble(latLngParts[1]);

                        convertMapUrl(latitude,longitude);
                        String age = SPUtils.getInstance().getString(AppConstans.age);
                        Intent intent = new Intent(context, Bookingmap_Admin.class);
                        intent.putExtra("providerEmail", email);
                        intent.putExtra("image", image);
                        intent.putExtra("providerName", name);
                        intent.putExtra("age", age);
                        intent.putExtra("address", SPUtils.getInstance().getString(AppConstans.userAddress));
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("key", key);
                        context.startActivity(intent);

                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid coordinates", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Invalid map URL format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Invalid map URL format", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "This message does not contain a map link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertMapUrl(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        StringBuilder locationText = new StringBuilder();
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null) {
                    locationText.append(address.getLocality());
                }
                if (address.getAdminArea() != null) {
                    if (locationText.length() > 0) {
                        locationText.append(", ");
                    }
                    locationText.append(address.getAdminArea());
                }

                if (address.getCountryName() != null) {
                    if (locationText.length() > 0) {
                        locationText.append(", ");
                    }
                    locationText.append(address.getCountryName());
                }

            }
            SPUtils.getInstance().put(AppConstans.userAddress,String.valueOf(locationText));
        } catch (IOException e) {
            Log.e(TAG, "Error getting location details: ", e);
        }
    }

    private void checkIfUserIsGuess(String userId, String time, String heads, String cash, String serviceName, String price, String date, String address, String provideremail, String curruntUserEmail,
                                    String providerName, String image, Context context, String key, String age, String lengthOfservice, String phonenumber,String snapshotkey,String lawType) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Lawyer").child(userId);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
//                    String locationLink = "";
//                    if (address != null && !address.isEmpty()) {
//                        try {
//                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
//                            List<Address> addressList = geocoder.getFromLocationName(address, 1);
//                            if (addressList != null && !addressList.isEmpty()) {
//                                Address addr = addressList.get(0);
//                                double latitude = addr.getLatitude();
//                                double longitude = addr.getLongitude();
//                                locationLink = "https://www.google.com/maps/?q=" + latitude + "," + longitude;
//                            } else {
//                                Log.e(TAG, "No coordinates found for the address");
//                            }
//                        } catch (IOException e) {
//                            Log.e(TAG, "Geocoding failed", e);
//                        }
//                    }
                    String availedMessage = null;
                    if(isConfirmed){
                         availedMessage = "Hi, I'm " + username + "\n" +
                                "The book has been confirmed with:\n" +
                                 "law name select by client: "+serviceName+"\n"+
                                "Selected schedule: " + "time: " + time + "\n" +
                                "date: " + date + "\n" +
                                "Thank you!";
                    }else{
                        availedMessage = "Hi, I'm " + username + "\n" +
                                "The book has been complete with:\n" +
                                "law name select by client: "+serviceName+"\n"+
                                "Selected schedule: " + "time: " + time + "\n" +
                                "date: " + date + "\n" +
                                "Thank you!";
                    }


                    Log.d(TAG, availedMessage);
                    SPUtils.getInstance().put(AppConstans.availedMessage, availedMessage);
                    checkAndCreateChatRoom(provideremail, providerName, image, curruntUserEmail, context, address, key, availedMessage);
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    Booking booking = new Booking(providerName, serviceName, price, heads, phonenumber, date, time, image, address, provideremail, age, lengthOfservice, cash, key,timestamp,snapshotkey);
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Booking booking2 = new Booking(adminUsername, serviceName, price, heads, adminPhone, date, time, adminImage, adminAddress, adminEmail, adminAge, SPUtils.getInstance().getString(AppConstans.AdminLenght), cash, uid,timestamp,snapshotkey);
                    String chatRoomId = createChatRoomId(curruntUserEmail, provideremail);

                    if(isConfirmed) {
                        confirmed_lawyer.child(chatRoomId).child("bookInfo").child(snapshotkey).setValue(booking2)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d(TAG, "Cancel data saved successfully.");
                                    }
                                });
                        confirmed_client.child(chatRoomId).child("bookInfo").child(snapshotkey).setValue(booking)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Cancel data saved successfully.");
                                        book.child(chatRoomId).child("bookInfo").child(snapshotkey).removeValue();
                                        Mybook.child(chatRoomId).child("bookInfo").child(snapshotkey).removeValue();
                                        setConfirmed(chatRoomId,snapshotkey,lawType);
                                    }
                                });
                    }else{
                        DatabaseReference CompletebookArtist = FirebaseDatabase.getInstance().getReference("CompletebookLawyer");
                        CompletebookArtist.child(chatRoomId).child("bookInfo").child(snapshotkey).setValue(booking2)
                                .addOnCompleteListener(task3 -> {
                                    if (task3.isSuccessful()) {
                                        Log.d(TAG, "Cancel data saved successfully.");
                                    }
                                });
                        DatabaseReference complete =  FirebaseDatabase.getInstance().getReference("Completebook");
                        complete.child(chatRoomId).child("bookInfo").child(snapshotkey).setValue(booking)
                                .addOnCompleteListener(task4 -> {
                                    if (task4.isSuccessful()) {
                                        Log.d(TAG, "Cancel data saved successfully.");
                                        confirmed_client.child(chatRoomId).child("bookInfo").child(snapshotkey).removeValue();
                                        confirmed_lawyer.child(chatRoomId).child("bookInfo").child(snapshotkey).removeValue();
                                        transactionMap.child(chatRoomId).removeValue();
                                        decreasebookCount();
                                        setComplete(chatRoomId,snapshotkey,lawType);
                                    }
                                });
                    }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking Guess user: " + databaseError.getMessage());
            }
        });
    }

    private void setComplete(String chatRoomId, String snapshotkey, String lawType) {
        DatabaseReference CompletebookArtist = FirebaseDatabase.getInstance().getReference("CompletebookLawyer").child(chatRoomId).child("bookInfo").child(snapshotkey);
        DatabaseReference complete =  FirebaseDatabase.getInstance().getReference("Completebook").child(chatRoomId).child("bookInfo").child(snapshotkey);
        CompletebookArtist.child("lawType").setValue(lawType).addOnSuccessListener(aVoid ->
                        Log.d("Lawtype","push Success"))
                .addOnFailureListener(aVoid ->
                        Log.d("Lawtype","push Failed"));
        complete.child("lawType").setValue(lawType).addOnSuccessListener(aVoid ->
                        Log.d("Lawtype","push Success"))
                .addOnFailureListener(aVoid ->
                        Log.d("Lawtype","push Failed"));
    }

    private void setConfirmed(String chatRoomId, String snapshotkey, String lawType) {
        DatabaseReference lawtype2 = confirmed_client.child(chatRoomId).child("bookInfo").child(snapshotkey);
        DatabaseReference lawtype1 = confirmed_lawyer.child(chatRoomId).child("bookInfo").child(snapshotkey);
        lawtype2.child("lawType").setValue(lawType).addOnSuccessListener(aVoid ->
                        Log.d("Lawtype","push Success"))
                .addOnFailureListener(aVoid ->
                        Log.d("Lawtype","push Failed"));
        lawtype1.child("lawType").setValue(lawType).addOnSuccessListener(aVoid ->
                        Log.d("Lawtype","push Success"))
                .addOnFailureListener(aVoid ->
                        Log.d("Lawtype","push Failed"));

    }

    private void decreasebookCount() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference bookCountRef = databaseReference.child("BookCount").child(key);
        Log.d("decreaseCount: ","Decrease: "+key);
        bookCountRef.child("count").get().addOnSuccessListener(snapshot -> {
            int currentCount = 0;
            if (snapshot.exists()) {
                currentCount = snapshot.getValue(Integer.class);
            }

            int newCount = currentCount - 1;
            bookCountRef.child("count").setValue(newCount)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseDB", "Count updated successfully to " + newCount))
                    .addOnFailureListener(e -> Log.e("FirebaseDB", "Error updating count", e));
        }).addOnFailureListener(e -> Log.e("FirebaseDB", "Error fetching count", e));
    }

    private void checkAndCreateChatRoom(String provideremail, String providerName, String image, String curruntUserEmail,Context context,String address,String key,String availedmessage) {
        String chatRoomId = createChatRoomId(curruntUserEmail, provideremail);
        databaseReference.child("chatRooms").child(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setUsers(Arrays.asList(curruntUserEmail, provideremail));
                    chatroomIds.child("chatRooms").child(chatRoomId).setValue(chatRoom)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Complete Success", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, chatActivity2.class);
                                intent.putExtra("chatRoomId", chatRoomId);
                                intent.putExtra("providerName", providerName);
                                intent.putExtra("image", image);
                                intent.putExtra("providerEmail", provideremail);
                                intent.putExtra("address", address);
                                intent.putExtra("key", key);
                                intent.putExtra("cancelledmessage", availedmessage);
                                context.startActivity(intent);
                                if (context instanceof Activity) {
                                    ((Activity) context).finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to create chat room", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    String previousMessage  = SPUtils.getInstance().getString(AppConstans.lastmessage);
                    Intent intent = new Intent(context, chatActivity2.class);
                    intent.putExtra("chatRoomId", chatRoomId);
                    intent.putExtra("providerName", providerName);
                    intent.putExtra("image", image);
                    intent.putExtra("providerEmail", provideremail);
                    intent.putExtra("address", address);
                    intent.putExtra("key", key);
                    if (!availedmessage.equals(previousMessage)) {
                        intent.putExtra("cancelledmessage", availedmessage);
                        if(isConfirmed){
                            Toast.makeText(context, "Confirmed Success", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Complete Success", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(context,"Already cancelled",Toast.LENGTH_SHORT).show();
                    }
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
        chatRoomId = email1.compareTo(email2) < 0
                ? email1.replace(".", "_") + "_" + email2.replace(".", "_")
                : email2.replace(".", "_") + "_" + email1.replace(".", "_");

        SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);

        return chatRoomId;
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void updateview(boolean view) {
        isCompleted = view;
        Log.d("isCompleted", "isCompleted= "+isCompleted);
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView,time,date,servicename,price,lawyer_typetxt;
        ImageView avatar;
        AppCompatButton cancel,confirmed;
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            confirmed = itemView.findViewById(R.id.confirmed);
            lawyer_typetxt = itemView.findViewById(R.id.lawyer_typetxt);
            cancel = itemView.findViewById(R.id.cancel);
            servicename = itemView.findViewById(R.id.servicename);
            nameTextView = itemView.findViewById(R.id.username);
            date = itemView.findViewById(R.id.date);
            avatar = itemView.findViewById(R.id.provider_avatar);
            price = itemView.findViewById(R.id.price);
            time = itemView.findViewById(R.id.time);
        }
    }
}

package com.law.booking.activity.MainPageActivity.Guess;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.ak.ColoredDate;
import com.ak.KalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.bookingUi.Paymentreceipt;
import com.law.booking.activity.MainPageActivity.bookingUi.booknow;
import com.law.booking.activity.MainPageActivity.bookingUi.history_book;
import com.law.booking.activity.MainPageActivity.chat.chatActivity;
import com.law.booking.activity.tools.Model.ChatRoom;
import com.law.booking.activity.tools.Model.Schedule;
import com.law.booking.activity.tools.Model.Schedule2;
import com.law.booking.activity.tools.Service.MessageNotificationService;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Time_activity extends AppCompatActivity {
    private TextView profiletxt;
    private DatabaseReference Mysched,databaseReference;
    String key = SPUtils.getInstance().getString(AppConstans.providers);
    private ImageView bell,messageImg,back;
    private String image;
    private String email;
    private String providerName;
    private String address,adminkey,price,heads,phonenumber;
    private String serviceName;
    private String age,lengthOfservice;
    private String schedulekey;
    private String dateString;
    private boolean isOnline;
    private boolean hmua;
    private String TAG = "Time_activity";
    private KalendarView mKalendarView;
    private List<ColoredDate> highlightedDates = new ArrayList<>();
    private List<Schedule2> scheduleList = new ArrayList<>();
    String bookprovideremail = SPUtils.getInstance().getString(AppConstans.bookprovideremail);
    private String chatRoomId;
    private AppCompatButton proceed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time2);
        changeStatusBarColor(getResources().getColor(R.color.white2));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        profiletxt = findViewById(R.id.profiletxt);
        messageImg = findViewById(R.id.messageImg);
        proceed = findViewById(R.id.proceed);
        mKalendarView = findViewById(R.id.kalendar);
        bell = findViewById(R.id.bell);
        back = findViewById(R.id.back);
        profiletxt.setText("Date");

        SPUtils.getInstance().put(AppConstans.key, adminkey);
        isOnline = getIntent().getBooleanExtra("isOnline", false);
        serviceName = getIntent().getStringExtra("serviceName");
        Log.d(TAG, "Is Online: " + isOnline);
        SPUtils.getInstance().put(AppConstans.KEY, adminkey);
        Log.d("SavedKey", "userId: " + adminkey);
        image = getIntent().getStringExtra("image");
        providerName = getIntent().getStringExtra("username");
        SPUtils.getInstance().put(AppConstans.providerName,providerName);
        address = getIntent().getStringExtra("address");
        email = getIntent().getStringExtra("email");
        age = getIntent().getStringExtra("age");
        lengthOfservice = getIntent().getStringExtra("lengthOfservice");
        price = getIntent().getStringExtra("price");
        heads = getIntent().getStringExtra("heads");
        adminkey = getIntent().getStringExtra("key");
        dateString = getIntent().getStringExtra("date");
        schedulekey = getIntent().getStringExtra("schedulekey");
        hmua = getIntent().getBooleanExtra("hmua",false);
        Log.d("Schedulekey","keys: "+schedulekey);
        Log.d("Schedulekey","adminkey: "+adminkey);
        Log.d("Mydate","date: "+dateString);
        phonenumber = getIntent().getStringExtra("phonenumber");
        Log.d(TAG,"price: "+price);
        Log.d(TAG,"heads: "+heads);
        Log.d(TAG,"Phone: "+phonenumber);
        Log.d(TAG,"lengthOfservice: "+lengthOfservice);
        fetchSchedules();
        initShowbook();
        back.setOnClickListener(view -> onBackPressed());
        messageImg.setOnClickListener(view -> chat());
        SavedAta(chatRoomId,providerName,image,email,address,adminkey,isOnline,phonenumber,age);
        mKalendarView.setDateSelector(new KalendarView.DateSelector() {
            @Override
            public void onDateClicked(Date selectedDate) {
                handleDateSelection(selectedDate);
            }
        });

    }

    private void handleDateSelection(Date selectedDate) {
        Log.d(TAG, "Clicked date: " + selectedDate);
        boolean hasSchedule = false;
        for (Schedule2 schedule : scheduleList) {
            if (isSameDay(schedule.getDate(), selectedDate)) {
                proceed.setVisibility(View.VISIBLE);
                hasSchedule = true;
                String date = String.valueOf(schedule.getDate());
                String schedulekey = schedule.getKey();
                String timeframe = schedule.getTimeframe();
                String price = SPUtils.getInstance().getString(AppConstans.pricetag);
                String heads = SPUtils.getInstance().getString(AppConstans.heads);
                String email = SPUtils.getInstance().getString(AppConstans.providerEmail);
                String image = SPUtils.getInstance().getString(AppConstans.image);
                String username = SPUtils.getInstance().getString(AppConstans.providerName);
                String address = SPUtils.getInstance().getString(AppConstans.address);
                String age = SPUtils.getInstance().getString(AppConstans.age);
                String phonenumber = SPUtils.getInstance().getString(AppConstans.phone);
                String Servicename = SPUtils.getInstance().getString(AppConstans.servicename);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(schedule.getDate());

                proceed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(schedulekey == null){
                            return;
                        }
                        Intent intent = null;
                        if (timeframe == null) {
                            intent = new Intent(getApplicationContext(), Paymentreceipt.class);
                            intent.putExtra("email", email);
                            intent.putExtra("username", username);
                            intent.putExtra("image", image);
                            intent.putExtra("address", address);
                            intent.putExtra("age", age);
                            intent.putExtra("lengthOfservice", "");
                            intent.putExtra("key", key);
                            intent.putExtra("isOnline", false);
                            intent.putExtra("serviceName", Servicename);
                            intent.putExtra("price", price);
                            intent.putExtra("heads", heads);
                            intent.putExtra("date", formattedDate);
                            intent.putExtra("schedulekey", schedulekey);
                            intent.putExtra("phonenumber", phonenumber);
                            intent.putExtra("time", "Whole day");
                        } else {
                            intent = new Intent(getApplicationContext(), booknow.class);
                            intent.putExtra("email", email);
                            intent.putExtra("username", username);
                            intent.putExtra("image", image);
                            intent.putExtra("address", address);
                            intent.putExtra("age", age);
                            intent.putExtra("lengthOfservice", "");
                            intent.putExtra("key", key);
                            intent.putExtra("isOnline", false);
                            intent.putExtra("serviceName", Servicename);
                            intent.putExtra("price", price);
                            intent.putExtra("heads", heads);
                            intent.putExtra("date", date);
                            intent.putExtra("schedulekey", schedulekey);
                            intent.putExtra("phonenumber", phonenumber);
                        }
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();

                    }
                });
            }
        }

        if (!hasSchedule) {
            proceed.setVisibility(View.GONE);
            Log.d(TAG, "No schedule found for this date.");
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void chat() {
        checkAndCreateChatRoom(email,providerName,image);
    }

    private void checkAndCreateChatRoom(String providerEmail, String providerName,String image) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String chatRoomId = createChatRoomId(currentUserEmail, providerEmail);
        databaseReference.child("chatRooms").child(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setUsers(Arrays.asList(currentUserEmail, providerEmail));
                    databaseReference.child("chatRooms").child(chatRoomId).setValue(chatRoom)
                            .addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(getApplicationContext(), chatActivity.class);
                                intent.putExtra("chatRoomId", chatRoomId);
                                intent.putExtra("providerName", providerName);
                                intent.putExtra("image",image);
                                intent.putExtra("providerEmail",providerEmail);
                                intent.putExtra("address",address);
                                intent.putExtra("key",adminkey);
                                intent.putExtra("isOnline",isOnline);
                                SavedAta(chatRoomId,providerName,image,providerEmail,address,adminkey,isOnline,phonenumber,age);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Failed to create chat room", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Intent intent = new Intent(getApplicationContext(), chatActivity.class);
                    intent.putExtra("chatRoomId", chatRoomId);
                    intent.putExtra("providerName", providerName);
                    intent.putExtra("image",image);
                    intent.putExtra("providerEmail",providerEmail);
                    intent.putExtra("address",address);
                    intent.putExtra("key",adminkey);
                    intent.putExtra("isOnline",isOnline);
                    SavedAta(chatRoomId,providerName,image,providerEmail,address,adminkey,isOnline,phonenumber,age);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to check chat room", Toast.LENGTH_SHORT).show();
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

    private void SavedAta(String chatRoomId, String providerName, String image, String providerEmail, String address, String key, boolean isOnline,String Phone,String age) {
        SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);
        SPUtils.getInstance().put(AppConstans.providerName, providerName);
        SPUtils.getInstance().put(AppConstans.image, image);
        SPUtils.getInstance().put(AppConstans.providerEmail, providerEmail);
        SPUtils.getInstance().put(AppConstans.address, address);
        SPUtils.getInstance().put(AppConstans.key, key);
        SPUtils.getInstance().put(AppConstans.isOnline, isOnline);
        SPUtils.getInstance().put(AppConstans.phone, Phone);
        SPUtils.getInstance().put(AppConstans.age, age);
    }




    private void initShowbook() {
        startService(new Intent(this, MessageNotificationService.class));
        TextView badgeCount = findViewById(R.id.badge_count);
        String badgenum = SPUtils.getInstance().getString(AppConstans.booknum);
        if(badgenum.isEmpty() || badgenum.equals("null")){
            badgeCount.setText("0");
        }else{
            badgeCount.setVisibility(View.VISIBLE);
            badgeCount.setText(badgenum);
        }
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), history_book.class);
                intent.putExtra("bookprovideremail", bookprovideremail);
                startActivity(intent);
            }
        });

    }


    private void fetchSchedules() {
        Mysched = FirebaseDatabase.getInstance().getReference("Mysched").child(key);
        Mysched.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Schedule2> tempScheduleList = new ArrayList<>();
                for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                    Schedule schedule = scheduleSnapshot.getValue(Schedule.class);
                    if (schedule != null) {
                        String key = scheduleSnapshot.getKey();
                        tempScheduleList.add(new Schedule2(schedule.getName(), schedule.getImageUrl(), schedule.getDate(), key, schedule.getTime(),schedule.getTimeframe()));
                    }
                }
                scheduleList.addAll(tempScheduleList);
                for (Schedule2 schedule : scheduleList) {
                    try {
                        highlightedDates.add(new ColoredDate(schedule.getDate(), getResources().getColor(R.color.red_holiday)));
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }

                }
                mKalendarView.setColoredDates(highlightedDates);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebasedatabase", "Error fetching schedules: " + databaseError.getMessage());
            }
        });
    }


    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
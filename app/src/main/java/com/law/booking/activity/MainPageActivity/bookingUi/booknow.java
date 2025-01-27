package com.law.booking.activity.MainPageActivity.bookingUi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ak.ColoredDate;
import com.ak.EventObjects;
import com.ak.KalendarView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.chat.chatActivity;
import com.law.booking.activity.MainPageActivity.profile.providerProfile2;
import com.law.booking.activity.tools.Model.ChatRoom;
import com.law.booking.activity.tools.Model.Schedule;
import com.law.booking.activity.tools.Model.Schedule3;
import com.law.booking.activity.tools.Model.TimeSlot;
import com.law.booking.activity.tools.Model.TimeSlotClickListener;
import com.law.booking.activity.tools.Service.MessageNotificationService;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.ScheduleAdapter3;
import com.law.booking.activity.tools.adapter.Time_adapter;
import com.law.booking.activity.tools.adapter.empty_schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class booknow extends AppCompatActivity implements TimeSlotClickListener {
    private String chatRoomId;
    private String image;
    private String email;
    private String providerName;
    private String address,adminkey,price,heads,phonenumber;
    private String serviceName;
    private String age,lengthOfservice;
    private String schedulekey;
    private String dateString;
    private boolean isOnline;
    private String TAG = "Booknow";
    private TextView userAddress,userLenghtexp,name,profiletxt,morningslot,afternoon_slot,evening_slot;
    private ImageView profileimage,backBtn,bell,messageImg;
    private DatabaseReference databaseReference;
    private RecyclerView myschedule,afternoon_recycler,evening_recycler;
    private DatabaseReference adminRef,mySched;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String userName, userImageUrl;
    private List<Schedule3> scheduleList = new ArrayList<>();
    private List<ColoredDate> highlightedDates = new ArrayList<>();
    private Set<Date> selectedDates = new HashSet<>();
    private ScheduleAdapter3 scheduleAdapter;
    String bookprovideremail = SPUtils.getInstance().getString(AppConstans.bookprovideremail);
    AppCompatButton procced;
    KalendarView mKalendarView;
    private RelativeLayout relative_lay;
    private empty_schedule emptyAdapter;
    private List<TimeSlot> morningtimeslot = new ArrayList<>();
    private List<TimeSlot> afternoontimeslot = new ArrayList<>();
    private List<TimeSlot> eveningtimeslot = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booknow2);
        changeStatusBarColor(getResources().getColor(R.color.white2));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        SPUtils.getInstance().put(AppConstans.key, adminkey);
        isOnline = getIntent().getBooleanExtra("isOnline", false);
        serviceName = getIntent().getStringExtra("serviceName");
        Log.d(TAG, "Is Online: " + isOnline);
        SPUtils.getInstance().put(AppConstans.KEY, adminkey);
        Log.d("SavedKey", "userId: " + adminkey);
        image = getIntent().getStringExtra("image");
        providerName = getIntent().getStringExtra("username");
        address = getIntent().getStringExtra("address");
        email = getIntent().getStringExtra("email");
        age = getIntent().getStringExtra("age");
        lengthOfservice = getIntent().getStringExtra("lengthOfservice");
        price = getIntent().getStringExtra("price");
        heads = getIntent().getStringExtra("heads");
        adminkey = getIntent().getStringExtra("key");
        dateString = getIntent().getStringExtra("date");
        schedulekey = getIntent().getStringExtra("schedulekey");
        Log.d("Schedulekey","keys: "+schedulekey);
        Log.d("Schedulekey","adminkey: "+adminkey);
        Log.d("Mydate","date: "+dateString);
        phonenumber = getIntent().getStringExtra("phonenumber");
        Log.d(TAG,"price: "+price);
        Log.d(TAG,"heads: "+heads);
        Log.d(TAG,"Phone: "+phonenumber);
        initUi();
        initShowbook();
        show_timeframe(schedulekey,adminkey);
        messageImg.setOnClickListener(view -> chat());
    }

    private void chat() {
        checkAndCreateChatRoom(email,providerName,image);
    }


    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    private void initUi() {
        relative_lay = findViewById(R.id.relative_lay);
        evening_slot = findViewById(R.id.evening_slot);
        evening_recycler = findViewById(R.id.evening_recycler);
        afternoon_slot = findViewById(R.id.afternoon_slot);
        afternoon_recycler = findViewById(R.id.afternoon_recycler);
        morningslot = findViewById(R.id.morningslot);
        mKalendarView = findViewById(R.id.kalendar);
        messageImg = findViewById(R.id.messageImg);
        myschedule = findViewById(R.id.myschedule);
        profiletxt = findViewById(R.id.profiletxt);
        bell = findViewById(R.id.bell);
        profileimage = findViewById(R.id.avatar);
        procced = findViewById(R.id.proceed);
        name = findViewById(R.id.name);
        backBtn = findViewById(R.id.back);
        userAddress = findViewById(R.id.address);
        userLenghtexp = findViewById(R.id.lenghtofservice);
        backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(view -> onBackPressed());
        procced.setOnClickListener(view -> proccedIntent());
        Glide.with(this)
                .load(image)
                .transform(new CircleCrop())
                .error(R.mipmap.man)
                .into(profileimage);
        String location = getString(R.string.address);
        String names = getString(R.string.name);
        String taon = getString(R.string.yearss);
        userLenghtexp.setText((lengthOfservice != null ? lengthOfservice : "N/A")+" "+taon);
        Log.d(TAG, "Storing Provider Name: " + providerName);
        SPUtils.getInstance().put(AppConstans.providerName, providerName);
        Log.d(TAG, "Storing Email: " + email);
        SPUtils.getInstance().put(AppConstans.providers, adminkey);
        SPUtils.getInstance().put(AppConstans.email, email);
        name.setText(names+" " + (providerName != null ? providerName : "N/A"));
        userAddress.setText(location+": " + (address != null ? address : "N/A"));
        scheduleAdapter = new ScheduleAdapter3(scheduleList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myschedule.setLayoutManager(horizontalLayoutManager);
        myschedule.setAdapter(scheduleAdapter);
        List<ColoredDate> datesColors = new ArrayList<>();
        datesColors.add(new ColoredDate(new Date(), getResources().getColor(R.color.red_holiday)));
        mKalendarView.setColoredDates(datesColors);
        fetchSchedules();
        profiletxt.setText("Book process");
        List<EventObjects> events = new ArrayList<>();

        events.add(new EventObjects("meeting", new Date()));

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dateString);
            String formattedDate = outputFormat.format(date);
            SPUtils.getInstance().put(AppConstans.date,formattedDate);
            Log.d("Formatted Date", formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }




        mKalendarView.setEvents(events);
        mKalendarView.setDateSelector(new KalendarView.DateSelector() {
            @Override
            public void onDateClicked(Date selectedDate) {
                handleDateSelection(selectedDate);
            }
        });
        procced.setEnabled(false);
        procced.setAlpha(0.5f);



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


    private void proccedIntent() {
        if (scheduleList.isEmpty()) {
            Toast.makeText(booknow.this, "No schedule data available for this provider.", Toast.LENGTH_SHORT).show();
            SPUtils spUtils = SPUtils.getInstance();
            String price = AppConstans.pricetag;
            String heads = AppConstans.heads;
            String date = AppConstans.date;
            String time = AppConstans.time;
            spUtils.remove(price);
            spUtils.remove(heads);
            spUtils.remove(date);
            spUtils.remove(time);
            String mergemessageData = price+", "+heads+", "+date+", "+time;
            Log.d(TAG,"Data clear: "+ mergemessageData);
            return;
        }
        String price = SPUtils.getInstance().getString(AppConstans.pricetag);
        String heads = SPUtils.getInstance().getString(AppConstans.heads);
        String date = SPUtils.getInstance().getString(AppConstans.date);
        String time = SPUtils.getInstance().getString(AppConstans.time);
        String servicename = SPUtils.getInstance().getString(AppConstans.servicename);
        Intent intent = new Intent(getApplicationContext(), Paymentreceipt.class);
        intent.putExtra("email",email);
        intent.putExtra("username", providerName);
        intent.putExtra("image", image);
        intent.putExtra("address", address);
        intent.putExtra("age", age);
        intent.putExtra("lengthOfservice", lengthOfservice);
        intent.putExtra("key", adminkey);
        intent.putExtra("isOnline", isOnline);
        intent.putExtra("serviceName", servicename);
        intent.putExtra("price",price);
        intent.putExtra("heads",heads);
        intent.putExtra("phonenumber",phonenumber);
        intent.putExtra("date",date);
        intent.putExtra("time",time);
        startActivity(intent);
        overridePendingTransition(0,0);
        finish();
    }

    private void fetchSchedules() {
        FirebaseDatabase.getInstance().getReference("Mysched").child(adminkey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                scheduleList.clear();
                highlightedDates.clear();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                dateString = getIntent().getStringExtra("date");
                Date date = null;
                try {
                    date = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    Log.e("DateParsing", "Error parsing date: " + dateString, e);
                }
                for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                    Schedule schedule = scheduleSnapshot.getValue(Schedule.class);
                    if (schedule != null && date != null) {
                        scheduleList.add(new Schedule3(schedule.getName(), schedule.getImageUrl(), schedule.getDate(), adminkey, schedule.getTime()));
                        mKalendarView.moveToDate(date);
                        highlightedDates.add(new ColoredDate(date, getResources().getColor(R.color.red_holiday)));
                    }
                }
                mKalendarView.setColoredDates(highlightedDates);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void handleDateSelection(Date selectedDate) {
        boolean isHighlighted = false;
        for (ColoredDate coloredDate : highlightedDates) {
            if (isSameDay(coloredDate.getDate(), selectedDate)) {
                isHighlighted = true;
                break;
            }
        }
        if (!isHighlighted) {
            relative_lay.setVisibility(View.GONE);
            Toast.makeText(this, "Please select a date that is highlighted.", Toast.LENGTH_SHORT).show();
            return;
        }else{
            relative_lay.setVisibility(View.VISIBLE);
        }

        if (selectedDates.contains(selectedDate)) {
            selectedDates.remove(selectedDate);
        } else {
            selectedDates.add(selectedDate);
        }
    }

    private void show_timeframe(String schedulekey, String adminkey) {
        Log.d("Keys", "schedulekey: " + schedulekey);
        Log.d("Keys", "adminkey: " + adminkey);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Morning_slot");
        reference.child(adminkey)
                .orderByChild("key")
                .equalTo(schedulekey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                        Date date = null;
                        try {
                            date = dateFormat.parse(dateString); // Assuming dateString is a valid date string passed to this method
                        } catch (ParseException e) {
                            Log.e("DateParsing", "Error parsing date: " + dateString, e);
                        }
                        if (dataSnapshot.exists() && date != null) {
                            morningtimeslot.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                TimeSlot timeSlot = snapshot.getValue(TimeSlot.class);
                                if (timeSlot != null) {
                                    String fromfirebase = timeSlot.getDate();
                                    try {
                                        Date firebaseDate = dateFormat.parse(fromfirebase);
                                        Log.d("Mydate", "fromfirebase: " + firebaseDate);
                                        Log.d("Mydate", "fromdate: " + date);
                                        if (firebaseDate.equals(date)) {
                                            morningtimeslot.add(timeSlot);
                                            Log.d("Keys", "timeSlot: " + timeSlot.getTime());
                                        }
                                    } catch (ParseException e) {
                                        Log.e("DateParsing", "Error parsing fromfirebase: " + fromfirebase, e);
                                    }
                                }
                            }
                            if (morningtimeslot.isEmpty()){
                                morningslot.setVisibility(View.GONE);
                            }else{
                                morningslot.setVisibility(View.VISIBLE);
                            }
                            Time_adapter timeAdapter = new Time_adapter(morningtimeslot,booknow.this);
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
                            myschedule.setLayoutManager(gridLayoutManager);
                            myschedule.setAdapter(timeAdapter);
                        } else {
                            Log.d("TimeSlot", "No data found for the given schedule key and admin key.");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("TimeSlot", "Error fetching data: " + databaseError.getMessage());
                    }
                });
        show_afternoon(schedulekey,adminkey);
    }

    private void show_afternoon(String schedulekey, String adminkey) {
        Log.d("Keys", "schedulekey: " + schedulekey);
        Log.d("Keys", "adminkey: " + adminkey);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Afternoon_slot");
        reference.child(adminkey)
                .orderByChild("key")
                .equalTo(schedulekey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                        Date date = null;
                        try {
                            date = dateFormat.parse(dateString); // Assuming dateString is a valid date string passed to this method
                        } catch (ParseException e) {
                            Log.e("DateParsing", "Error parsing date: " + dateString, e);
                        }
                        if (dataSnapshot.exists() && date != null) {
                            afternoontimeslot.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                TimeSlot timeSlot = snapshot.getValue(TimeSlot.class);
                                if (timeSlot != null) {
                                    String fromfirebase = timeSlot.getDate();
                                    try {
                                        Date firebaseDate = dateFormat.parse(fromfirebase);
                                        Log.d("Mydate", "fromfirebase: " + firebaseDate);
                                        Log.d("Mydate", "fromdate: " + date);
                                        if (firebaseDate.equals(date)) {
                                            afternoontimeslot.add(timeSlot);
                                            Log.d("Keys", "timeSlot: " + timeSlot.getTime());
                                        }
                                    } catch (ParseException e) {
                                        Log.e("DateParsing", "Error parsing fromfirebase: " + fromfirebase, e);
                                    }
                                }
                            }
                            if (afternoontimeslot.isEmpty()){
                                afternoon_slot.setVisibility(View.GONE);
                            }else{
                                afternoon_slot.setVisibility(View.VISIBLE);
                            }
                            Time_adapter timeAdapter = new Time_adapter(afternoontimeslot,booknow.this);
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
                            afternoon_recycler.setLayoutManager(gridLayoutManager);
                            afternoon_recycler.setAdapter(timeAdapter);
                        } else {
                            Log.d("TimeSlot", "No data found for the given schedule key and admin key.");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("TimeSlot", "Error fetching data: " + databaseError.getMessage());
                    }
                });
        show_evening(schedulekey,adminkey);
    }

    private void show_evening(String schedulekey, String adminkey) {
        Log.d("Keys", "schedulekey: " + schedulekey);
        Log.d("Keys", "adminkey: " + adminkey);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Evening_slot");
        reference.child(adminkey)
                .orderByChild("key")
                .equalTo(schedulekey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                        Date date = null;
                        try {
                            date = dateFormat.parse(dateString); // Assuming dateString is a valid date string passed to this method
                        } catch (ParseException e) {
                            Log.e("DateParsing", "Error parsing date: " + dateString, e);
                        }
                        if (dataSnapshot.exists() && date != null) {
                            eveningtimeslot.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                TimeSlot timeSlot = snapshot.getValue(TimeSlot.class);
                                if (timeSlot != null) {
                                    String fromfirebase = timeSlot.getDate();
                                    try {
                                        Date firebaseDate = dateFormat.parse(fromfirebase);
                                        Log.d("Mydate", "fromfirebase: " + firebaseDate);
                                        Log.d("Mydate", "fromdate: " + date);
                                        if (firebaseDate.equals(date)) {
                                            eveningtimeslot.add(timeSlot);
                                            Log.d("Keys", "timeSlot: " + timeSlot.getTime());
                                        }
                                    } catch (ParseException e) {
                                        Log.e("DateParsing", "Error parsing fromfirebase: " + fromfirebase, e);
                                    }
                                }
                            }
                            if (eveningtimeslot.isEmpty()){
                                evening_slot.setVisibility(View.GONE);
                            }else{
                                evening_slot.setVisibility(View.VISIBLE);
                            }
                            Time_adapter timeAdapter = new Time_adapter(eveningtimeslot,booknow.this);
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
                            evening_recycler.setLayoutManager(gridLayoutManager);
                            evening_recycler.setAdapter(timeAdapter);
                        } else {
                            Log.d("TimeSlot", "No data found for the given schedule key and admin key.");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("TimeSlot", "Error fetching data: " + databaseError.getMessage());
                    }
                });
    }

    private boolean isSameDay(Date date1, Date date2) {
        return date1.getYear() == date2.getYear() &&
                date1.getMonth() == date2.getMonth() &&
                date1.getDate() == date2.getDate();
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
                                SavedAta(chatRoomId,providerName,image,providerEmail,address,adminkey,isOnline);
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
                    SavedAta(chatRoomId,providerName,image,providerEmail,address,adminkey,isOnline);
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

    private void SavedAta(String chatRoomId, String providerName, String image, String providerEmail, String address, String key, boolean isOnline) {
        SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);
        SPUtils.getInstance().put(AppConstans.providerName, providerName);
        SPUtils.getInstance().put(AppConstans.image, image);
        SPUtils.getInstance().put(AppConstans.providerEmail, providerEmail);
        SPUtils.getInstance().put(AppConstans.address, address);
        SPUtils.getInstance().put(AppConstans.key, key);
        SPUtils.getInstance().put(AppConstans.isOnline, isOnline);
    }

    public void onBackPressed(){
        Intent back = new Intent(getApplicationContext(), providerProfile2.class);
        back.putExtra("email", email);
        back.putExtra("username", providerName);
        back.putExtra("image", image);
        back.putExtra("address", address);
        back.putExtra("age", age);
        back.putExtra("lengthOfservice", SPUtils.getInstance().getString(AppConstans.servicelength));
        back.putExtra("key", adminkey);
        back.putExtra("isOnline", isOnline);
        startActivity(back);
        overridePendingTransition(0,0);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onTimeSlotSelected(String time, boolean isSelected) {
        if (isSelected) {
            procced.setEnabled(true);
            procced.setAlpha(1.0f);
        } else {
            procced.setEnabled(false);
            procced.setAlpha(0.5f);
        }
    }
}

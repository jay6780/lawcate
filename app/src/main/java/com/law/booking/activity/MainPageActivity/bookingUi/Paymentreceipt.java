package com.law.booking.activity.MainPageActivity.bookingUi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.chat.User_list;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.Model.Booking2;
import com.law.booking.activity.tools.Model.MychatId;
import com.law.booking.activity.tools.Model.Schedule;
import com.law.booking.activity.tools.Service.MessageNotificationService;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.orhanobut.dialogplus.DialogPlus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Paymentreceipt extends AppCompatActivity {
    private ImageView avatar, back,bell,msgbtn;
    private TextView name, servicename, headss, prices, datevalue,title;
    private TextView timevalue, phonumbervalue;
    private String image;
    private String email;
    private String providerName;
    private String address, key, price, heads, phonenumber;
    private String serviceName, date, time;
    private String addressadmin;
    private String age, lengthOfservice;
    private boolean isOnline;
    private Spinner payment_spinner;
    private String TAG = "Paymentreceipt";
    private AppCompatButton confirm;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference,databaseReference2;
    private DialogPlus loadingdilog;
    private String userName, userImageUrl,userAge,userEmail,userPhone,userAddress;
    String bookprovideremail = SPUtils.getInstance().getString(AppConstans.bookprovideremail);
    private TimePicker time_picker;
    private String lawType;
    private MaterialSpinner lawyer_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paymentreceipt);
        changeStatusBarColor(getResources().getColor(R.color.white2));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        key = getIntent().getStringExtra("key");
        SPUtils.getInstance().put(AppConstans.key, key);
        isOnline = getIntent().getBooleanExtra("isOnline", false);
        serviceName = getIntent().getStringExtra("serviceName");
        Log.d(TAG, "Is Online: " + isOnline);
        Log.d( "Payments",TAG);
        SPUtils.getInstance().put(AppConstans.KEY, key);
        Log.d("SavedKey", "userId: " + key);
        image = getIntent().getStringExtra("image");
        providerName = getIntent().getStringExtra("username");
        addressadmin = getIntent().getStringExtra("address");
        email = getIntent().getStringExtra("email");
        age = getIntent().getStringExtra("age");
        lengthOfservice = getIntent().getStringExtra("lengthOfservice");
        price = getIntent().getStringExtra("price");
        heads = getIntent().getStringExtra("heads");
        phonenumber = getIntent().getStringExtra("phonenumber");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        Log.d(TAG, "email: " + email);
        initView();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference2 = FirebaseDatabase.getInstance().getReference("MybookUser");
        databaseReference = FirebaseDatabase.getInstance().getReference("Mybook");
        initGuess();
        initShowbook();
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
        msgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), User_list.class);
                startActivity(intent);
            }
        });
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), history_book.class);
                intent.putExtra("bookprovideremail", bookprovideremail);
                startActivity(intent);
            }
        });

    }

    private void initGuess() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Client").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userName = dataSnapshot.child("username").getValue(String.class);
                    userImageUrl = dataSnapshot.child("image").getValue(String.class);
                    userAge = dataSnapshot.child("age").getValue(String.class);
                    userEmail = dataSnapshot.child("email").getValue(String.class);
                    userPhone = dataSnapshot.child("phone").getValue(String.class);
                    userAddress = dataSnapshot.child("address").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void initView() {
        lawyer_type = findViewById(R.id.lawyer_type);
        time_picker = findViewById(R.id.time_picker);
        avatar = findViewById(R.id.avatar);
        name = findViewById(R.id.name);
        msgbtn = findViewById(R.id.messageImg);
        bell = findViewById(R.id.bell);
        payment_spinner = findViewById(R.id.payment_spinner);
        servicename = findViewById(R.id.servicename);
        title = findViewById(R.id.profiletxt);
        headss = findViewById(R.id.heads);
        back = findViewById(R.id.back);
        prices = findViewById(R.id.price);
        datevalue = findViewById(R.id.datevalue);
        timevalue = findViewById(R.id.timevalue);
        phonumbervalue = findViewById(R.id.phonumbervalue);
        confirm = findViewById(R.id.confirm);

        Glide.with(this)
                .load(image)
                .transform(new CircleCrop())
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .into(avatar);

        name.setText(providerName);
        servicename.setText(serviceName);
        headss.setText(heads + " Heads");
        prices.setText(price +" php");
        datevalue.setText(date);
        timevalue.setText(time);
        title.setText("Confirmation");
        phonumbervalue.setText(phonenumber);
        back.setOnClickListener(view -> onBackPressed());
        confirm.setOnClickListener(view -> confirmation());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Select Payment", "Cash"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payment_spinner.setAdapter(adapter);
        payment_spinner.setSelection(0);
        initTime();
        lawType = "Select type";
        lawyer_type.setItems("Select type", "Legal advice and consultation", "Representation in court", "Document Preparation", "Dispute Resolution");
        lawyer_type.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                if(!item.equals("Select type")){
                    lawType = item;
                    Log.d("SelectedTYPE","TYPE: "+lawType);
                }
            }
        });

    }


    private void initTime() {
        time_picker.setIs24HourView(false);
        Log.d("Time","timevalue: "+time);
        if (time.equals("Whole day")) {
            timevalue.setText("0");
            time = "0";
            time_picker.setVisibility(View.VISIBLE);
            startSpinner();
        } else{
            timevalue.setText(time);
        }


    }
    private void startSpinner() {
        time_picker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            String amPm = (hourOfDay >= 12) ? "PM" : "AM";
            int displayHour = (hourOfDay > 12) ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
            String selectedTime = String.format("%d:%02d %s", displayHour, minute, amPm);

            if (time != null) {
                timevalue.setText(selectedTime);
                time = selectedTime;
            }else{
                time = "0";
            }
        });
    }

    private void confirmation() {
        String selectedPayment = "Cash";

        if(lawType.equals("Select type")){
            Toast.makeText(getApplicationContext(), "Please select correct law type", Toast.LENGTH_SHORT).show();
            return;
        }
        if(time.equals("0")){
            Toast.makeText(getApplicationContext(), "Select time first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPayment.equals("Select Payment")) {
            Toast.makeText(getApplicationContext(), "Please choose a payment method first", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingdilog = new Dialog().loadingDialog(Paymentreceipt.this);
        loadingdilog.show();
        String useremail = SPUtils.getInstance().getString(AppConstans.userEmail);
        String userId = firebaseAuth.getCurrentUser().getUid();
        if (useremail != null) {
            String childKey = createChatRoomId(useremail, email);
            SPUtils.getInstance().put(AppConstans.bookprovideremail, email);
            String snapshotkey = databaseReference.push().getKey();
            String timestamp = String.valueOf(System.currentTimeMillis());
            Booking2 booking = new Booking2(providerName, serviceName, price, heads, phonenumber, date, time, image, addressadmin, email, age, lengthOfservice, selectedPayment, key,snapshotkey,timestamp);
            Booking2 booking2 = new Booking2(userName, serviceName, price, heads, userPhone, date, time, userImageUrl, userAddress, useremail, userAge, "", selectedPayment, userId,snapshotkey,timestamp);
            // Add data to MybookUser
            DatabaseReference bookInfoRef2 = databaseReference2.child(childKey).child("bookInfo").child(snapshotkey);
            bookInfoRef2.setValue(booking2);


            // Add data to Mybook
            DatabaseReference bookInfoRef = databaseReference.child(childKey).child("bookInfo").child(snapshotkey);
            bookInfoRef.setValue(booking)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Booking data saved successfully.");
                            Toast.makeText(getApplicationContext(), "Book Success", Toast.LENGTH_SHORT).show();
                            new android.os.Handler().postDelayed(() -> {
                                if (loadingdilog != null && loadingdilog.isShowing()) {
                                    loadingdilog.dismiss();
                                }
                                savedSchedule(userId, key, date);
                                savedSchedId(email);
                                savedBookId(childKey,snapshotkey);
                                savedLaWType(childKey,snapshotkey,lawType);
                                savedBookCount(key);
                                savedbookcounting(key);
                                savedlaw_count(key);
                                savEBookIdforAdmin(childKey, key,snapshotkey);
                                Intent intent = new Intent(Paymentreceipt.this, history_book.class);
                                startActivity(intent);
                                finish();
                            }, 1500);
                        } else {
                            Log.e(TAG, "Failed to save booking data.", task.getException());
                            if (loadingdilog != null && loadingdilog.isShowing()) {
                                loadingdilog.dismiss();
                            }
                        }
                    });
        }
    }

    private void savedLaWType(String childKey, String snapshotkey,String type) {
        DatabaseReference lawtype2 = databaseReference2.child(childKey).child("bookInfo").child(snapshotkey);
        DatabaseReference lawtype1 = databaseReference.child(childKey).child("bookInfo").child(snapshotkey);
        lawtype2.child("lawType").setValue(type).addOnSuccessListener(aVoid ->
                        Log.d("Lawtype","push Success"))
                .addOnFailureListener(aVoid ->
                        Log.d("Lawtype","push Failed"));
        lawtype1.child("lawType").setValue(type).addOnSuccessListener(aVoid ->
                        Log.d("Lawtype","push Success"))
                .addOnFailureListener(aVoid ->
                        Log.d("Lawtype","push Failed"));
    }


    private void savedbookcounting(String key) {
        DatabaseReference hmuaref = FirebaseDatabase.getInstance()
                .getReference()
                .child("Lawyer")
                .child(key)
                .child("bookcount");
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

    private void savedlaw_count(String key) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Lawname")
                .child(key);

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String pushkey = ref.push().getKey();
        pushdata(ref, pushkey, timeStamp, serviceName, key, 1);
        incrementTotalBookingCount(key);
        }

    private void incrementTotalBookingCount(String lawyerId) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Lawyer")
                .child(lawyerId)
                .child("totalBookings");

        ref.get().addOnSuccessListener(snapshot -> {
            int currentCount = 0;
            if (snapshot.exists()) {
                currentCount = snapshot.getValue(Integer.class);
            }
            int finalCurrentCount = currentCount;
            ref.setValue(currentCount + 1)
                    .addOnSuccessListener(aVoid ->
                            Log.d("FirebaseDB", "Total booking count updated to " + (finalCurrentCount + 1)))
                    .addOnFailureListener(e ->
                            Log.e("FirebaseDB", "Error updating total booking count", e));
        }).addOnFailureListener(e ->
                Log.e("FirebaseDB", "Error fetching total booking count", e));
    }


    private void pushdata(DatabaseReference ref, String pushkey, String timeStamp,
                          String serviceName, String lawyerId, int count) {
        Map<String, Object> data = new HashMap<>();
        data.put("timeStamp", timeStamp);
        data.put("key", pushkey);
        data.put("serviceName", serviceName);
        data.put("count", count);
        data.put("lawyerId", lawyerId);

        ref.child(pushkey).setValue(data)
                .addOnSuccessListener(aVoid ->
                        Log.d("FirebaseDB", "Booking added with count = 1"))
                .addOnFailureListener(e ->
                        Log.e("FirebaseDB", "Error adding booking", e));
    }


    private void savEBookIdforAdmin(String chatId,String key,String snapshotkey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String time = String.valueOf(System.currentTimeMillis());
        Map<String ,String> dataId = new HashMap<>();
        dataId.put("snapshotkey",snapshotkey);
        dataId.put("time",time);
        dataId.put("chatId",chatId);
        dataId.put("key",key);
        databaseReference.child("BookIdAdmin").child(key).push().setValue(dataId)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseDB", "Data saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error saving data", e);
                });
    }

    private void savedBookCount(String key) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference bookCountRef = databaseReference.child("BookCount").child(key);
        bookCountRef.child("count").get().addOnSuccessListener(snapshot -> {
            int currentCount = 0;
            if (snapshot.exists()) {
                currentCount = snapshot.getValue(Integer.class);
            }

            int newCount = currentCount + 1;
            bookCountRef.child("count").setValue(newCount)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseDB", "Count updated successfully to " + newCount))
                    .addOnFailureListener(e -> Log.e("FirebaseDB", "Error updating count", e));
        }).addOnFailureListener(e -> Log.e("FirebaseDB", "Error fetching count", e));
    }

    private void savedBookId(String childKey,String snapshotkey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String time = String.valueOf(System.currentTimeMillis());
        Map<String , String> dataId = new HashMap<>();
        dataId.put("snapshotkey",snapshotkey);
        dataId.put("time",time);
        dataId.put("chatId",childKey);
        dataId.put("key",userId);
        databaseReference.child("BookingId").child(userId).push().setValue(dataId)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseDB", "Data saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error saving data", e);
                });
    }


    private void savedSchedId(String email) {
        String useremail = SPUtils.getInstance().getString(AppConstans.userEmail);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (useremail != null) {
            String childKey = createChatRoomId(email, useremail);
            MychatId mychatId = new MychatId(userId, childKey);
            databaseReference.child("MybookId").child(userId).setValue(mychatId)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseDB", "Data saved successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseDB", "Error saving data", e);
                    });
        }
    }


    private void savedSchedule(String userId, String key,String dateString) {
        String mergedUserIdKey = userId + "_" + key;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Invalid date format.", Toast.LENGTH_SHORT).show();
            return;
        }
        Schedule newSchedule = new Schedule(userName, userImageUrl,date);
        DatabaseReference scheduleRef = FirebaseDatabase.getInstance().getReference("UserSchedule").child(mergedUserIdKey);
        String scheduleId = scheduleRef.push().getKey();
        if (scheduleId != null) {
            scheduleRef.child(scheduleId).setValue(newSchedule)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Schedule saved successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to save schedule.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private String createChatRoomId(String email1, String email2) {
        String chatRoomId = email1.compareTo(email2) < 0
                ? email1.replace(".", "_") + "_" + email2.replace(".", "_")
                : email2.replace(".", "_") + "_" + email1.replace(".", "_");

        return chatRoomId;
    }



    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    public void onBackPressed(){
        finish();
        super.onBackPressed();
    }

}
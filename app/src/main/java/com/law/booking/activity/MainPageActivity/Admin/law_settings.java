package com.law.booking.activity.MainPageActivity.Admin;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Law_names;
import com.law.booking.activity.tools.adapter.SettingsAdapter_law;

import java.util.ArrayList;
import java.util.List;

public class law_settings extends AppCompatActivity {
    TextView username;
    ImageView back;
    RecyclerView law_recycler;
    SettingsAdapter_law settingsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));
        username = findViewById(R.id.username);
        back = findViewById(R.id.back);
        law_recycler = findViewById(R.id.law_recycler);
        username.setText("Law settings");
        back.setOnClickListener(view -> onBackPressed());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initGuessData(userId);


    }


    private void initGuessData(String userId) {
        FirebaseDatabase.getInstance().getReference("Lawyer").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Law_names> button_list = new ArrayList<>();
                if (!dataSnapshot.exists()) {
                    return;
                }
                Boolean isCorporate = dataSnapshot.child("isCorporate").getValue(Boolean.class);
                Boolean isCriminal = dataSnapshot.child("isCriminal").getValue(Boolean.class);
                Boolean isFamily = dataSnapshot.child("isFamily").getValue(Boolean.class);
                Boolean isHumanRights = dataSnapshot.child("isHumanRights").getValue(Boolean.class);
                Boolean isTax = dataSnapshot.child("isTax").getValue(Boolean.class);
                Boolean isContract = dataSnapshot.child("isContract").getValue(Boolean.class);
                Boolean isOnsite_book = dataSnapshot.child("isOnsite_book").getValue(Boolean.class);
                Boolean isOnline_book = dataSnapshot.child("isOnline_book").getValue(Boolean.class);

                if(isCorporate !=null &&  isCriminal !=null &&
                        isFamily !=null &&  isHumanRights !=null
                        &&  isTax !=null &&isContract !=null &&isOnsite_book !=null &&isOnline_book !=null){
                    button_list.add(new Law_names("Corporate Law",isCorporate));
                    button_list.add(new Law_names("Criminal Law",isCriminal));
                    button_list.add(new Law_names("Family Law",isFamily));
                    button_list.add(new Law_names("Human Rights Law",isHumanRights));
                    button_list.add(new Law_names("Tax Law",isTax));
                    button_list.add(new Law_names("Contract Law",isContract));
                    button_list.add(new Law_names("Online",isOnline_book));
                    button_list.add(new Law_names("On site",isOnsite_book));
                }


                law_recycler.setLayoutManager(new LinearLayoutManager(law_settings.this));
                settingsAdapter = new SettingsAdapter_law(law_settings.this,button_list);
                law_recycler.setAdapter(settingsAdapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseData", "Database error: " + databaseError.getMessage());
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
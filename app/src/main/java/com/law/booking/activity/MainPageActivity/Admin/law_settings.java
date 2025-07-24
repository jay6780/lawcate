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
    private List<Law_names> button_list = new ArrayList<>();
    private List<String> law_names = new ArrayList<>();
    private List<Boolean> booleanList = new ArrayList<>();
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
        initRecycler();


    }

    private void initRecycler() {
        law_recycler.setLayoutManager(new LinearLayoutManager(law_settings.this));
        settingsAdapter = new SettingsAdapter_law(law_settings.this,button_list);
        law_recycler.setAdapter(settingsAdapter);

    }


    private void initGuessData(String userId) {
        Log.d("myUserId","value: "+userId);
        law_names.add("Corporate Law");
        law_names.add("Criminal Law");
        law_names.add("Family Law");
        law_names.add("Human Rights Law");
        law_names.add("Tax Law");
        law_names.add("Contract Law");
        law_names.add("Online");
        law_names.add("On site");

        FirebaseDatabase.getInstance().getReference("Lawyer").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isCorporate = dataSnapshot.child("isCorporate").getValue(Boolean.class);
                Boolean isCriminal = dataSnapshot.child("isCriminal").getValue(Boolean.class);
                Boolean isFamily = dataSnapshot.child("isFamily").getValue(Boolean.class);
                Boolean isHumanRights = dataSnapshot.child("isHumanRights").getValue(Boolean.class);
                Boolean isTax = dataSnapshot.child("isTax").getValue(Boolean.class);
                Boolean isContract = dataSnapshot.child("isContract").getValue(Boolean.class);
                Boolean isOnsite_book = dataSnapshot.child("isOnsite_book").getValue(Boolean.class);
                Boolean isOnline_book = dataSnapshot.child("isOnline_book").getValue(Boolean.class);

                booleanList.add(isCorporate);
                booleanList.add(isCriminal);
                booleanList.add(isFamily);
                booleanList.add(isHumanRights);
                booleanList.add(isTax);
                booleanList.add(isContract);
                booleanList.add(isOnsite_book);
                booleanList.add(isOnline_book);

                for (int i = 0; i < law_names.size(); i++) {
                    Boolean value = booleanList.get(i);
                    button_list.add(new Law_names(law_names.get(i), Boolean.TRUE.equals(value)));
                }

                settingsAdapter.notifyDataSetChanged();

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
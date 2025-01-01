package com.law.booking.activity.MainPageActivity.Admin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;

import java.util.HashMap;
import java.util.Map;

import it.beppi.tristatetogglebutton_library.TriStateToggleButton;

public class law_settings extends AppCompatActivity {
    TextView username;
    TextView isCriminalText,IsCorporateText,isFamilyText,isImmigrationText,isPropertyText;
    ImageView back;
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
        isCriminalText = findViewById(R.id.isCriminal);
        back = findViewById(R.id.back);
        IsCorporateText = findViewById(R.id.IsCorporate);
        isFamilyText = findViewById(R.id.isFamily);
        isImmigrationText = findViewById(R.id.isImmigration);
        isPropertyText = findViewById(R.id.isProperty);
        username.setText("Law settings");
        back.setOnClickListener(view -> onBackPressed());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initGuessData(userId);
    }


    private void initGuessData(String userId) {
        FirebaseDatabase.getInstance().getReference("Lawyer").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isCorporate = dataSnapshot.child("isCorporate").getValue(Boolean.class);
                    Boolean isCriminal = dataSnapshot.child("isCriminal").getValue(Boolean.class);
                    Boolean isFamily = dataSnapshot.child("isFamily").getValue(Boolean.class);
                    Boolean isImmigration = dataSnapshot.child("isImmigration").getValue(Boolean.class);
                    Boolean isProperty = dataSnapshot.child("isProperty").getValue(Boolean.class);
                    initSetValue(isCorporate,isCriminal,isFamily,isImmigration,isProperty);
                    IsCorporateText.setText("Corporate");
                    isCriminalText.setText("Criminal");
                    isFamilyText.setText("Family");
                    isImmigrationText.setText("Immigration");
                    isPropertyText.setText("Property");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void initSetValue(Boolean isCorporate, Boolean isCriminal, Boolean isFamily, Boolean isImmigration, Boolean isProperty) {
        TriStateToggleButton IsCorporate_toggle = (TriStateToggleButton) findViewById(R.id.IsCorporate_toggle);
        TriStateToggleButton isCriminal_toggle = (TriStateToggleButton) findViewById(R.id.isCriminal_toggle);
        TriStateToggleButton isFamily_toggle = (TriStateToggleButton) findViewById(R.id.isFamily_toggle);
        TriStateToggleButton isImmigration_toggle = (TriStateToggleButton) findViewById(R.id.isImmigration_toggle);
        TriStateToggleButton isProperty_toggle = (TriStateToggleButton) findViewById(R.id.isProperty_toggle);
        IsCorporate_toggle.setToggleStatus(isCorporate);
        isCriminal_toggle.setToggleStatus(isCriminal);
        isFamily_toggle.setToggleStatus(isFamily);
        isImmigration_toggle.setToggleStatus(isImmigration);
        isProperty_toggle.setToggleStatus(isProperty);
        Map<String, Object> updates = new HashMap<>();


        IsCorporate_toggle.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
                @Override
                public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {

                    switch (toggleStatus) {
                        case off:
                        case on:
                            updates.put("isCorporate", booleanToggleStatus);
                            savedLawsettings(updates);
                            break;
                    }

                }
            });

            isCriminal_toggle.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
                @Override
                public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                    switch (toggleStatus) {
                        case off:
                        case on:
                            updates.put("isCriminal", booleanToggleStatus);
                            savedLawsettings(updates);
                            break;
                    }
                }
            });

        isFamily_toggle.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off:
                    case on:
                        updates.put("isFamily", booleanToggleStatus);
                        savedLawsettings(updates);
                        break;
                }
            }
        });

        isImmigration_toggle.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off:
                    case on:
                        updates.put("isImmigration", booleanToggleStatus);
                        savedLawsettings(updates);
                        break;
                }
            }
        });


        isProperty_toggle.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off:
                    case on:
                        updates.put("isProperty", booleanToggleStatus);
                        savedLawsettings(updates);
                        break;
                }
            }
        });

    }


    private void savedLawsettings(Map<String, Object> updates) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference adminRef = FirebaseDatabase.getInstance()
                    .getReference("Lawyer")
                    .child(user.getUid());

            adminRef.updateChildren(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getApplicationContext(), "Data update!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Saveddatafromservice", "Failed to save data: " + e.getMessage());
                    });
        }
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
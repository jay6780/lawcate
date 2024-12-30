package com.law.booking.activity.MainPageActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.law.booking.R;
import com.law.booking.activity.tools.Service.MessageNotificationService;
import com.law.booking.activity.welcome;

public class Splash extends AppCompatActivity {
    private DatabaseReference guessRef, adminRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, MessageNotificationService.class));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        guessRef = FirebaseDatabase.getInstance().getReference("Client");
        adminRef = FirebaseDatabase.getInstance().getReference("Lawyer");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentUser != null) {
                    Intent intent = new Intent(Splash.this, newHome.class);
                    String userId = currentUser.getUid();
                    adminRef = adminRef.child(userId);
                    guessRef = guessRef.child(userId);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else {
                    Intent intent = new Intent(Splash.this, welcome.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                }

            }
        }, 1500);
    }
}

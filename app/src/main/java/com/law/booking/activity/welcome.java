package com.law.booking.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.law.booking.R;

import com.law.booking.activity.MainPageActivity.Guess.Createacc;

public class welcome extends AppCompatActivity {
    AppCompatButton createacc,login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        createacc = findViewById(R.id.signup);
        login = findViewById(R.id.signin);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), com.law.booking.activity.MainPageActivity.login.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }
        });
        createacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Createacc.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }
        });
    }
}
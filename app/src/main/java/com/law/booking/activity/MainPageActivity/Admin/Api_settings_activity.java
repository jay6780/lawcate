package com.law.booking.activity.MainPageActivity.Admin;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;

import java.util.HashMap;
import java.util.Map;

public class Api_settings_activity extends AppCompatActivity implements View.OnClickListener {
    private TextView username;
    private EditText magic_apiEditext,api_editText;
    private AppCompatButton btn_save;
    private  String apigoogle, magicApi;
    private ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_settings);
        getSupportActionBar().hide();
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));
        username = findViewById(R.id.username);
        magic_apiEditext = findViewById(R.id.magic_apiEditext);
        api_editText = findViewById(R.id.api_editText);
        btn_save = findViewById(R.id.btn_save);
        back = findViewById(R.id.back);
        btn_save.setOnClickListener(this);
        back.setOnClickListener(this);
        username.setText("Api settings");
        apigoogle = getIntent().getStringExtra("googleApi");
        magicApi = getIntent().getStringExtra("magicApi");
        if(!apigoogle.isEmpty() && !magicApi.isEmpty()){
            api_editText.setText(apigoogle);
            magic_apiEditext.setText(magicApi);
        }
    }


    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_save:
                apigoogle = api_editText.getText().toString().trim();
                magicApi = magic_apiEditext.getText().toString().trim();
                savedApi(apigoogle,magicApi);
                break;
            case R.id.back:
                onBackPressed();
                break;
        }
    }

    private void savedApi(String apigoogle, String magicApi) {
        if(apigoogle.isEmpty()){
            Toast.makeText(getApplicationContext(), "Google api can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(magicApi.isEmpty()){
            Toast.makeText(getApplicationContext(), "Magic api can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference apiRef = FirebaseDatabase.getInstance().getReference("Apikeys");
        String key = apiRef.push().getKey();
        Map<String,String> datapush = new HashMap();
        datapush.put("googleApi",apigoogle);
        datapush.put("magicApi",magicApi);
        datapush.put("timeStamp",timeStamp);

        apiRef.child(key).setValue(datapush).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                api_editText.setText("");
                magic_apiEditext.setText("");
                Toast.makeText(getApplicationContext(), "Api push success", Toast.LENGTH_SHORT).show();
                initRefreshkey();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Api push failed: "+e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initRefreshkey() {
        DatabaseReference apiRef = FirebaseDatabase.getInstance().getReference("Apikeys");
        apiRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String googleApi = dataSnapshot.child("googleApi").getValue(String.class);
                    String magicApi = dataSnapshot.child("magicApi").getValue(String.class);
                    if (googleApi != null && magicApi != null) {
                        SPUtils.getInstance().put(AppConstans.googleMapApi,googleApi);
                        SPUtils.getInstance().put(AppConstans.magicApi,magicApi);
                        api_editText.setText(googleApi);
                        magic_apiEditext.setText(magicApi);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
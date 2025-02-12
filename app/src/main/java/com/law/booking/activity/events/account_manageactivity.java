package com.law.booking.activity.events;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.adapter.Account_adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class account_manageactivity extends AppCompatActivity {
    private RecyclerView account_recycler;
    private Account_adapter account_adapter;
    private ArrayList<Usermodel> userlist;
    private ImageView back;
    private SearchView searchProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manageactivity);
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        intiView();
        initFirebase();
        setupSearchView();
    }

    private void setupSearchView() {
        searchProvider.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    account_adapter.updateList(userlist);
                } else {
                    filterProviderList(newText);
                }
                return true;
            }
        });
    }

    private void filterProviderList(String query) {
        ArrayList<Usermodel> filteredEventOrgList = new ArrayList<>();
        for (Usermodel eventOrg : userlist) {
            if (eventOrg.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredEventOrgList.add(eventOrg);
            }
        }
        account_adapter.updateList(filteredEventOrgList);
    }



    private void initFirebase() {
        account_recycler.setLayoutManager(new LinearLayoutManager(account_manageactivity.this));
        userlist = new ArrayList<>();
        account_adapter = new Account_adapter(userlist, account_manageactivity.this);
        account_recycler.setAdapter(account_adapter);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lawyer");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userlist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usermodel usermodel = snapshot.getValue(Usermodel.class);
                    usermodel.setKey(snapshot.getKey());
                    userlist.add(usermodel);

                }

                Collections.sort(userlist, new Comparator<Usermodel>() {
                    @Override
                    public int compare(Usermodel o1, Usermodel o2) {
                        return Integer.compare(o2.getBookcount(), o1.getBookcount());
                    }
                });

                account_adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("User_list", "DatabaseError: " + databaseError.getMessage());
            }
        });
    }

    private void intiView() {
        back = findViewById(R.id.back);
        account_recycler = findViewById(R.id.account_recycler);
        searchProvider = findViewById(R.id.search);
        back.setOnClickListener(view -> onBackPressed());
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
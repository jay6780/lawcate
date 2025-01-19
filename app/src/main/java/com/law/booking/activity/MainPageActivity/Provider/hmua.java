package com.law.booking.activity.MainPageActivity.Provider;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.BuildConfig;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.bookingUi.history_book;
import com.law.booking.activity.MainPageActivity.chat.User_list;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.ArtistAdapter;
import com.law.booking.activity.tools.adapter.emptyAdapter_package;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class hmua extends AppCompatActivity implements OnRefreshListener {
    RecyclerView providerRecycler;
    DatabaseReference databaseReference;
    ArrayList<Usermodel> providerList;
    ArtistAdapter providerAdapter;
    private TextView profiletxt;
    private ImageView gunting,messageImg,bell,settings;
    private SearchView userSearch;
    private SkeletonScreen skeletonScreen;
    private LinearLayout  ll_skeleton;
    private boolean isSkeletonShown = false;
    String bookprovideremail = SPUtils.getInstance().getString(AppConstans.bookprovideremail);
    private emptyAdapter_package empty;
    private boolean corporate,criminal,family,immigration,property,isAll;
    private String selectedLocation;
    private DialogPlus dialog;
    private SmartRefreshLayout refreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hmua_layout);
        providerRecycler = findViewById(R.id.provider);
        userSearch = findViewById(R.id.search);
        gunting = findViewById(R.id.gunting);
        bell = findViewById(R.id.bell);
        profiletxt = findViewById(R.id.profiletxt);
        refreshLayout = findViewById(R.id.refreshLayout);
        messageImg = findViewById(R.id.messageImg);
        ll_skeleton = findViewById(R.id.ll_skeleton);
        settings = findViewById(R.id.settings);
        providerRecycler.setLayoutManager(new LinearLayoutManager(this));
        databaseReference = FirebaseDatabase.getInstance().getReference("Lawyer");
        ll_skeleton.setVisibility(View.VISIBLE);
        providerList = new ArrayList<>();
        providerAdapter = new ArtistAdapter(providerList, this);
        empty = new emptyAdapter_package(this);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        corporate = intent.getBooleanExtra("isCorporate", false);
        criminal = intent.getBooleanExtra("isCriminal", false);
        family = intent.getBooleanExtra("isFamily", false);
        immigration = intent.getBooleanExtra("isImmigration", false);
        property = intent.getBooleanExtra("isProperty", false);
        isAll = intent.getBooleanExtra("isAll", false);
        settings.setOnClickListener(view -> filterBy());
        profiletxt.setText(title);
        initSkeleton();
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setupSearchView();
        initbadgeCount();
        gunting.setOnClickListener(view -> onBackPressed());
        messageImg.setOnClickListener(view -> intenttochat());
        bell.setOnClickListener(view -> intentToHistory());
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableRefresh(true);
        startfirebase();
    }

    private void startfirebase() {
        String filteredlocation = selectedLocation;
        String address = SPUtils.getInstance().getString(AppConstans.homeAddress);
        String streetAndCity = extractregion(address);
        if(streetAndCity == null){
            Toast.makeText(getApplicationContext(),"Please open location service",Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("Street","Streetval: "+streetAndCity);
        if(selectedLocation !=null){
            selectedLocation = filteredlocation;
        }else{
            selectedLocation = streetAndCity;
        }

        initfirebaseData(selectedLocation,dialog);
    }

    private String extractregion(String address) {
        if (address == null) return "";
        String[] parts = address.split(",");
        String region = parts.length > 1 ? parts[1].trim() : "";
        return region;
    }


    private void initbadgeCount() {
        TextView badgeCount = findViewById(R.id.badge_count);
        String badgenum = SPUtils.getInstance().getString(AppConstans.booknum);
        if(badgenum.isEmpty() || badgenum.equals("null")){
            badgeCount.setText("0");
        }else{
            badgeCount.setVisibility(View.VISIBLE);
            badgeCount.setText(badgenum);
        }
    }

    private void filterBy() {
     dialog = DialogPlus.newDialog(hmua.this)
                .setContentHolder(new ViewHolder(R.layout.choose_dialog))
                .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .create();
        View dialogView = dialog.getHolderView();
        TextView title = dialogView.findViewById(R.id.title);
        AppCompatButton okbtn = dialogView.findViewById(R.id.Ok);
        okbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        title.setText("Filter by");
        Spinner type = dialogView.findViewById(R.id.type_spinner);
        CharSequence[] options = {"Filter by", "Location"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(hmua.this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        fetchlocation(dialogView, "Select Location",dialog);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dialog.show();
    }
    private void fetchlocation(View dialogView, String defaultRegion,DialogPlus dialogPlus) {
        String provinceUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=province+in+Philippines&key=" + BuildConfig.mapApikey;
        String cityUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=city+in+Philippines&key=" + BuildConfig.mapApikey;
        OkHttpClient client = new OkHttpClient();
        Request provinceRequest = new Request.Builder()
                .url(provinceUrl)
                .build();

        Request cityRequest = new Request.Builder()
                .url(cityUrl)
                .build();

        client.newCall(provinceRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    List<String> provinces = parseRegions(jsonResponse);

                    client.newCall(cityRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if (response.isSuccessful() && response.body() != null) {
                                String jsonResponse = response.body().string();
                                List<String> cities = parseRegions(jsonResponse);
                                provinces.addAll(cities);
                                provinces.add(0, defaultRegion);

                                runOnUiThread(() -> {
                                    Spinner type = dialogView.findViewById(R.id.type_spinner);
                                    ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(hmua.this,
                                            android.R.layout.simple_spinner_item, provinces);
                                    regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    type.setAdapter(regionAdapter);

                                    type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            selectedLocation = provinces.get(position).trim().toLowerCase();
                                            if (!selectedLocation.equals("Select Location")) {
                                                initfirebaseData(selectedLocation,dialogPlus);
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {
                                        }
                                    });
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private List<String> parseRegions(String jsonResponse) {
        List<String> regions = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray results = jsonObject.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String regionName = result.getString("name");
                regions.add(regionName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return regions;
    }

    private void initfirebaseData(String selectedLocation, DialogPlus dialogPlus) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                providerList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usermodel usermodel = snapshot.getValue(Usermodel.class);
                    if (usermodel != null) {
                        usermodel.setKey(snapshot.getKey());
                        String address = usermodel.getAddress();
                        Boolean isCorporate = snapshot.child("isCorporate").getValue(Boolean.class);
                        Boolean isCriminal = snapshot.child("isCriminal").getValue(Boolean.class);
                        Boolean isFamily = snapshot.child("isFamily").getValue(Boolean.class);
                        Boolean isImmigration = snapshot.child("isImmigration").getValue(Boolean.class);
                        Boolean isProperty = snapshot.child("isProperty").getValue(Boolean.class);
                        Boolean isSuperAdmin = snapshot.child("isSuperAdmin").getValue(Boolean.class);
                        Boolean isVerify = snapshot.child("isVerify").getValue(Boolean.class);
                        boolean addressMatchesRegion = false;
                        if (address != null && !address.isEmpty()) {
                            String normalizedAddress = address.trim().toLowerCase();
                            String normalizedRegion = selectedLocation.trim().toLowerCase();
                            addressMatchesRegion = normalizedAddress.contains(normalizedRegion);
                        }

                        boolean matches = (corporate && isCorporate != null && isCorporate) ||
                                (criminal && isCriminal != null && isCriminal) ||
                                (family && isFamily != null && isFamily) ||
                                (immigration && isImmigration != null && isImmigration) ||
                                (property && isProperty != null && isProperty);

                        if (addressMatchesRegion && (matches || isAll) &&
                                (isSuperAdmin == null || !isSuperAdmin) &&
                                (isVerify == null || isVerify)) {
                            if (dialogPlus != null && dialogPlus.isShowing()) {
                                dialogPlus.dismiss();
                            }
                            providerList.add(usermodel);
                        }
                    }
                }

                Collections.sort(providerList, new Comparator<Usermodel>() {
                    @Override
                    public int compare(Usermodel o1, Usermodel o2) {
                        float rating1 = o1.getRatings();
                        float rating2 = o2.getRatings();
                        return Float.compare(rating2, rating1);
                    }
                });

                providerAdapter.notifyDataSetChanged();

                // Update RecyclerView based on the provider list
                if (providerList.isEmpty()) {
                    providerRecycler.setAdapter(empty);
                } else {
                    providerRecycler.setAdapter(providerAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(hmua.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                Log.e("User_list", "DatabaseError: " + databaseError.getMessage());
            }
        });
    }



    private void intentToHistory() {
        Intent intent = new Intent(getApplicationContext(), history_book.class);
        intent.putExtra("bookprovideremail", bookprovideremail);
        startActivity(intent);
    }

    private void intenttochat() {
        Intent chat = new Intent(getApplicationContext(), User_list.class);{
            startActivity(chat);
        }
    }

    private void initSkeleton() {
        skeletonScreen = Skeleton.bind(ll_skeleton)
                .load(R.layout.skeletonlayout_2)
                .duration(1000)
                .color(R.color.colorFontGreyDark)
                .angle(20)
                .show();
        new android.os.Handler().postDelayed(() -> {
            skeletonScreen.hide();
            providerRecycler.setVisibility(View.VISIBLE);
            ll_skeleton.setVisibility(View.GONE);
        }, 1000);
    }


    private void setupSearchView() {
        userSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showSkeleton();
                filterProviderList(newText);
                return true;
            }
        });
    }

    private void showSkeleton() {
        if (!isSkeletonShown) {
            skeletonScreen = Skeleton.bind(ll_skeleton)
                    .load(R.layout.skeletonlayout_2)
                    .duration(1000)
                    .color(R.color.colorFontGreyDark)
                    .angle(20)
                    .show();
            isSkeletonShown = true;
        }
    }
    private void hideSkeleton() {
        if (skeletonScreen != null && isSkeletonShown) {
            skeletonScreen.hide();
            isSkeletonShown = false;
        }
    }


    private void filterProviderList(String query) {
        ArrayList<Usermodel> filteredList = new ArrayList<>();
        for (Usermodel user : providerList) {
            if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        providerAdapter.updateList(filteredList);
        if (filteredList.isEmpty()) {
            hideSkeleton();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideSkeleton();
                }
            }, 1000);
        }
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

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.getLayout().postDelayed(() -> {
            boolean isRefreshSuccessful = fetchDataFromSource();
            if (isRefreshSuccessful) {
                refreshLayout.finishRefresh();
            } else {
                refreshLayout.finishRefresh(false);
            }
        }, 100);
    }

    private boolean fetchDataFromSource() {
        try {
            String address = SPUtils.getInstance().getString(AppConstans.homeAddress);
            String streetAndCity = extractregion(address);
            selectedLocation = null;
            initfirebaseData(streetAndCity,dialog);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
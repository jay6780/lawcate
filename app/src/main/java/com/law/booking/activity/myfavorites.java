package com.law.booking.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.bookingUi.history_book;
import com.law.booking.activity.MainPageActivity.chat.User_list;
import com.law.booking.activity.MainPageActivity.newHome;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Service.MessageNotificationService;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.ArtistAdapter;
import com.law.booking.activity.tools.adapter.ArtistAdapter2;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;

public class myfavorites extends AppCompatActivity implements OnRefreshListener {
    private SearchView searchProvider;
    private RecyclerView artistRecycler, eventOrg;
    private DatabaseReference favoritesRef, databaseReference, databaseReference2, guessRef;
    private ArrayList<Usermodel> providerList, eventOrgList;
    private ArtistAdapter providerAdapter;
    private ArtistAdapter2 eventOrgAdapter;
    private FirebaseAuth mAuth;
    private TextView TopArt, TopOrganizer;
    private DrawerLayout drawerLayout;
    private RelativeLayout rl;
    private ImageView drawerToggle, avatar, gunting, avatar2, bell;
    private TextView email2, username2, email3, profiletxt, username, location_address;
    String bookprovideremail = SPUtils.getInstance().getString(AppConstans.bookprovideremail);
    private LinearLayout favoriteLayout,loading_layout,root_view;
    private SmartRefreshLayout refreshLayout;
    private SkeletonScreen skeletonScreen;
    private String favoriteId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfavorites);
        changeStatusBarColor(getResources().getColor(R.color.white2));
        eventOrg = findViewById(R.id.eventOrg);
        searchProvider = findViewById(R.id.search);
        bell = findViewById(R.id.bell);
        drawerToggle = findViewById(R.id.toggle_favorite);
        artistRecycler = findViewById(R.id.artist);
        root_view = findViewById(R.id.root_view);
        loading_layout = findViewById(R.id.loading_layout);
        favoriteLayout = findViewById(R.id.favoriteLayout);
        refreshLayout = findViewById(R.id.refreshLayout);
        TopOrganizer = findViewById(R.id.TopOrganizer);
        drawerLayout = findViewById(R.id.drawer_layout);
        TopArt = findViewById(R.id.TopArt);
        rl = findViewById(R.id.user_viewsmenu);
        rl.setVisibility(View.VISIBLE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        intiClick_nav();
        mAuth = FirebaseAuth.getInstance();
        setupSearchView();
        initDraWtoggle2();
        initFavorites();
        intiGuess();
        initShowbook();
        initSkeleton();
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableRefresh(true);
    }
    private void initSkeleton() {
        favoriteLayout.setVisibility(View.VISIBLE);
        skeletonScreen = Skeleton.bind(root_view)
                .load(R.layout.skeletonlayout_2)
                .duration(1500)
                .color(R.color.colorFontGreyDark)
                .angle(20)
                .show();
        new Handler().postDelayed(() -> {
            skeletonScreen.hide();
            root_view.setVisibility(View.GONE);
        }, 1500);
    }

    private void intiGuess() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            guessRef = FirebaseDatabase.getInstance().getReference("Client").child(userId);
            guessRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usermodel user = dataSnapshot.getValue(Usermodel.class);
                        if (user != null) {
                            initUserInfo(user.getImage(), user.getUsername(), user.getEmail());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors here, if necessary.
                }

            });
        }
    }

    private void initFavorites() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child("favorites").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        providerList = new ArrayList<>();  // Initialize the providerList
                        eventOrgList = new ArrayList<>();  // Initialize the eventOrgList

                        for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                            favoriteId = favoriteSnapshot.getKey(); // Get the favoriteId
                            Boolean isFavorite = favoriteSnapshot.getValue(Boolean.class); // Check if it's marked as a favorite

                            if (isFavorite != null && isFavorite) {
//                                Log.d("Favorites", "Favorite ID: " + favoriteId);
                                // Call initFirebase for each favoriteId to load the corresponding data
                                initFirebase(favoriteId);
                            }
                        }
                    } else {
                        Log.d("Favorites", "No favorites found for the user.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error fetching favorites", databaseError.toException());
                }
            });
        }
    }

    private void initShowbook() {
        startService(new Intent(this, MessageNotificationService.class));
        TextView badgeCount = findViewById(R.id.badge_count);
        String badgenum = SPUtils.getInstance().getString(AppConstans.booknum);
        if(badgenum == null){
            badgeCount.setText("0");
            SPUtils.getInstance().put(AppConstans.booknum, "0");
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



    private void initFirebase(String favoriteId) {
        providerList = new ArrayList<>();
        providerAdapter = new ArtistAdapter(providerList, this);
        artistRecycler.setAdapter(providerAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("Lawyer").child(favoriteId);
        artistRecycler.setLayoutManager(new LinearLayoutManager(this));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usermodel usermodel = dataSnapshot.getValue(Usermodel.class);
                    if (usermodel != null) {
                        usermodel.setKey(dataSnapshot.getKey());
                        providerList.add(usermodel);
                    }
                    providerAdapter.notifyDataSetChanged();

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("User_list", "DatabaseError: " + databaseError.getMessage());
            }
        });

//        initEvent(favoriteId);

    }

    private void initEvent(String favoriteId) {
        eventOrg.setLayoutManager(new LinearLayoutManager(this));
        eventOrgList = new ArrayList<>();
        eventOrgAdapter = new ArtistAdapter2(eventOrgList, this);
        eventOrg.setAdapter(eventOrgAdapter);
        databaseReference2 = FirebaseDatabase.getInstance().getReference("Events").child(favoriteId);
        databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usermodel eventOrgModel = dataSnapshot.getValue(Usermodel.class);
                    if (eventOrgModel != null) {
                        eventOrgModel.setKey(dataSnapshot.getKey());
                        eventOrgList.add(eventOrgModel);
                    }
                    eventOrgAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("EventOrg_list", "DatabaseError: " + databaseError.getMessage());
            }
        });
    }


    private void initUserInfo(String image,String usernamedata,String email) {
        String address = SPUtils.getInstance().getString(AppConstans.homeAddress);
        username = findViewById(R.id.username);
        location_address = findViewById(R.id.location_address);
        avatar = findViewById(R.id.avatar);
        avatar2= findViewById(R.id.avatar2);
        email2 = findViewById(R.id.email2);
        username2 = findViewById(R.id.username2);
        email3 = findViewById(R.id.email);
        profiletxt = findViewById(R.id.profiletxt);
        gunting = findViewById(R.id.gunting);
        String streetAndCity = extractStreetAndCity(address);
        location_address.setText(streetAndCity);
        email3.setVisibility(View.GONE);
        if (image != null && !image.isEmpty()) {
            RequestOptions requestOptions = new RequestOptions().circleCrop();
            Glide.with(this)
                    .load(image)
                    .apply(requestOptions)
                    .into(avatar);
        } else {
            avatar.setImageResource(R.mipmap.man);
        }

        if (image != null && !image.isEmpty()) {
            RequestOptions requestOptions = new RequestOptions().circleCrop();
            Glide.with(this)
                    .load(image)
                    .apply(requestOptions)
                    .into(avatar2);
        } else {
            avatar2.setImageResource(R.mipmap.man);
        }
        username.setText(usernamedata);
        username2.setText(usernamedata);
        email2.setText(email);
    }

    private String extractStreetAndCity(String address) {
        if (address == null) return "";

        String[] parts = address.split(",");
        String street = parts.length > 0 ? parts[0].trim() : "";
        String city = parts.length > 1 ? parts[1].trim() : "";

        return street + "\n" + city;
    }


    private void intiClick_nav() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.home_user) {
                    Intent intent = new Intent(getApplicationContext(), newHome.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else if (v.getId() == R.id.favorites) {
                    Intent intent = new Intent(getApplicationContext(), myfavorites.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else if (v.getId() == R.id.messageImg) {
                    Intent intent = new Intent(getApplicationContext(), User_list.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                } else if (v.getId() == R.id.logout_user) {
                    Dialog dialog = new Dialog();
                    dialog.logout(myfavorites.this);

                }
            }
        };

        idlisterners2(clickListener);
    }

    private void idlisterners2(View.OnClickListener clickListener) {
        findViewById(R.id.logout_user).setOnClickListener(clickListener);
        findViewById(R.id.home_user).setOnClickListener(clickListener);
        findViewById(R.id.favorites).setOnClickListener(clickListener);
        findViewById(R.id.messageImg).setOnClickListener(clickListener);
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
                    resetAllData();
                } else {
                    filterProviderList(newText);
                    filterEventOrgList(newText);
                }
                return true;
            }
        });
    }

    private void initDraWtoggle2() {
        drawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.purple_theme));
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.white2));
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    private void resetAllData() {
        providerAdapter.updateList(providerList);
        eventOrgAdapter.updateList(eventOrgList);
        TopArt.setVisibility(View.VISIBLE);
//        TopOrganizer.setVisibility(View.VISIBLE);
    }

    private void filterProviderList(String query) {
        ArrayList<Usermodel> filteredProviderList = new ArrayList<>();
        for (Usermodel user : providerList) {
            if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredProviderList.add(user);
            }
        }

        providerAdapter.updateList(filteredProviderList);
        if (!filteredProviderList.isEmpty()) {
            eventOrgAdapter.updateList(new ArrayList<>());
            TopOrganizer.setVisibility(View.GONE);
        } else {
            providerAdapter.updateList(new ArrayList<>());
        }
    }

    private void filterEventOrgList(String query) {
        ArrayList<Usermodel> filteredEventOrgList = new ArrayList<>();
        for (Usermodel eventOrg : eventOrgList) {
            if (eventOrg.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredEventOrgList.add(eventOrg);
            }
        }

        eventOrgAdapter.updateList(filteredEventOrgList);
        if (!filteredEventOrgList.isEmpty()) {
            TopArt.setVisibility(View.GONE);
            providerAdapter.updateList(new ArrayList<>());
        } else {
            eventOrgAdapter.updateList(new ArrayList<>());
        }
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.getLayout().postDelayed(() -> {
            boolean isRefreshSuccessful = fetchDataFromSource();
            if (isRefreshSuccessful) {
                refreshLayout.finishRefresh();
                loading_layout.setVisibility(View.VISIBLE);
                loading_layout.postDelayed(() ->   loading_layout.setVisibility(View.GONE), 1500);
            } else {
                refreshLayout.finishRefresh(false);
            }
        }, 100);
    }


    private boolean fetchDataFromSource() {
        try {
            initFavorites();
            initSkeleton();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
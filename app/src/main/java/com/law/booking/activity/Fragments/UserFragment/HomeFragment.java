package com.law.booking.activity.Fragments.UserFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.activity.MainPageActivity.Provider.Event_provider;
import com.law.booking.activity.MainPageActivity.Provider.hmua;
import com.law.booking.activity.tools.Model.Discounts;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Utils.Utils;
import com.law.booking.activity.tools.adapter.ArtistAdapter;
import com.law.booking.activity.tools.adapter.ArtistAdapter2;
import com.law.booking.activity.tools.adapter.ImageAdapter;

import com.law.booking.R;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnRefreshListener {
    private Banner banner;
    private SearchView searchProvider;
    private RecyclerView artistRecycler, eventOrg;
    private DatabaseReference databaseReference, databaseReference2, databaseReference3;
    private ArrayList<Usermodel> providerList, eventOrgList;
    private ArtistAdapter providerAdapter;
    private ArtistAdapter2 eventOrgAdapter;
    private TextView TopArt, ViewAll, TopOrganizer, ViewAll_artist, category;
    private LinearLayout home_layout, root_view, loading_layout;
    private SkeletonScreen skeletonScreen;
    private LinearLayout bannercontent, linearView;
    private SmartRefreshLayout refreshLayout;
    private boolean isCorporate = true;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);
        initClick(view);
        initView(view);
        initFirebase();
        initgender();
        initfirebase2();
        setupSearchView();
        initSkeleton();
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableRefresh(true);
        return view;

    }


    private void initSkeleton() {
        home_layout.setVisibility(View.VISIBLE);
        skeletonScreen = Skeleton.bind(root_view)
                .load(R.layout.skeletonlayout_2)
                .duration(1500)
                .color(R.color.colorFontGreyDark)
                .angle(20)
                .show();
        new Handler().postDelayed(() -> {
            skeletonScreen.hide();
            root_view.setVisibility(View.GONE);
            initFirebase();
        }, 1500);
    }

    private void initgender() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference3 = FirebaseDatabase.getInstance().getReference("Client").child(userId);
            databaseReference3.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usermodel user = dataSnapshot.getValue(Usermodel.class);
                        if (user != null) {
                            String gender = user.getGender();
                            intiBanner(gender);
                        }
                    }
                }

                @SuppressLint("LongLogTag")
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }

            });
        }
    }

    private void initfirebase2() {
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

    private void resetAllData() {
        // Reset visibility for all headers
        bannercontent.setVisibility(View.VISIBLE);
        category.setVisibility(View.VISIBLE);
        linearView.setVisibility(View.VISIBLE);
        TopArt.setVisibility(View.VISIBLE);
        ViewAll.setVisibility(View.VISIBLE);
//        TopOrganizer.setVisibility(View.VISIBLE);
//        ViewAll_artist.setVisibility(View.VISIBLE);

        // Reset both lists to show all data
        providerAdapter.updateList(providerList);
//        eventOrgAdapter.updateList(eventOrgList);
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
            bannercontent.setVisibility(View.GONE);
            category.setVisibility(View.GONE);
            linearView.setVisibility(View.GONE);
            TopOrganizer.setVisibility(View.GONE);
            TopArt.setVisibility(View.GONE);
            ViewAll.setVisibility(View.GONE);
            ViewAll_artist.setVisibility(View.GONE);
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
            providerAdapter.updateList(new ArrayList<>());
            bannercontent.setVisibility(View.GONE);
            category.setVisibility(View.GONE);
            linearView.setVisibility(View.GONE);
            TopArt.setVisibility(View.GONE);
            ViewAll.setVisibility(View.GONE);
            ViewAll_artist.setVisibility(View.GONE);
        } else {
            eventOrgAdapter.updateList(new ArrayList<>());
        }
    }


    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Lawyer");
        databaseReference2 = FirebaseDatabase.getInstance().getReference("Events");
        artistRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        providerList = new ArrayList<>();
        providerAdapter = new ArtistAdapter(providerList, getActivity());
        artistRecycler.setAdapter(providerAdapter);

        // Initialize eventOrg RecyclerView for eventOrgList
        eventOrg.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventOrgList = new ArrayList<>();
        eventOrgAdapter = new ArtistAdapter2(eventOrgList, getActivity());
        eventOrg.setAdapter(eventOrgAdapter);

        // Load data for ADMIN
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                providerList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usermodel usermodel = snapshot.getValue(Usermodel.class);
                    usermodel.setKey(snapshot.getKey());
                    Boolean isSuperAdmin = snapshot.child("isSuperAdmin").getValue(Boolean.class);
                    if ((isSuperAdmin == null || !isSuperAdmin)) {
                        providerList.add(usermodel);
                    }

                }
                providerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("User_list", "DatabaseError: " + databaseError.getMessage());
            }
        });

        // Load data for EventOrg
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventOrgList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usermodel eventOrgModel = snapshot.getValue(Usermodel.class);
                    eventOrgModel.setKey(snapshot.getKey());
                    eventOrgList.add(eventOrgModel);
                }
                eventOrgAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("EventOrg_list", "DatabaseError: " + databaseError.getMessage());
            }
        });
    }

    private void intiBanner(String gender) {
        final List<Discounts> discounts = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Discounts");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                discounts.clear();
                if (!dataSnapshot.exists()) {
                    banner.setVisibility(View.GONE);
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Discounts discount = snapshot.getValue(Discounts.class);
                    if (discount.getGender() == null || discount.getGender().isEmpty()) {
                        discounts.add(discount);
                    } else if ("Female".equals(gender) && "Female".equals(discount.getGender())) {
                        discounts.add(discount);
                    } else if ("Male".equals(gender) && "Male".equals(discount.getGender())) {
                        discounts.add(discount);
                    }
                }

                if (discounts.isEmpty()) {
                    banner.setVisibility(View.GONE); // Hide banner if no discounts for females
                } else {
                    setupBanner(discounts); // Display banner with filtered discounts
                    banner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data: " + databaseError.getMessage());
            }
        });
    }

    private void setupBanner(List<Discounts> discounts) {
        double sd = 2.8;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (getActivity() != null) {
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels - Utils.dp2px(getActivity(), 20);
            int height = (int) (screenWidth / sd) + Utils.dp2px(getActivity(), 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(screenWidth, height);
            lp.leftMargin = Utils.dp2px(getActivity(), 10);
            lp.rightMargin = Utils.dp2px(getActivity(), 10);
            lp.topMargin = Utils.dp2px(getActivity(), 5);
            banner.setLayoutParams(lp);
            banner.setAdapter(new ImageAdapter(discounts, getContext()))
                    .setIndicator(new CircleIndicator(getActivity()))
                    .setOnBannerListener((data, position) -> {
                    })
                    .start();
        }
    }


    private void initView(View view) {
        loading_layout = view.findViewById(R.id.loading_layout);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        bannercontent = view.findViewById(R.id.bannercontent);
        category = view.findViewById(R.id.category);
        linearView = view.findViewById(R.id.linearView);
        root_view = view.findViewById(R.id.root_view);
        home_layout = view.findViewById(R.id.home_layout);
        banner = view.findViewById(R.id.banner);
        ViewAll_artist = view.findViewById(R.id.ViewAll_artist);
        ViewAll = view.findViewById(R.id.ViewAll);
        TopOrganizer = view.findViewById(R.id.TopOrganizer);
        TopArt = view.findViewById(R.id.TopArt);
        eventOrg = view.findViewById(R.id.eventOrg);
        searchProvider = view.findViewById(R.id.search);
        artistRecycler = view.findViewById(R.id.artist);
    }

    private void initClick(View view) {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) return;

                switch (v.getId()) {
                    case R.id.linear1:
                        boolean isCorporate = true;
                        Intent intent = new Intent(getActivity(), hmua.class);
                        intent.putExtra("title", "Corporate");
                        intent.putExtra("isCorporate", isCorporate);
                        startActivity(intent);
                        break;
                    case R.id.linear2:
                        boolean isFamily = true;
                        Intent intent3 = new Intent(getActivity(), hmua.class);
                        intent3.putExtra("title", "Family");
                        intent3.putExtra("isFamily", isFamily);
                        startActivity(intent3);
                        break;

                    case R.id.linear3:
                        boolean isCriminal = true;
                        Intent intent2 = new Intent(getActivity(), hmua.class);
                        intent2.putExtra("title", "Criminal");
                        intent2.putExtra("isCriminal", isCriminal);
                        startActivity(intent2);
                        break;

                    case R.id.linear4:
                        boolean isImmigration = true;
                        Intent intent4 = new Intent(getActivity(), hmua.class);
                        intent4.putExtra("title", "Immigration");
                        intent4.putExtra("isImmigration", isImmigration);
                        startActivity(intent4);
                        break;

                    case R.id.linear5:
                        boolean isProperty = true;
                        Intent intent5 = new Intent(getActivity(), hmua.class);
                        intent5.putExtra("title", "Property");
                        intent5.putExtra("isProperty", isProperty);
                        startActivity(intent5);
                        break;

                    case R.id.ViewAll:
                        boolean isAll = true;
                        Intent intent6 = new Intent(getActivity(), hmua.class);
                        intent6.putExtra("title", "All lawyers");
                        intent6.putExtra("isAll", isAll);
                        startActivity(intent6);

                        break;
                    case R.id.ViewAll_artist:
                        startActivity(new Intent(getActivity(), Event_provider.class));
                        break;
                }
            }
        };
        idListeners(view, clickListener);
    }

    private void idListeners(View view, View.OnClickListener clickListener) {
        view.findViewById(R.id.linear1).setOnClickListener(clickListener);
        view.findViewById(R.id.linear2).setOnClickListener(clickListener);
        view.findViewById(R.id.linear3).setOnClickListener(clickListener);
        view.findViewById(R.id.linear4).setOnClickListener(clickListener);
        view.findViewById(R.id.linear5).setOnClickListener(clickListener);
        view.findViewById(R.id.ViewAll).setOnClickListener(clickListener);
        view.findViewById(R.id.ViewAll_artist).setOnClickListener(clickListener);

    }

    @Override
    public void onResume() {
        super.onResume();
        initFirebase();
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
            initSkeleton();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
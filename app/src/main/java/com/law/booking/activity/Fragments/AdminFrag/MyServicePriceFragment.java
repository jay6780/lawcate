package com.law.booking.activity.Fragments.AdminFrag;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.Admin.AddService;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.adapter.ServiceAdapter;

import java.util.ArrayList;

public class MyServicePriceFragment extends Fragment {
    private RecyclerView myserviceRecycler;
    private ServiceAdapter serviceAdapter;
    private DatabaseReference serviceRef;
    private ImageView add, clear;
    private ArrayList<Service> services;
    private LinearLayout home_layout, root_view, loading_layout;
    private SkeletonScreen skeletonScreen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service2, container, false);
        myserviceRecycler = view.findViewById(R.id.my_serviceRecycler);
        add = view.findViewById(R.id.add);
        clear = view.findViewById(R.id.clear);
        clear.setVisibility(View.GONE);
        root_view = view.findViewById(R.id.root_view);
        home_layout = view.findViewById(R.id.home_layout);
        loading_layout = view.findViewById(R.id.loading_layout);
        services = new ArrayList<>();
        myserviceRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            serviceRef = FirebaseDatabase.getInstance().getReference("Service").child(user.getUid());
            fetchServices();
        }
        add.setOnClickListener(v -> openAddServiceActivity());
        clear.setOnClickListener(view1 -> clearDiscount());
        initSkeleton();
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
        }, 1500);
    }


    private void clearDiscount() {
        Dialog clearDiscount = new Dialog();
        clearDiscount.clearDiscount(getActivity());
    }

    private void openAddServiceActivity() {
        Intent intent = new Intent(getContext(), AddService.class);
        startActivity(intent);
    }

    private void fetchServices() {
        DatabaseReference serviceRef = FirebaseDatabase.getInstance().getReference("Service").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                services.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    if (service != null) {
                        services.add(service);
                    }
                }
                serviceAdapter = new ServiceAdapter(services, getContext());
                myserviceRecycler.setAdapter(serviceAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MyServicePriceFragment", "Error fetching services: " + databaseError.getMessage());
            }
        });
    }

}
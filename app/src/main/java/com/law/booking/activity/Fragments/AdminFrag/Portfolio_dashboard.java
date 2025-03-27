package com.law.booking.activity.Fragments.AdminFrag;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.Admin.CoverUpload;
import com.law.booking.activity.MainPageActivity.Admin.Multpile_upload;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.adapter.portfolioAdapter_admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Portfolio_dashboard extends Fragment {
    private DatabaseReference portfoilio,coverphoto;
    private portfolioAdapter_admin portfolioAdapter;
    private ViewSkeletonScreen skeletonScreen;
    private LinearLayout ll_skeleton;
    private RecyclerView portfolioRecycler;
    private NestedScrollView scroller;
    private ImageView add,clear;
    String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String TAG = "ServiceProvidersFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dashboard_portfolio, container, false);
        ll_skeleton = view.findViewById(R.id.ll_skeleton);
        ll_skeleton.setVisibility(View.VISIBLE);
        scroller = view.findViewById(R.id.scroller);
        add = view.findViewById(R.id.add);
        clear = view.findViewById(R.id.clear);
        portfolioRecycler = view.findViewById(R.id.portfolioRecycler);
        portfoilio = FirebaseDatabase.getInstance().getReference("Myportfolio").child(key);
        coverphoto = FirebaseDatabase.getInstance().getReference("Cover_photo").child(key);
        initSkeleton();
        fetchPortfolio();
        add.setOnClickListener(view1 -> gotoAdd());
        clear.setOnClickListener(view1 -> clearImage());

        return view;

    }

    private void clearImage() {
        new AlertDialog.Builder(getContext())
                .setTitle("Choose an action")
                .setItems(new String[]{"Clear cover photo","Clear portfolio"}, (dialog, which) -> {
                    if (which == 0) {
                        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                .setMessage("Are you sure you want to clear cover photo?")
                                .setPositiveButton("Yes", (dialog2, which2) -> {
                                    coverphoto.removeValue();
                                    Toast.makeText(getContext(),"All photos cleared",Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("No", (dialog2, which2) -> {
                                    dialog.dismiss();
                                })
                                .show();
                    } else if (which == 1) {
                        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                .setMessage("Are you sure you want to clear these images?")
                                .setPositiveButton("Yes", (dialog2, which2) -> {
                                    portfoilio.removeValue();
                                    Toast.makeText(getContext(),"All photos cleared",Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("No", (dialog2, which2) -> {
                                    dialog.dismiss();
                                })
                                .show();
                    }
                })
                .show();


    }

    private void gotoAdd() {
        new AlertDialog.Builder(getContext())
                .setTitle("Choose an action")
                .setItems(new String[]{"Upload portfolio photo","Upload cover photo"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent go = new Intent(getContext(), Multpile_upload.class);
                        startActivity(go);
                    } else if (which == 1) {
                        Intent chat = new Intent(getActivity(), CoverUpload.class);
                        getActivity().startActivity(chat);
                        getActivity().overridePendingTransition(0,0);
                    }
                })
                .show();


    }

    private void fetchPortfolio() {
        portfoilio.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                Set<String> uniqueCaptions = new HashSet<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    if (service != null) {
                        String caption = service.getCaption().toLowerCase();
                        if (!uniqueCaptions.contains(caption)) {
                            uniqueCaptions.add(caption);
                            services.add(service);
                        }
                    }
                }
                fetchPortfoliorecycler(services);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
    }

    private void fetchPortfoliorecycler(List<Service> services) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        portfolioRecycler.setLayoutManager(gridLayoutManager);
        portfolioAdapter = new portfolioAdapter_admin(services, getContext());
        portfolioRecycler.setAdapter(portfolioAdapter);
    }

    private void initSkeleton() {
        skeletonScreen = Skeleton.bind(ll_skeleton)
                .load(R.layout.skeletonlayout_2)
                .duration(1000)
                .color(R.color.colorFontGreyDark)
                .angle(20)
                .show();
        new Handler().postDelayed(() -> {
            skeletonScreen.hide();
            portfolioRecycler.setVisibility(View.VISIBLE);
            scroller.setVisibility(View.VISIBLE);
            ll_skeleton.setVisibility(View.GONE);
        }, 1000);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}

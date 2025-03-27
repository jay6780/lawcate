package com.law.booking.activity.Fragments.AdminFrag;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Review;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.adapter.ReviewAdapter;

import java.util.ArrayList;
import java.util.List;

public class Review_adminfrag extends Fragment {
    private RecyclerView commentRecycler;
    private DatabaseReference reviewRef;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private String TAG = "Rating_adminview";
    private DatabaseReference serviceRef;
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_adminfrag, container, false);
        commentRecycler = view.findViewById(R.id.commentRecycler);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList, getContext());
        commentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        commentRecycler.setAdapter(reviewAdapter);
        fetchServices();
        fetchdata();
        return view;
    }
    private void fetchServices() {
        serviceRef = FirebaseDatabase.getInstance().getReference("Service").child(currentUserId);
        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    services.add(service);
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
    }

    private void fetchdata() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(uid);
        reviewRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Review review = dataSnapshot.getValue(Review.class);
                if (review != null) {
                    reviewList.add(review);
                    reviewAdapter.notifyItemInserted(reviewList.size() - 1);
                    commentRecycler.scrollToPosition(reviewList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Review review = dataSnapshot.getValue(Review.class);
                if (review != null) {
                    for (int i = 0; i < reviewList.size(); i++) {
                        if (reviewList.get(i).getReviewId().equals(review.getReviewId())) {
                            reviewList.set(i, review);
                            reviewAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Review review = dataSnapshot.getValue(Review.class);
                if (review != null) {
                    for (int i = 0; i < reviewList.size(); i++) {
                        if (reviewList.get(i).getReviewId().equals(review.getReviewId())) {
                            reviewList.remove(i);
                            reviewAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Handle child moved if necessary, depending on your use case.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read reviews: " + databaseError.getMessage());
            }
        });
    }
}
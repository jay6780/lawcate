package com.law.booking.activity.MainPageActivity.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.law.booking.activity.tools.Model.Review;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.Utils.Utils;
import com.law.booking.activity.tools.adapter.ReviewAdapter;
import com.law.booking.activity.tools.adapter.coverAdapter;
import com.law.booking.R;
import com.taufiqrahman.reviewratings.BarLabels;
import com.taufiqrahman.reviewratings.RatingReviews;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

public class Rating_adminview extends AppCompatActivity {
    private TextView rateTextView;
    private RatingReviews ratingReviews;
    private String username = SPUtils.getInstance().getString(AppConstans.AdminUsername);
    private String image = SPUtils.getInstance().getString(AppConstans.AdminImage);
    private ImageView userImage,back;
    private TextView usernameTxt;
    private RecyclerView commentRecycler;
    private DatabaseReference reviewRef;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private String TAG = "Rating_adminview";
    private DatabaseReference serviceRef;
    private Banner banner;
    private ImageView heart;
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private LinearLayout bannercontent,root_view;
    private RelativeLayout reviewlayoutview;
    private SkeletonScreen skeletonScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_adminview);
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        rateTextView = findViewById(R.id.rateTextView);
        userImage = findViewById(R.id.userImage);
        ratingReviews = findViewById(R.id.rating_reviews);
        back = findViewById(R.id.back);
        banner = findViewById(R.id.banner);
        bannercontent = findViewById(R.id.bannercontent);
        userImage = findViewById(R.id.userImage);
        heart = findViewById(R.id.heart);
        commentRecycler = findViewById(R.id.commentRecycler);
        usernameTxt = findViewById(R.id.username);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList,Rating_adminview.this);
        commentRecycler.setLayoutManager(new LinearLayoutManager(Rating_adminview.this));
        commentRecycler.setAdapter(reviewAdapter);
        root_view = findViewById(R.id.root_view);
        reviewlayoutview = findViewById(R.id.reviewlayoutview);
        initLoaduserDetail();
        initViewRate();
        heart.setVisibility(View.GONE);
        fetchServices();
        fetchdata();
        back.setOnClickListener(view -> onBackPressed());
        initSkeleton();
    }
    private void initSkeleton() {
        reviewlayoutview.setVisibility(View.VISIBLE);
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


    private void fetchServices() {
        serviceRef = FirebaseDatabase.getInstance().getReference("Service").child(currentUserId);
        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    String image = service != null ? service.getImageUrl() : null;

                    if (image != null) {
                        bannercontent.setVisibility(View.VISIBLE);
                        adjustMargin(userImage, -40);
                    } else {
                        bannercontent.setVisibility(View.GONE);
                        adjustMargin(userImage, 20);
                        adjustMargin(usernameTxt, 20);
                    }
                    services.add(service);
                }
                setupBanner(services);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Adjusts the top margin of the given view.
     *
     * @param view  The view to adjust the margin for.
     * @param marginTop The top margin value to set.
     */
    private void adjustMargin(View view, int marginTop) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.topMargin = marginTop;
        view.setLayoutParams(params);
    }


    private void initLoaduserDetail() {
        usernameTxt.setText(username);
        if (image != null && !image.isEmpty()) {
            RequestOptions requestOptions = new RequestOptions().circleCrop();
            Glide.with(this)
                    .load(image)
                    .apply(requestOptions)
                    .into(userImage);
        } else {
            userImage.setImageResource(R.mipmap.man);
        }
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

    private void setupBanner(List<Service> services) {
        if (Rating_adminview.this != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Rating_adminview.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int heightInPixels = Utils.dp2px(Rating_adminview.this, 300);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(screenWidth, heightInPixels);
            banner.setLayoutParams(lp);
            banner.setAdapter(new coverAdapter(services, Rating_adminview.this))
                    .setIndicator(new CircleIndicator(Rating_adminview.this))
                    .setOnBannerListener((data, position) -> {
                        // Handle banner click events
                    })
                    .start();
        }
    }


    private void initViewRate() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reviews").child(uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalRating = 0;
                    int reviewCount = 0;
                    int[] ratingCounts = new int[5]; // For ratings 5, 4, 3, 2, 1
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        Integer rating = reviewSnapshot.child("rating").getValue(Integer.class);
                        if (rating != null && rating >= 1 && rating <= 5) {
                            totalRating += rating;
                            reviewCount++;
                            ratingCounts[5 - rating]++; // Map 5 -> 0, 4 -> 1, ..., 1 -> 4
                        }
                    }
                    if (reviewCount > 0) {
                        float averageRating = (float) totalRating / reviewCount;
                        rateTextView.setText(String.format("%.1f", averageRating));
                    }
                    int colors[] = new int[]{
                            Color.parseColor("#0e9d58"),
                            Color.parseColor("#bfd047"),
                            Color.parseColor("#ffc105"),
                            Color.parseColor("#ef7e14"),
                            Color.parseColor("#d36259")
                    };
                    ratingReviews.createRatingBars(100, BarLabels.STYPE2, colors, ratingCounts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FetchFilteredPackages", "Failed to fetch data: " + error.getMessage());
            }
        });
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
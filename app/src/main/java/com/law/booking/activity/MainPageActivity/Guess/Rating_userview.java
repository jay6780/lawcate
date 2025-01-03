package com.law.booking.activity.MainPageActivity.Guess;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.addreview.addReviewActivity;
import com.law.booking.activity.tools.Model.Review;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.Utils.Utils;
import com.law.booking.activity.tools.adapter.ReviewAdapter;
import com.law.booking.activity.tools.adapter.coverAdapter;

import com.taufiqrahman.reviewratings.BarLabels;
import com.taufiqrahman.reviewratings.RatingReviews;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rating_userview extends AppCompatActivity {
    String key = SPUtils.getInstance().getString(AppConstans.key);
    private TextView rateTextView;
    private RatingReviews ratingReviews;
    private String username;
    private String image;
    private ImageView userImage,back;
    private TextView usernameTxt;
    private RecyclerView commentRecycler;
    private DatabaseReference reviewRef;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private String TAG = "Rating_adminview";
    private ImageView addreview;
    private DatabaseReference serviceRef,guessRef;
    private Banner banner;
    private ImageView heart;
    private DatabaseReference userFavoritesRef,EventOrg;
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
        banner = findViewById(R.id.banner);
        bannercontent = findViewById(R.id.bannercontent);
        userImage = findViewById(R.id.userImage);
        reviewlayoutview = findViewById(R.id.reviewlayoutview);
        ratingReviews = findViewById(R.id.rating_reviews);
        heart = findViewById(R.id.heart);
        back = findViewById(R.id.back);
        commentRecycler = findViewById(R.id.commentRecycler);
        usernameTxt = findViewById(R.id.username);
        root_view = findViewById(R.id.root_view);
        addreview = findViewById(R.id.addreview);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList, Rating_userview.this);
        commentRecycler.setLayoutManager(new LinearLayoutManager(Rating_userview.this));
        commentRecycler.setAdapter(reviewAdapter);
        userFavoritesRef = FirebaseDatabase.getInstance().getReference("favorites");
        initSkeleton();
        initViewRate();
        initHeartColor();
        fetchdata();
        back.setOnClickListener(view -> onBackPressed());
        initloadProviderdata();
        addreview.setVisibility(View.VISIBLE);
        addreview.setOnClickListener(view -> addreviews());
        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initHeart();
            }
        });
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

    private void initHeartColor() {
        userFavoritesRef.child(currentUserId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    heart.setImageResource(R.mipmap.heart_fullred);
                } else {
                    heart.setImageResource(R.mipmap.heart_nobg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ArtistAdapter", "Error checking favorite status: " + error.getMessage());
            }
        });
    }

    private void initHeart() {
        userFavoritesRef.child(currentUserId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userFavoritesRef.child(currentUserId).child(key).removeValue();
                   heart.setImageResource(R.mipmap.heart_nobg);
                } else {
                    // Add to favorites
                    userFavoritesRef.child(currentUserId).child(key).setValue(true);
                    heart.setImageResource(R.mipmap.heart_fullred);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ArtistAdapter", "Error updating favorite status: " + error.getMessage());
            }
        });

    }

    private void addreviews() {
        Intent intent = new Intent(Rating_userview.this, addReviewActivity.class);
        startActivity(intent);
    }
    private void initGuessData() {
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
                            String gender = user.getGender();
                            Log.d(TAG, "Gender: " + gender);
                            fetchServices(gender);
                        }
                    }
                }
                @SuppressLint("LongLogTag")
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error fetching user data: " + databaseError.getMessage());
                }
            });
        }
    }
    private void fetchServices(String gender) {
        serviceRef = FirebaseDatabase.getInstance().getReference("Service").child(key);
        serviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Service> services = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Service service = snapshot.getValue(Service.class);
                        processServiceWithGender(service, services, gender);
                    }
                    setupBanner(services);
                } else {
                    // Admin does not exist in "Service", fallback to "EventOrg"
                    fetchServicesFromEventOrg();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
    }

    private void fetchServicesFromEventOrg() {
        EventOrg = FirebaseDatabase.getInstance().getReference("EventOrg").child(key);
        EventOrg.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    processServiceWithGender(service, services, "");
                }
                setupBanner(services);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services from EventOrg: " + databaseError.getMessage());
            }
        });
    }

    private void processServiceWithGender(Service service, List<Service> services, String gender) {
        if (service != null) {
            String image = service.getImageUrl();
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
    }

    private void processServiceWithoutGender(Service service, List<Service> services) {
        if (service != null) {
            String image = service.getImageUrl();
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

    private void setupBanner(List<Service> services) {
        if (Rating_userview.this != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Rating_userview.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int heightInPixels = Utils.dp2px(Rating_userview.this, 300);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(screenWidth, heightInPixels);
            banner.setLayoutParams(lp);
            banner.setAdapter(new coverAdapter(services, Rating_userview.this))
                    .setIndicator(new CircleIndicator(Rating_userview.this))
                    .setOnBannerListener((data, position) -> {
                        // Handle banner click events
                    })
                    .start();
        }
    }




    private void initloadProviderdata() {
        if (key != null && !key.isEmpty()) {
            DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Lawyer").child(key);
            DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("ADMIN").child(key);

            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usermodel user = dataSnapshot.getValue(Usermodel.class);
                        if (user != null) {
                            username = user.getUsername();
                            image = user.getImage();
                            updateUserDetails(username, image);
                        }
                    } else {
                        loadFromEvents(eventsRef);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error fetching user data from ADMIN: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e(TAG, "Invalid key provided.");
        }
        initGuessData();
    }

    private void loadFromEvents(DatabaseReference eventsRef) {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usermodel user = dataSnapshot.getValue(Usermodel.class);
                    if (user != null) {
                        username = user.getUsername();
                        image = user.getImage();
                        updateUserDetails(username, image);
                    }
                } else {
                    Log.e(TAG, "No data found for the provided key in both ADMIN and Events.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user data from Events: " + databaseError.getMessage());
            }
        });
    }

    private void updateUserDetails(String username, String image) {
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
        reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(key);
        reviewRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Review review = dataSnapshot.getValue(Review.class);
                if (review != null) {
                    reviewList.add(review);
                    Collections.sort(reviewList, (r1, r2) -> Long.compare(r2.getTimemilli(), r1.getTimemilli()));

                    reviewAdapter.notifyDataSetChanged();
                    commentRecycler.scrollToPosition(0);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Review review = dataSnapshot.getValue(Review.class);
                if (review != null) {
                    for (int i = 0; i < reviewList.size(); i++) {
                        if (reviewList.get(i).getReviewId().equals(review.getReviewId())) {
                            reviewList.set(i, review);
                            Collections.sort(reviewList, (r1, r2) -> Long.compare(r2.getTimemilli(), r1.getTimemilli()));
                            reviewAdapter.notifyDataSetChanged();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read reviews: " + databaseError.getMessage());
            }
        });
    }

    private void initViewRate() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reviews").child(key);
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
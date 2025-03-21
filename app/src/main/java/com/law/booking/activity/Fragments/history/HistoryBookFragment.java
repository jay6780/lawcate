package com.law.booking.activity.Fragments.history;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Booking2;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.BookemptyAdapter;
import com.law.booking.activity.tools.adapter.BookingAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryBookFragment extends Fragment {
    private RecyclerView bookrecycler;
    private BookingAdapter bookingAdapter;
    private List<Booking2> bookingList = new ArrayList<>();
    private BookemptyAdapter nodata;
    private String TAG = "history_book";
    private DatabaseReference bookingId;
    private LinearLayout ll_skeleton;
    private ViewSkeletonScreen skeletonScreen;
    int booknumber = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment, container, false);
        bookrecycler = view.findViewById(R.id.historyRecycler);
        bookingAdapter = new BookingAdapter(bookingList, getContext());
        bookrecycler.setAdapter(bookingAdapter);
        bookrecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        nodata = new BookemptyAdapter(getContext());
        ll_skeleton = view.findViewById(R.id.ll_skeleton);
        ll_skeleton.setVisibility(View.VISIBLE);
        initShowbook();
        initSkeleton();

        return view;
    }

    private void initShowbook() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            bookingId = FirebaseDatabase.getInstance().getReference("BookingId").child(userId);
            bookingId.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String chatId = childSnapshot.child("chatId").getValue(String.class);
                            String snapshotkey = childSnapshot.child("snapshotkey").getValue(String.class);
                            if (chatId != null && snapshotkey != null) {
                                fetchBookIds(chatId, snapshotkey);
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error fetching user data: " + databaseError.getMessage());
                }
            });
        }
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
            bookrecycler.setVisibility(View.VISIBLE);
            ll_skeleton.setVisibility(View.GONE);
        }, 1000);
    }

    private void fetchBookIds(String chatId, String snapshotkey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mybookRef = database.getReference("Mybook");
        DatabaseReference mybookUserRef = database.getReference("MybookUser");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            bookingList.clear();
            booknumber = 0;
//            Log.d("HistoryBook", "Chat Room ID: " + chatId);
            mybookRef.child(chatId).child("bookInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        Log.d("HistoryBook", "data not exists");
                        bookingAdapter.notifyDataSetChanged();
                        bookrecycler.setAdapter(nodata);
                        return;
                    }
                    for (DataSnapshot databook : dataSnapshot.getChildren()) {
                        Booking2 booking = databook.getValue(Booking2.class);
                        if (booking != null && booking.getSnapshotkey().equals(snapshotkey)) {
                            bookingList.add(booking);
                            booknumber++;
                            String booknum = String.valueOf(booknumber);
                            Log.d("HistoryBook", "booknum: " + booknum);
                            SPUtils.getInstance().put(AppConstans.booknum, booknum);
                            Log.d(TAG, "bookNum: " + booknum);
                        }

                        Collections.sort(bookingList, (b1, b2) -> b2.getTimestamp().compareTo(b1.getTimestamp()));

                        bookingAdapter.isConfirmed(false);
                        bookingAdapter.notifyDataSetChanged();
                        bookrecycler.setAdapter(bookingAdapter);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("HistoryBook", "Error fetching data from Mybook for chat ID " + chatId + ": " + databaseError.getMessage());
                }
            });

            mybookUserRef.child(chatId).child("bookInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot bookInfoSnapshot : dataSnapshot.getChildren()) {
                            String snapshotkey = bookInfoSnapshot.child("snapshotkey").getValue(String.class);
                            Log.d("MybookUser Snapshot", "snapshotkey: " + snapshotkey);
                            SPUtils.getInstance().put(AppConstans.snapshotkey, snapshotkey);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("HistoryBook", "Error fetching snapshotkey from MybookUser: " + databaseError.getMessage());
                }
            });
        }
    }
}
package com.law.booking.activity.Fragments.eventFrag;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.law.booking.activity.events.history_book_event;
import com.law.booking.activity.settingsEvent.event_settings;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.R;

public class event_profileFragment extends Fragment {

    private ImageView myAvatar;
    private TextView usernametv, useremailtv;
    private RelativeLayout bookingHistory;
    private View view5;
    private String username = SPUtils.getInstance().getString(AppConstans.EventUsername);
    private String email = SPUtils.getInstance().getString(AppConstans.Eventemail);
    private String image = SPUtils.getInstance().getString(AppConstans.EventImage);
    private String phone  = SPUtils.getInstance().getString(AppConstans.EventPhone);
    private String name  = SPUtils.getInstance().getString(AppConstans.EventFullname);
    private String address  = SPUtils.getInstance().getString(AppConstans.EventAdress);
    private String age  = SPUtils.getInstance().getString(AppConstans.EventAge);
    private String lengthOfService  = SPUtils.getInstance().getString(AppConstans.EventLenght);
    public event_profileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.new_profile, container, false);
        initViews(view);
        initClickers(view);
        initLoad(username, email, image);
        return view;
    }

    private void initLoad(String username, String email, String image) {
        usernametv.setText(username);
        useremailtv.setText(email);
        if (image != null && !image.isEmpty()) {
            RequestOptions requestOptions = new RequestOptions().circleCrop();
            Glide.with(getActivity())
                    .load(image)
                    .apply(requestOptions)
                    .into(myAvatar);
        } else {
            myAvatar.setImageResource(R.drawable.baseline_person_24);
        }
    }

    private void initClickers(View view) {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.updatebtn) {
                    Dialog dialog = new Dialog();
                    dialog.updateEventProfile2(getActivity(),image,email,username,phone,name,address,age,lengthOfService);
                } else if (v.getId() == R.id.logout) {
                    Dialog dialog = new Dialog();
                    dialog.logout(getActivity());
                } else if (v.getId() == R.id.settings) {
                    Intent intent = new Intent(getActivity(), event_settings.class);
                    startActivity(intent);
                } else if (v.getId() == R.id.bookingHistory) {
                    Intent intent = new Intent(getActivity(), history_book_event.class);
                    startActivity(intent);
                }
            }
        };

        idListeners(view, clickListener);
    }

    private void idListeners(View view, View.OnClickListener clickListener) {
        view.findViewById(R.id.logout).setOnClickListener(clickListener);
        view.findViewById(R.id.updatebtn).setOnClickListener(clickListener);
        view.findViewById(R.id.settings).setOnClickListener(clickListener);
        view.findViewById(R.id.bookingHistory).setOnClickListener(clickListener);
    }

    private void initViews(View view) {
        bookingHistory = view.findViewById(R.id.bookingHistory);
        myAvatar = view.findViewById(R.id.myAvatar);
        usernametv = view.findViewById(R.id.usernametv);
        useremailtv = view.findViewById(R.id.useremailtv);
        view5 = view.findViewById(R.id.view5);

        if (SPUtils.getInstance().getBoolean(AppConstans.Administrator)){
            bookingHistory.setVisibility(View.GONE);
            view5.setVisibility(View.GONE);
        }
    }

}

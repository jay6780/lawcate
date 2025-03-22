package com.law.booking.activity.Fragments.UserFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.law.booking.R;
import com.law.booking.activity.tools.Model.Button_class;
import com.law.booking.activity.tools.adapter.Button_adapter;
import com.law.booking.activity.tools.adapter.lawAdapter;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

public class Home_user_fragment extends Fragment {
    private RecyclerView button_recycler;
    private Button_adapter button_adapter;
    private LinearLayout linear_btn;
    private Banner page_banner;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_user_fragment, container, false);
        button_recycler = view.findViewById(R.id.button_recycler);
        linear_btn = view.findViewById(R.id.linear_btn);
        page_banner = view.findViewById(R.id.page_banner);
        linear_btn.setVisibility(View.VISIBLE);

        final List<Integer> images = new ArrayList<>();
        images.add(R.mipmap.law1);
        images.add(R.mipmap.law2);
        page_banner.setAdapter(new lawAdapter(images))
                .setIndicator(new CircleIndicator(getContext()))
                .setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(Object data, int position) {
                    }
                })
                .start();

        List<Button_class> button_list = new ArrayList<>();
        button_list.add(new Button_class("Nearby Firms"));
        button_list.add(new Button_class("Corporate Law"));
        button_list.add(new Button_class("Criminal Law"));
        button_list.add(new Button_class("Tax Law"));
        button_list.add(new Button_class("Human Rights Law"));
        button_list.add(new Button_class("Contract Law"));
        button_list.add(new Button_class("Family Law"));

        button_adapter = new Button_adapter(getContext(),button_list);
        GridLayoutManager layoutManager=new GridLayoutManager(getContext(),2);
        button_recycler.setLayoutManager(layoutManager);
        button_recycler.setAdapter(button_adapter);
        return view;
    }
}
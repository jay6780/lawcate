package com.law.booking.activity.MainPageActivity.Guess;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.law.booking.R;
import com.law.booking.activity.Fragments.UserFragment.FacedectionFragment;

public class facedetection extends AppCompatActivity {
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_fragment);
        imageUrl = getIntent().getStringExtra("imageUrl");

        if (savedInstanceState == null) {
            FacedectionFragment facedectionFragment = new FacedectionFragment();
            Bundle args = new Bundle();
            args.putString("imageUrl", imageUrl);
            facedectionFragment.setArguments(args);

            // Begin fragment transaction
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, facedectionFragment);
            transaction.commit();
        }
    }
}

package com.law.booking.activity.MainPageActivity.Provider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.law.booking.R;
import com.law.booking.activity.tools.adapter.ImagePagerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FullScreenImage_cover extends AppCompatActivity {
    ImageView backbtn, download;
    ViewPager2 viewPager;
    List<String> imageUrls;
    String image;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        setContentView(R.layout.full_screen_coverphoto);
        backbtn = findViewById(R.id.back);
        download = findViewById(R.id.download);
        viewPager = findViewById(R.id.view_pager);

        Intent intent = getIntent();
        imageUrls = intent.getStringArrayListExtra("image_list");
        position = intent.getIntExtra("position", 0);
        image = intent.getStringExtra("image");


        if (imageUrls != null) {
            ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageUrls);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(position, false);
        }

        backbtn.setOnClickListener(view -> onBackPressed());
        download.setOnClickListener(view -> savedphoto(imageUrls.get(viewPager.getCurrentItem())));
    }


    private void savedphoto(String imageUrl) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String fileName = "downloaded_image_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        Glide.with(FullScreenImage_cover.this)
                .asBitmap()
                .load(imageUrl) // Use the provided imageUrl instead of a global variable
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        try (FileOutputStream out = new FileOutputStream(file)) {
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            Toast.makeText(getApplicationContext(), "Image saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }
    public void onBackPressed(){
        finish();
        super.onBackPressed();
    }
}

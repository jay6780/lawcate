package com.law.booking.activity.MainPageActivity.Provider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.law.booking.activity.MainPageActivity.profile.providerProfile2;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.ImagePagerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FullScreenImage_profile_user extends AppCompatActivity {
    ImageView backbtn, download,avatar;
    ViewPager2 viewPager;
    List<String> imageUrls;
    String caption,usernameString,image;
    int position;
    TextView caption_text,username;

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
        setContentView(R.layout.fullscreen_menu56);
        caption_text = findViewById(R.id.caption_text);
        username = findViewById(R.id.username);
        backbtn = findViewById(R.id.back);
        download = findViewById(R.id.download);
        viewPager = findViewById(R.id.view_pager);
        avatar = findViewById(R.id.avatar);

        Intent intent = getIntent();
        caption = intent.getStringExtra("caption");
        imageUrls = intent.getStringArrayListExtra("image_list");
        position = intent.getIntExtra("position", 0);
        usernameString = intent.getStringExtra("username");
        image = intent.getStringExtra("image");
        username.setText(usernameString);
        caption_text.setText(caption);

        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.baseline_person_24)
                .circleCrop()
                .error(R.mipmap.man)
                .into(avatar);


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

        Glide.with(FullScreenImage_profile_user.this)
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
        Intent back = new Intent(getApplicationContext(), providerProfile2.class);
        back.putExtra("username", SPUtils.getInstance().getString(AppConstans.providerName));
        back.putExtra("address",SPUtils.getInstance().getString(AppConstans.address));
        back.putExtra("age", SPUtils.getInstance().getString(AppConstans.provider_age));
        back.putExtra("lengthOfservice",SPUtils.getInstance().getString(AppConstans.service_length));
        back.putExtra("image",SPUtils.getInstance().getString(AppConstans.image));
        back.putExtra("email",SPUtils.getInstance().getString(AppConstans.email));
        back.putExtra("key",SPUtils.getInstance().getString(AppConstans.key));
        back.putExtra("description",SPUtils.getInstance().getString(AppConstans.description));
        startActivity(back);
        finish();
        overridePendingTransition(0,0);
        super.onBackPressed();
    }
}

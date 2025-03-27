package com.law.booking.activity.MainPageActivity.Admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.law.booking.R;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.adapter.Photo_adapter;
import com.orhanobut.dialogplus.DialogPlus;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ly.kite.photopicker.Photo;
import ly.kite.photopicker.PhotoPicker;

public class CoverUpload extends AppCompatActivity {
    private ImageView addphoto, add, back;
    private DialogPlus dialogPlus;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private static final int REQUEST_CODE_PHOTO_PICKER = 1;
    private List<Uri> imageUris = new ArrayList<>();
    private AppCompatButton saved;
    private RecyclerView photo_recycler;
    private Photo_adapter imageAdapter;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cover_upload);
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        storageReference = FirebaseStorage.getInstance().getReference("Cover_photo");
        databaseReference = FirebaseDatabase.getInstance().getReference("Cover_photo")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        addphoto = findViewById(R.id.ImageService);
        add = findViewById(R.id.add);
        back = findViewById(R.id.back);
        saved = findViewById(R.id.saved);
        title = findViewById(R.id.title);
        title.setText("Upload cover photo");
        title.setTextSize(18);
        photo_recycler = findViewById(R.id.photo_recycler);
        photo_recycler.setLayoutManager(new GridLayoutManager(this, 3));

        imageAdapter = new Photo_adapter(this, imageUris);
        photo_recycler.setAdapter(imageAdapter);

        back.setOnClickListener(view -> onBackPressed());
        add.setVisibility(View.GONE);

        initClickers();
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    private void initClickers() {
        addphoto.setOnClickListener(v -> openImagePicker());

        saved.setOnClickListener(view -> {
            if (!imageUris.isEmpty()) {
                showConfirmationDialog();
            } else {
                Toast.makeText(CoverUpload.this, "Please select images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Upload")
                .setMessage("Are you sure you want to save these images?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    for (Uri uri : imageUris) {
                        uploadImageToFirebase(uri);
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    imageUris.clear();
                    imageAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
                .show();
    }


    private void openImagePicker() {
        PhotoPicker.startPhotoPickerForResult(this, REQUEST_CODE_PHOTO_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            Photo[] photos = PhotoPicker.getResultPhotos(data);
            if (photos.length > 0) {
                for (Photo photo : photos) {
                    imageUris.add(Uri.parse(photo.getUri().toString()));
                }
                imageAdapter.notifyDataSetChanged();
            }
            Log.i("MultiUpload", "User selected " + photos.length + " photos");

        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                addphoto.setImageURI(resultUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop failed: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        dialogPlus = new Dialog().loadingDialog(this);
        dialogPlus.show();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            while (baos.toByteArray().length / 1024 > 500 && quality > 0) {
                baos.reset();
                quality -= 5;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            byte[] data = baos.toByteArray();
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
            UploadTask uploadTask = fileReference.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot ->
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveImageUrlToDatabase(uri.toString());
                        dialogPlus.dismiss();
                    }).addOnFailureListener(e -> {
                        dialogPlus.dismiss();
                        Toast.makeText(CoverUpload.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    })
            ).addOnFailureListener(e -> {
                dialogPlus.dismiss();
                Toast.makeText(CoverUpload.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            dialogPlus.dismiss();
            Toast.makeText(CoverUpload.this, "Failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        DatabaseReference userPortfolioRef = databaseReference;

        String key = userPortfolioRef.push().getKey();
        if (key != null) {
            userPortfolioRef.child(key).child("imageUrl").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to upload image to database", Toast.LENGTH_SHORT).show()
                    );
        }
    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
package com.law.booking.activity.pdf;

import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.law.booking.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class pdf_activity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    private static final String TAG = "pdf_activity";
    private String fileUrl,title;
    private PDFView pdfView;
    private ImageView btn_back5,download_btn;
    private TextView pdftitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        changeStatusBarColor(getResources().getColor(R.color.purple_theme));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        btn_back5 = findViewById(R.id.btn_back5);
        pdfView = findViewById(R.id.pdfView);
        download_btn = findViewById(R.id.download_btn);
        download_btn.setVisibility(View.VISIBLE);
        pdftitle = findViewById(R.id.title);
        fileUrl = getIntent().getStringExtra("fileUrl");
        title = getIntent().getStringExtra("title");
        pdftitle.setText(title);
        pdftitle.setTextSize(15);
        if (fileUrl != null && !fileUrl.isEmpty()) {
            downloadAndDisplayPdf(fileUrl);
        } else {
            Toast.makeText(this, "Invalid file URL", Toast.LENGTH_SHORT).show();
            finish();
        }
        btn_back5.setOnClickListener(view -> onBackPressed());
        download_btn.setOnClickListener(view -> savePdfToStorage(fileUrl));
    }

    private void savePdfToStorage(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(this, "Invalid file URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(fileUrl);

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(title);
            request.setDescription("Downloading PDF...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setDestinationInExternalPublicDir("/Download", title + ".pdf");

            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "DownloadManager not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving PDF", e);
            Toast.makeText(this, "Failed to download PDF", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            savePdfToStorage(fileUrl);
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    private void downloadAndDisplayPdf(String fileUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(pdf_activity.this, "Failed to download PDF", Toast.LENGTH_SHORT).show());
                    return;
                }

                InputStream inputStream = connection.getInputStream();
                File pdfFile = new File(getCacheDir(), "temp.pdf");
                FileOutputStream outputStream = new FileOutputStream(pdfFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();
                runOnUiThread(() -> displayPdfFromUri(Uri.fromFile(pdfFile)));

            } catch (Exception e) {
                Log.e(TAG, "Error downloading or displaying PDF", e);
                runOnUiThread(() -> Toast.makeText(pdf_activity.this, "Error displaying PDF", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void displayPdfFromUri(Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(0)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10)
                .pageFitPolicy(FitPolicy.WIDTH)
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        setTitle(String.format("Page %d of %d", page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        Log.d(TAG, "PDF loaded with " + nbPages + " pages.");
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0,0);
        super.onBackPressed();
    }
}

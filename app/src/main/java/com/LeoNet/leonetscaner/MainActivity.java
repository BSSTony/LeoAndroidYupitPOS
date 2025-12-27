package com.LeoNet.leonetscaner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BARCODE_API";
    private static final int REQUEST_CODE_PICK_FOLDER = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_FOLDER_URI = "folder_uri";
    private static final int REQUEST_CODE_BARCODE_SCAN = 222;

    private ViewPager2 viewPager;
    private Button btnScan;
    private ProgressDialog loader;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private int currentPage = 0;
    private Uri folderUri;

    private final Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            if (!imageUris.isEmpty()) {
                currentPage = (currentPage + 1) % imageUris.size();
                viewPager.setCurrentItem(currentPage, true);
            }
            handler.postDelayed(this, 4000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        setContentView(R.layout.activity_main);
        hideSystemUI();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        viewPager = findViewById(R.id.viewPager);
        btnScan = findViewById(R.id.btnScan);
        loader = new ProgressDialog(this);
        loader.setCancelable(false);

        if (viewPager == null || btnScan == null) {
            Toast.makeText(this, "UI error: Check XML", Toast.LENGTH_LONG).show();
            return;
        }

        viewPager.setAdapter(new SlideAdapterFromUri(this, imageUris));
        setupViewPagerAnimation();
        handler.postDelayed(slideRunnable, 4000);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUri = prefs.getString(KEY_FOLDER_URI, null);

        if (savedUri != null) {
            folderUri = Uri.parse(savedUri);
            loadImagesFromFolder(folderUri);
        } else {
            selectFolder();
        }

        btnScan.setOnClickListener(v -> {
            Intent i = new Intent(this, BarcodeScanActivity.class);
            startActivityForResult(i, REQUEST_CODE_BARCODE_SCAN);
        });

    }

    private void selectFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ===== FOLDER PICKER (UNCHANGED) =====
        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == RESULT_OK && data != null) {
            folderUri = data.getData();
            if (folderUri == null) return;

            int flags = data.getFlags() &
                    (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            getContentResolver().takePersistableUriPermission(folderUri, flags);

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_FOLDER_URI, folderUri.toString())
                    .apply();

            loadImagesFromFolder(folderUri);
            return;
        }

        // ===== BARCODE SCAN (CameraX Activity) =====
        if (requestCode == REQUEST_CODE_BARCODE_SCAN
                && resultCode == RESULT_OK
                && data != null) {

            String barcode = data.getStringExtra("barcode");
            if (barcode != null && !barcode.isEmpty()) {
                callBarcodeApi(barcode);   // 🔥 Your existing API logic
            }
        }
    }


    private void loadImagesFromFolder(Uri treeUri) {
        loader.setMessage("Loading images...");
        loader.show();

        new Thread(() -> {
            ArrayList<Uri> temp = new ArrayList<>();
            DocumentFile folder = DocumentFile.fromTreeUri(this, treeUri);

            if (folder != null && folder.isDirectory()) {
                for (DocumentFile file : folder.listFiles()) {
                    String name = file.getName();
                    if (file.isFile() && name != null &&
                            (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
                        temp.add(file.getUri());
                    }
                }
            }

            runOnUiThread(() -> {
                loader.dismiss();
                imageUris.clear();
                imageUris.addAll(temp);
                viewPager.getAdapter().notifyDataSetChanged();

                if (temp.isEmpty()) {
                    Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void setupViewPagerAnimation() {
        viewPager.setOffscreenPageLimit(3);
        RecyclerView rv = (RecyclerView) viewPager.getChildAt(0);
        rv.setClipChildren(false);
        rv.setClipToPadding(false);
        rv.setPadding(80, 0, 80, 0);
        rv.addItemDecoration(new MarginItemDecoration(20));
    }

    private void callBarcodeApi(String barcode) {
        loader.setMessage("Fetching product...");
        loader.show();
        btnScan.setEnabled(false);

        String url = "http://yupitpos.neoit.tech:9060/api/products/barcode/" + barcode;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-API-KEY", "G7x9!vB3qR$kP8zL1mT6wF0sH2nY4aD5")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    loader.dismiss();
                    btnScan.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> loader.dismiss());

                try {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            btnScan.setEnabled(true);
                            Toast.makeText(MainActivity.this,
                                    "Product not found", Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    JSONObject data = new JSONObject(response.body().string())
                            .getJSONObject("data");
                    Intent i = new Intent(MainActivity.this, ProductDetailsActivity.class);
                    i.putExtra("productCode", data.getString("productCode"));
                    i.putExtra("name", data.getString("name"));
                    i.putExtra("description", data.getString("description"));
                    i.putExtra("price", data.getDouble("sellingPrice"));
                    i.putExtra("qty", data.getInt("quantityAvailable"));
                    i.putExtra("img", data.getString("imageUrl"));
                    runOnUiThread(() -> {
                        btnScan.setEnabled(true);
                        startActivity(i);
                    });

                } catch (Exception e) {
                    Log.e(TAG, "JSON Error", e);
                    runOnUiThread(() -> {
                        btnScan.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                "Something went wrong", Toast.LENGTH_LONG).show();
                    });
                }
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(slideRunnable);
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    // ================= ADAPTER =================
    public static class SlideAdapterFromUri extends RecyclerView.Adapter<SlideAdapterFromUri.ViewHolder> {
        private final ArrayList<Uri> images;
        public SlideAdapterFromUri(Context context, ArrayList<Uri> images) {
            this.images = images;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView img = new ImageView(parent.getContext());
            img.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setBackgroundColor(Color.WHITE);
            return new ViewHolder(img);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imageView.setImageURI(images.get(position));
        }
        @Override
        public int getItemCount() {
            return images.size();
        }
        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }

    public static class MarginItemDecoration extends RecyclerView.ItemDecoration {
        private final int margin;
        public MarginItemDecoration(int margin) {
            this.margin = margin;
        }
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = margin;
            outRect.right = margin;
        }
    }
}

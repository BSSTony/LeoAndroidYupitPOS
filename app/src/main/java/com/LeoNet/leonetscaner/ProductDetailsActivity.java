package com.LeoNet.leonetscaner;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class ProductDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetails";
    String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        TextView tvCode = findViewById(R.id.tvCode);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvDesc = findViewById(R.id.tvDesc);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvQty = findViewById(R.id.tvQty);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageView imgProduct = findViewById(R.id.imgProduct);
        ProgressBar imgLoader = findViewById(R.id.imgLoader);
        CardView imgCard = findViewById(R.id.imgCard);

        String productCode = getIntent().getStringExtra("productCode");
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        double price = getIntent().getDoubleExtra("price", 0);
        int qty = getIntent().getIntExtra("qty", 0);
         imageUrl = getIntent().getStringExtra("img");

        tvCode.setText("Code: " + productCode);
        tvName.setText(name);
        tvDesc.setText(description);
        tvPrice.setText("$ " + price);
        tvQty.setText("Stock: " + qty);

        tvQty.setTextColor(qty > 0 ? Color.parseColor("#2ECC71") : Color.parseColor("#E74C3C"));

        btnBack.setOnClickListener(v -> finish());

//        if (imageUrl == null || imageUrl.trim().isEmpty()) {
//            imageUrl = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";
//        }

        imgCard.setVisibility(View.INVISIBLE);
        imgLoader.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource) {

                        Log.e(TAG, "Image load failed", e);
                        imgLoader.setVisibility(View.GONE);
                        imgCard.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource) {

                        imgLoader.setVisibility(View.GONE);
                        imgCard.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imgProduct);
    }
}

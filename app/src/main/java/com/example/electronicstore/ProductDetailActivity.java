package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.electronicstore.model.Product;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription, backText;
    private Button addToCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        addToCartButton = findViewById(R.id.addToCartButton);
        backText = findViewById(R.id.btn_back);

        backText.setOnClickListener(v -> {
            startActivity(new Intent(ProductDetailActivity.this, MainActivity.class));
        });

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
            String price = intent.getStringExtra("price");
            String description = intent.getStringExtra("description");
            String imageUrl = intent.getStringExtra("imageUrl");

            productName.setText(name);
            productPrice.setText(price);
            productDescription.setText(description);
            Glide.with(this).load(imageUrl).into(productImage);
        }

        addToCartButton.setOnClickListener(view -> {
            Toast.makeText(this, "Add product to cart successfully!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
        });
    }
}

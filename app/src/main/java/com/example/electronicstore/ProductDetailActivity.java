package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription, productAvailability, backText;
    private Button addToCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        productAvailability = findViewById(R.id.productAvailability);
        addToCartButton = findViewById(R.id.addToCartButton);
        backText = findViewById(R.id.btn_back);

        backText.setOnClickListener(v -> {
            startActivity(new Intent(ProductDetailActivity.this, MainActivity.class));
        });

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
            int price = intent.getIntExtra("price", 0);
            String description = intent.getStringExtra("description");
            String imageUrl = intent.getStringExtra("imageUrl");

            boolean isAvailable = true;
            if (intent.hasExtra("isAvailable")) {
                isAvailable = intent.getBooleanExtra("isAvailable", true);
            }

            productName.setText(name);
            productPrice.setText(formatPrice(price));
            productDescription.setText(description);
            Glide.with(this).load(imageUrl).into(productImage);

            if (isAvailable) {
                productAvailability.setText("Available");
                productAvailability.setTextColor(getResources().getColor(R.color.green));
                addToCartButton.setEnabled(true);
                addToCartButton.setBackgroundColor(getResources().getColor(R.color.blue));
            } else {
                productAvailability.setText("Out of Stock");
                productAvailability.setTextColor(getResources().getColor(R.color.red));
                addToCartButton.setEnabled(false);
                addToCartButton.setBackgroundColor(getResources().getColor(R.color.gray));
            }
        }


        addToCartButton.setOnClickListener(view -> {
            Toast.makeText(this, "Add product to cart successfully!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
        });
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}

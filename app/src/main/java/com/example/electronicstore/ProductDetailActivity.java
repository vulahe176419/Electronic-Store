package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.electronicstore.model.Cart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription, productAvailability;
    private ImageView backText;

    private Button addToCartButton, btnReview;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("carts");

        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        productAvailability = findViewById(R.id.productAvailability);
        btnReview = findViewById(R.id.btnReview);
        addToCartButton = findViewById(R.id.addToCartButton);
        backText = findViewById(R.id.btn_back);

        backText.setOnClickListener(v -> {
            startActivity(new Intent(ProductDetailActivity.this, MainActivity.class));
        });

        btnReview.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, ProductReviewsActivity.class);
            intent.putExtra("productId", productId); // Đảm bảo productId được truyền
            startActivity(intent);
        });

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
            int price = intent.getIntExtra("price", 0);
            String description = intent.getStringExtra("description");
            String imageUrl = intent.getStringExtra("imageUrl");
            productId = intent.getStringExtra("productId");

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

            addToCartButton.setOnClickListener(view -> {
                addProductToCart(productId, name, price, imageUrl);
            });
        }

    }

    private void addProductToCart(String productId, String productName, int price, String imageUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to add to cart!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProductDetailActivity.this, LoginActivity.class));
            return;
        }

        String userId = user.getUid();
        String cartId = databaseReference.push().getKey();
        if (cartId == null) {
            Toast.makeText(this, "Failed to generate cart ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        Cart cart = new Cart(
                cartId,
                cartId.hashCode(),
                imageUrl,
                price,
                productId,
                productName,
                1,
                userId
        );

        databaseReference.child(cartId).setValue(cart)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product added to cart successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}

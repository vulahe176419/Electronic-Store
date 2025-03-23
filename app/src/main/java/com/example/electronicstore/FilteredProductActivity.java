package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.adapter.MainProductAdapter;
import com.example.electronicstore.model.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilteredProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MainProductAdapter productAdapter;
    private List<Product> productList;
    private DatabaseReference productsRef;
    private String categoryId, categoryName;
    private TextView categoryTitle;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_product);

        categoryTitle = findViewById(R.id.categoryTitle);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryName != null) {
            categoryTitle.setText("All products of " + categoryName);
        }
        productList = new ArrayList<>();
        productAdapter = new MainProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        if (categoryId != null) {
            loadProductsByCategory();
        } else {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            finish();
        }

        auth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(FilteredProductActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(FilteredProductActivity.this, CategoryFilterActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(FilteredProductActivity.this, CartActivity.class));
                } else {
                    startActivity(new Intent(FilteredProductActivity.this, LoginActivity.class));
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(FilteredProductActivity.this, SettingsActivity.class));
                } else {
                    startActivity(new Intent(FilteredProductActivity.this, BeforeLoginActivity.class));
                }
                return true;
            } else {
                return false;
            }
        });
    }

    private void loadProductsByCategory() {
        productsRef.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Product product = data.getValue(Product.class);
                            if (product != null) {
                                product.setFormattedPrice(formatPrice(product.getPrice()));
                                productList.add(product);
                            }
                        }
                        productAdapter.setProducts(productList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FilteredProductActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VND";
    }
}

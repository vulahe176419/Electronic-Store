package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.adapter.ProductAdapter;
import com.example.electronicstore.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ProductManagerActivity extends AppCompatActivity {

    private RecyclerView productListRecycler;
    private Button addProductButton, addCategoryButton;
    private DatabaseReference databaseReference;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ImageView backText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_manager);

        productListRecycler = findViewById(R.id.product_list_recycler);
        addProductButton = findViewById(R.id.add_product_button);
        addCategoryButton = findViewById(R.id.add_category_button);
        backText = findViewById(R.id.btn_back);

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);

        productListRecycler.setLayoutManager(new LinearLayoutManager(this));
        productListRecycler.setAdapter(productAdapter);

        productAdapter.setOnProductClickListener((product, productId) -> {
            Intent intent = new Intent(ProductManagerActivity.this, EditProductActivity.class);
            intent.putExtra("product_pid", productId);
            startActivity(intent);
        });

        fetchProducts();

        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductManagerActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        addCategoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductManagerActivity.this, AddCategoryActivity.class);
            startActivity(intent);
        });

        backText.setOnClickListener(v -> {
            startActivity(new Intent(ProductManagerActivity.this, AdminDashboardActivity.class));
        });
    }

    private void fetchProducts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setPid(dataSnapshot.getKey());
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductManagerActivity.this,
                        "Failed to load products: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
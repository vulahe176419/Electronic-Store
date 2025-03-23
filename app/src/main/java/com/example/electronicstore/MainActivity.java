package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicstore.adapter.MainProductAdapter;
import com.example.electronicstore.model.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private MainProductAdapter productAdapter;
    private List<Product> productList;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference productsRef;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(MainActivity.this, CategoryFilterActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(MainActivity.this, CartActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, BeforeLoginActivity.class));
                }
                return true;
            } else {
                return false;
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();
        productAdapter = new MainProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null) {
                        product.setPid(child.getKey());
                        productList.add(product);
                    }
                }
                productAdapter.setProducts(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load products: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        productList = new ArrayList<>();
        productAdapter = new MainProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);

        productsRef = FirebaseDatabase.getInstance().getReference("products");
        loadAllProducts();
        setupSearch();
    }

    private void loadAllProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        product.setPid(snapshot.getKey());
                        String formattedPrice = formatPrice(product.getPrice());
                        product.setFormattedPrice(formattedPrice);
                        productList.add(product);
                    }
                }
                productAdapter.setProducts(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    loadAllProducts();
                } else {
                    searchProducts(newText);
                }
                return true;
            }
        });
    }

    private void searchProducts(String keyword) {
        String searchKeyword = keyword.toLowerCase();
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null && product.getName() != null) {
                        String productName = product.getName().toLowerCase();
                        if (productName.contains(searchKeyword)) {
                            product.setPid(snapshot.getKey());
                            String formattedPrice = formatPrice(product.getPrice());
                            product.setFormattedPrice(formattedPrice);
                            productList.add(product);
                        }
                    }
                }
                productAdapter.setProducts(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
    private void fetchProductsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }

                productAdapter.notifyDataSetChanged();
            }

            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

}

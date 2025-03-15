package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.adapter.CategoryAdapter;
import com.example.electronicstore.adapter.ProductAdapter;
import com.example.electronicstore.model.Category;
import com.example.electronicstore.model.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button logoutButton;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private RecyclerView categoryRecyclerView;
    private List<Category> categoryList;
    private CategoryAdapter categoryAdapter;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        bottomNavigationView = findViewById(R.id.bottomNav);
        auth = FirebaseAuth.getInstance();

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_category) {
//                        startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_cart) {
//                        startActivity(new Intent(MainActivity.this, CartActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_profile) {
                        if (auth.getCurrentUser() != null) {
                            startActivity(new Intent(MainActivity.this, PersonalAfterLoginActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, PersonalActivity.class));
                        }
                        return true;
                    } else {
                        return false;
                    }
        });

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        fetchProductsFromFirebase();

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();
        categoryList.add(new Category("Laptops", R.drawable.ic_laptop));
        categoryList.add(new Category("Phones", R.drawable.ic_phone));
        categoryList.add(new Category("Headphones", R.drawable.ic_headphone));

        categoryAdapter = new CategoryAdapter(categoryList, this);
        categoryRecyclerView.setAdapter(categoryAdapter);


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

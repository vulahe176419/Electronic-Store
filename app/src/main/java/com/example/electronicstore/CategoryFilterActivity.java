package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.adapter.CategoryAdapter;
import com.example.electronicstore.model.Category;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryFilterActivity extends AppCompatActivity {
    private RecyclerView recyclerViewCategories;
    private DatabaseReference categoryRef;
    private List<Category> categoryList;
    private CategoryAdapter categoryAdapter;
    private FirebaseAuth auth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_filter);

        auth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        return true;
                    } else if (itemId == R.id.nav_category) {
                        startActivity(new Intent(CategoryFilterActivity.this, MainActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_cart) {
                        if (auth.getCurrentUser() != null) {
                            startActivity(new Intent(CategoryFilterActivity.this, CartActivity.class));
                        } else {
                            startActivity(new Intent(CategoryFilterActivity.this, LoginActivity.class));
                        }
                        return true;
                    } else if (itemId == R.id.nav_profile) {
                        if (auth.getCurrentUser() != null) {
                            startActivity(new Intent(CategoryFilterActivity.this, BeforeLoginActivity.class));
                        } else {
                            startActivity(new Intent(CategoryFilterActivity.this, SettingsActivity.class));
                        }
                        return true;
                    } else {
                        return false;
                    }
                });

        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            Intent intent = new Intent(CategoryFilterActivity.this, FilteredProductActivity.class);
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("categoryName", category.getName());
            startActivity(intent);
        });
        recyclerViewCategories.setAdapter(categoryAdapter);

        categoryRef = FirebaseDatabase.getInstance().getReference("categories");
        loadCategories();
    }

    private void loadCategories() {
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Category category = data.getValue(Category.class);
                    if (category != null) {
                        categoryList.add(category);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryFilterActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }
    }

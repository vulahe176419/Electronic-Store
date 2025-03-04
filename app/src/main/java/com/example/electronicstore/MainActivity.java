package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button logoutButton;
    private SearchView searchView;
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        searchView = findViewById(R.id.searchView);
        productRecyclerView = findViewById(R.id.recyclerView);
        logoutButton = findViewById(R.id.buttonLogout);

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        productList = new ArrayList<>();
        productList.add(new Product("Laptop Dell", "12,000,000 VND", R.drawable.laptop_dell));
        productList.add(new Product("iPhone 15 Pro", "25,000,000 VND", R.drawable.iphone_15));
        productList.add(new Product("Tai nghe Sony", "1,500,000 VND", R.drawable.sony_headphone));

        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList);
        productRecyclerView.setAdapter(productAdapter);
    }
}

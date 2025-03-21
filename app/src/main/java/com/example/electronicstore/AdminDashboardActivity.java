package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private CardView userManagerText, productManagerText;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();

        userManagerText = findViewById(R.id.user_manager);
        productManagerText= findViewById(R.id.product_manager);
        logoutButton = findViewById(R.id.btn_logout);

        userManagerText.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, UserManagerActivity.class));
        });

        productManagerText.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ProductManagerActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(AdminDashboardActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AdminDashboardActivity.this, MainActivity.class));
            finish();
        });

    }
}
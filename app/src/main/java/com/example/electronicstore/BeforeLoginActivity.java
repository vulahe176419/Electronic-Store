package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class BeforeLoginActivity extends AppCompatActivity {
        private FirebaseAuth auth;
        private TextView loginText, signupText;
        private Button logoutButton;
        private BottomNavigationView bottomNavigationView;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_before_login);

            auth = FirebaseAuth.getInstance();

            loginText = findViewById(R.id.btn_login);
            signupText = findViewById(R.id.btn_signup);
//            logoutButton = findViewById(R.id.btn_logout);

            loginText.setOnClickListener(v -> {
                startActivity(new Intent(BeforeLoginActivity.this, LoginActivity.class));
            });

            signupText.setOnClickListener(v -> {
                startActivity(new Intent(BeforeLoginActivity.this, SignupActivity.class));
            });

            bottomNavigationView = findViewById(R.id.bottomNav);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_category) {
                    startActivity(new Intent(BeforeLoginActivity.this, CategoryFilterActivity.class));
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    if (auth.getCurrentUser() != null) {
                        startActivity(new Intent(BeforeLoginActivity.this, CartActivity.class));
                    } else {
                        startActivity(new Intent(BeforeLoginActivity.this, LoginActivity.class));
                    }
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    if (auth.getCurrentUser() != null) {
                        startActivity(new Intent(BeforeLoginActivity.this, SettingsActivity.class));
                    } else {
                        startActivity(new Intent(BeforeLoginActivity.this, BeforeLoginActivity.class));
                    }
                    return true;
                } else {
                    return false;
                }
            });
        }
    }
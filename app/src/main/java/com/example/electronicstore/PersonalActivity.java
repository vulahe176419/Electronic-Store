package com.example.electronicstore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class PersonalActivity extends AppCompatActivity {
        private FirebaseAuth auth;
        private TextView loginText, signupText;
        private Button logoutButton;
        private BottomNavigationView bottomNavigationView;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_personal);

            auth = FirebaseAuth.getInstance();

            loginText = findViewById(R.id.btn_login);
            signupText = findViewById(R.id.btn_signup);
            logoutButton = findViewById(R.id.btn_logout);

            loginText.setOnClickListener(v -> {
                startActivity(new Intent(PersonalActivity.this, LoginActivity.class));
            });

            signupText.setOnClickListener(v -> {
                startActivity(new Intent(PersonalActivity.this, SignupActivity.class));
            });

            logoutButton.setOnClickListener(v -> {
                auth.signOut();
                Toast.makeText(PersonalActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PersonalActivity.this, LoginActivity.class));
                finish();
            });

            bottomNavigationView = findViewById(R.id.bottomNav);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(PersonalActivity.this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.nav_category) {
//                        startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                    return true;
                } else if (itemId == R.id.nav_cart) {
//                        startActivity(new Intent(MainActivity.this, CartActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(PersonalActivity.this, PersonalActivity.class));
                    return true;
                } else {
                    return false;
                }
            });
        }
    }
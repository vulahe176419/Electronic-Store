package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class PersonalAfterLoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button logoutButton;

    private Button addressButton;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_after_login);

        auth = FirebaseAuth.getInstance();

        logoutButton = findViewById(R.id.btn_logoutt);

            logoutButton.setOnClickListener(v -> {
                auth.signOut();
                Toast.makeText(PersonalAfterLoginActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PersonalAfterLoginActivity.this, PersonalActivity.class));
                finish();
            });

        bottomNavigationView = findViewById(R.id.bottomNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(PersonalAfterLoginActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_category) {
//                        startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
//                        startActivity(new Intent(MainActivity.this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(PersonalAfterLoginActivity.this, PersonalAfterLoginActivity.class));
                } else {
                    startActivity(new Intent(PersonalAfterLoginActivity.this, PersonalActivity.class));
                }
                return true;
            } else {
                return false;
            }
        });

        addressButton = findViewById(R.id.btn_address);
        addressButton.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalAfterLoginActivity.this, ManageAddress.class);
            startActivity(intent);
        });

    }
}
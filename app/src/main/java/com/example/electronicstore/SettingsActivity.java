package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LinearLayout logoutButton;
    private LinearLayout addressButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        auth = FirebaseAuth.getInstance();

        logoutButton = findViewById(R.id.btn_logoutt);

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(SettingsActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, PersonalActivity.class));
            finish();
        });

        addressButton = findViewById(R.id.btn_address);
        addressButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ManageAddress.class);
            startActivity(intent);
        });
    }
}
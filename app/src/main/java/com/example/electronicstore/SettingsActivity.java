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

    private Button btnPendingConfirmation;
    private Button btnPendingPickup;
    private Button btnPendingDelivery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        logoutButton = findViewById(R.id.btn_logoutt);
        addressButton = findViewById(R.id.btn_address);
        btnPendingConfirmation = findViewById(R.id.btnPendingConfirmation);
        btnPendingPickup = findViewById(R.id.btnPendingPickup);
        btnPendingDelivery = findViewById(R.id.btnPendingDelivery);

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(SettingsActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, PersonalActivity.class));
            finish();
        });


        addressButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ManageAddress.class);
            startActivity(intent);
        });

        btnPendingConfirmation.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, OrderManagementActivity.class);
            intent.putExtra("selected_tab_position", 0); // Truyền trạng thái "pending"
            startActivity(intent);
        });

        btnPendingPickup.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, OrderManagementActivity.class);
            intent.putExtra("selected_tab_position", 1); // Truyền trạng thái "pending_pickup"
            startActivity(intent);
        });

        btnPendingDelivery.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, OrderManagementActivity.class);
            intent.putExtra("selected_tab_position", 2); // Truyền trạng thái
            startActivity(intent);
        });

    }
}
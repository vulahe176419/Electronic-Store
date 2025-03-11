package com.example.electronicstore;

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

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextView userManagerText;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();

        userManagerText = findViewById(R.id.user_manager);
        logoutButton = findViewById(R.id.btn_logout);

        userManagerText.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, UserManagerActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(AdminDashboardActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish();
        });

    }
}
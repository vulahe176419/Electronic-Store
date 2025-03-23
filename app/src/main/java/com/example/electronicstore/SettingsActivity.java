package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LinearLayout logoutButton, btnOrder, addressButton, btnContact, btnFAQ, btnIssue, btnTerm;
    private BottomNavigationView bottomNavigationView;
    private Button btnEditProfile;
    private TextView tvUserName, tvUserEmail;
    private DatabaseReference databaseReference;
    private ImageView btnNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(SettingsActivity.this, CategoryFilterActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(SettingsActivity.this, CartActivity.class));
                } else {
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (auth.getCurrentUser() != null) {
                    return true;
                } else {
                    startActivity(new Intent(SettingsActivity.this, BeforeLoginActivity.class));
                }
                return true;
            } else {
                return false;
            }
        });
        FirebaseUser user = auth.getCurrentUser();

        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);

        if (user != null) {
            tvUserEmail.setText(user.getEmail());
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            tvUserName.setText(name);
                        } else {
                            tvUserName.setText("No Name");
                        }
                    } else {
                        tvUserName.setText("No Name Found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SettingsActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            tvUserName.setText("Guest");
            tvUserEmail.setText("Not Logged In");
        }

        logoutButton = findViewById(R.id.btn_logoutt);
        addressButton = findViewById(R.id.btn_address);
        btnOrder = findViewById(R.id.btn_order);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnContact = findViewById(R.id.btn_contact_support);
        btnFAQ = findViewById(R.id.btn_faqs);
        btnIssue = findViewById(R.id.btn_report_issue);
        btnTerm = findViewById(R.id.btn_terms_policies);
        btnNotification = findViewById(R.id.btn_notification);

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(SettingsActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, BeforeLoginActivity.class));
            finish();
        });

        btnContact.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this,ContactSupportActivity.class);
            startActivity(intent);
        });

        btnFAQ.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this,FAQsActivity.class);
            startActivity(intent);
        });

        btnIssue.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this,ReportIssueActivity.class);
            startActivity(intent);
        });

        btnTerm.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this,TermsPoliciesActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });


        addressButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ManageAddress.class);
            startActivity(intent);
        });


        btnOrder.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, OrderManagementActivity.class);
            startActivity(intent);
        });

        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

    }
}
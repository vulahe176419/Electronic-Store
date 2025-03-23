package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etRepeatPassword;
    private Button btnSave;
    private ImageView ivBack;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        btnSave = findViewById(R.id.btnSave);
        ivBack = findViewById(R.id.ivBack);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            getUserData();
        }

        btnSave.setOnClickListener(v -> updateUserData());
        ivBack.setOnClickListener(v -> {
            startActivity(new Intent(EditProfileActivity.this, SettingsActivity.class));
        });
    }

    private void getUserData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);

                    etName.setText(name);
                    etEmail.setText(email);
                    etPassword.setText(password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserData() {
        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();
        String repeatPassword = etRepeatPassword.getText().toString().trim();

        if (newEmail.contains("@admin")) {
            Toast.makeText(EditProfileActivity.this, "Cannot change email with '@admin'", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(repeatPassword)) {
            Toast.makeText(EditProfileActivity.this, "Re-passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("name").setValue(newName);
        databaseReference.child("email").setValue(newEmail);
        databaseReference.child("password").setValue(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Update success", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfileActivity.this, SettingsActivity.class));
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.electronicstore.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditUserActivity extends AppCompatActivity {
    private EditText nameEdit, emailEdit, passwordEdit;
    private Button saveButton, deleteButton;
    private DatabaseReference userRef;
    private String targetUid, currentUid;
    private ImageView backText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        nameEdit = findViewById(R.id.name_edit);
        emailEdit = findViewById(R.id.email_edit);
        passwordEdit = findViewById(R.id.password_edit);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
        backText = findViewById(R.id.btn_back);

        targetUid = getIntent().getStringExtra("user_uid");
        if (targetUid == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUid = currentUser.getUid();
        } else {
            currentUid = null;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);
                    nameEdit.setText(name);
                    emailEdit.setText(email);
                    passwordEdit.setText(password);
                } else {
                    Toast.makeText(EditUserActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditUserActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setOnClickListener(v -> saveUser());
        deleteButton.setOnClickListener(v -> deleteAccount());

        backText.setOnClickListener(v -> finish());
    }

    private void saveUser() {
        String newName = nameEdit.getText().toString().trim();
        String newEmail = emailEdit.getText().toString().trim();
        String newPassword = passwordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("name").setValue(newName);
        userRef.child("email").setValue(newEmail);
        userRef.child("password").setValue(newPassword);

        Toast.makeText(this, "User data updated.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(EditUserActivity.this, UserManagerActivity.class));
        finish();
    }

    private void deleteAccount() {
        if (currentUid == null) {
            Toast.makeText(this, "You must be logged in to delete accounts.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUid.equals(targetUid)) {
            Toast.makeText(this, "You cannot delete your own account.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "User data deleted from database. Authentication deletion requires server-side action.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(EditUserActivity.this, UserManagerActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}

package com.example.electronicstore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private TextView loginText;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailEditText = findViewById(R.id.in_email);
        passwordEditText = findViewById(R.id.in_password);
        confirmPasswordEditText = findViewById(R.id.in_repassword);
        signupButton = findViewById(R.id.btn_register);
        loginText = findViewById(R.id.btn_login);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        signupButton.setOnClickListener(v -> registerUser());

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in the information completely!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Re-Password doesn't match!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Signing up...");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "Sign up successes!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "Sign up failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
//        logoutButton = findViewById(R.id.buttonLogout);
//
//        logoutButton.setOnClickListener(v -> {
//            auth.signOut();
//            Toast.makeText(MainActivity.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(MainActivity.this, LoginActivity.class));
//            finish();
//        });


        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }
}

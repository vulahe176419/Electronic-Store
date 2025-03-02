package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // No need to set a content view since we’re redirecting immediately
//        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser != null) {
//            // User is signed in, go to HomeActivity
//            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//            startActivity(intent);
//            finish(); // Remove MainActivity from back stack
//        } else {
//            // User is not signed in, go to LoginActivity
//            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish(); // Remove MainActivity from back stack
//        }

//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(MainActivity.this, ChoiceLoginActivity.class);
//
//                startActivity(intent);
//
//                finish();
//            }
//        }, 3000);

//        mAuth = FirebaseAuth.getInstance();
//
////        String email = "test@gmail.com";
////        String password = "Test@123";
//
//        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()){
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d("Main", "createUserWithEmail:success");
//                    FirebaseUser user = mAuth.getCurrentUser();
//
//                    Toast.makeText(getApplicationContext(), user.getEmail(), Toast.LENGTH_LONG).show();
//                    //updateUI(user);
//                }else{
//                    // If sign in fails, display a message to the user.
//                    Log.w("Main", "createUserWithEmail:failure", task.getException());
//                    Toast.makeText(MainActivity.this, "Authentication failed.",
//                            Toast.LENGTH_SHORT).show();
//                    //updateUI(null);
//                }
//            }
//
//        });
    }
}
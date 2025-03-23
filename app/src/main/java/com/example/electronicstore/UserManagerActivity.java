package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.adapter.UserAdapter;
import com.example.electronicstore.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UserManagerActivity extends AppCompatActivity {

    private RecyclerView userListRecycler;
    private Button addUserButton;
    private DatabaseReference databaseReference;
    private UserAdapter userAdapter;
    private List<User> userList;
    private ImageView backText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manager);

        userListRecycler = findViewById(R.id.user_list_recycler);
        addUserButton = findViewById(R.id.add_user_button);
        backText = findViewById(R.id.btn_back);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);

        userListRecycler.setLayoutManager(new LinearLayoutManager(this));
        userListRecycler.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener((user, userId) -> {
            Intent intent = new Intent(UserManagerActivity.this, EditUserActivity.class);
            intent.putExtra("user_uid", userId);
            startActivity(intent);
        });

        fetchUsers();

        addUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserManagerActivity.this, AddUserActivity.class);
            startActivity(intent);
        });

        backText.setOnClickListener(v -> finish());
    }

    private void fetchUsers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUid(dataSnapshot.getKey());
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagerActivity.this,
                        "Failed to load users: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
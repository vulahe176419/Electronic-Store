package com.example.electronicstore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.adapter.NotificationAdapter;
import com.example.electronicstore.model.Notification;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private ImageView backText;
    private TextView tvNoNotifications;
    private DatabaseReference notificationRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backText = findViewById(R.id.btn_back);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        notificationRef = FirebaseDatabase.getInstance().getReference("notifications");

        loadNotifications();

        backText.setOnClickListener(v -> finish());
    }

    private void loadNotifications() {
        notificationRef.orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Notification notification = data.getValue(Notification.class);
                    if (notification != null) {
                        notificationList.add(notification);
                    }
                }

                if (notificationList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvNoNotifications.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoNotifications.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load notifications", error.toException());
            }
        });
    }
}

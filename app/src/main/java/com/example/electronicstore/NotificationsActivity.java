package com.example.electronicstore;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicstore.adapter.NotificationAdapter;
import com.example.electronicstore.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private ImageView backText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backText = findViewById(R.id.btn_back);

        notificationList = new ArrayList<>();
        notificationList.add(new Notification("New Order", "You have a new order from Electronic Store.", "5m ago", R.drawable.ic_notification));
        notificationList.add(new Notification("Payment Received", "Your payment has been processed successfully.", "10m ago", R.drawable.ic_payment));
        notificationList.add(new Notification("Delivery Update", "Your order is out for delivery.", "30m ago", R.drawable.ic_delivery));

        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        backText.setOnClickListener(v -> finish());
    }
}

package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class OrderConfirmationActivity extends AppCompatActivity {
    private TextView totalText, paymentMethodText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_confirmation);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Order Confirmed");

        // Nhận dữ liệu từ CheckoutActivity
        Intent intent = getIntent();
        double total = intent.getDoubleExtra("total", 0.0);
        String paymentMethod = intent.getStringExtra("paymentMethod");

        // Khởi tạo các view
        totalText = findViewById(R.id.totalText);
        paymentMethodText = findViewById(R.id.paymentMethodText);

        // Hiển thị thông tin
        totalText.setText(String.format("%.0f VND", total));
        paymentMethodText.setText(paymentMethod != null ? paymentMethod : "N/A");

        // Nút Continue Shopping
        Button continueShoppingButton = findViewById(R.id.continueShoppingButton);
        continueShoppingButton.setOnClickListener(v -> {
            Intent cartIntent = new Intent(OrderConfirmationActivity.this, CartActivity.class);
            cartIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa các Activity trung gian
            startActivity(cartIntent);
            finish();
        });

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
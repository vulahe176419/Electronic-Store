package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.DecimalFormat;
import java.util.Objects;

public class OrderConfirmationActivity extends AppCompatActivity {
    private TextView totalText, paymentMethodText, addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_confirmation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Order Confirmed");

        Intent intent = getIntent();
        double total = intent.getDoubleExtra("total", 0.0);
        String paymentMethod = intent.getStringExtra("paymentMethod");
        String address = intent.getStringExtra("address");

        totalText = findViewById(R.id.totalText);
        paymentMethodText = findViewById(R.id.paymentMethodText);
        addressText = findViewById(R.id.addressText);

        DecimalFormat formatter = new DecimalFormat("#,###");
        totalText.setText(formatter.format(total) + " VND");
        paymentMethodText.setText(paymentMethod != null ? paymentMethod : "N/A");
        addressText.setText(address != null ? address : "N/A");

        Button continueShoppingButton = findViewById(R.id.continueShoppingButton);
        continueShoppingButton.setOnClickListener(v -> {
            Intent cartIntent = new Intent(OrderConfirmationActivity.this, MainActivity.class);
            cartIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(cartIntent);
            finish();
        });

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
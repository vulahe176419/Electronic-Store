package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class CheckoutActivity extends AppCompatActivity {
    private TextView subtotalText, shippingText, totalText;
    private RadioGroup paymentMethodsGroup;
    private double subtotal, shipping = 15000, total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Checkout");

        Intent intent = getIntent();
        subtotal = intent.getDoubleExtra("subtotal", 0.0);
        total = subtotal + shipping;

        subtotalText = findViewById(R.id.subtotalText);
        shippingText = findViewById(R.id.shippingText);
        totalText = findViewById(R.id.totalText);
        paymentMethodsGroup = findViewById(R.id.paymentMethodsGroup);

        subtotalText.setText(String.format("%.0f VND", subtotal));
        shippingText.setText(String.format("%.0f VND", shipping));
        totalText.setText(String.format("%.0f VND", total));

        Button confirmPaymentButton = findViewById(R.id.confirmPaymentButton);
        confirmPaymentButton.setOnClickListener(v -> {
            String paymentMethod = getSelectedPaymentMethod();
            Toast.makeText(this, "Payment confirmed with " + paymentMethod, Toast.LENGTH_SHORT).show();

            Intent confirmationIntent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
            confirmationIntent.putExtra("total", total);
            confirmationIntent.putExtra("paymentMethod", paymentMethod);
            startActivity(confirmationIntent);

            finish();
        });

        // Xử lý nút back trên Toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private String getSelectedPaymentMethod() {
        int selectedId = paymentMethodsGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.codRadio) {
            return "Cash on Delivery";
        } else if (selectedId == R.id.cardRadio) {
            return "Credit/Debit Card";
        } else if (selectedId == R.id.walletRadio) {
            return "Digital Wallet";
        }
        return "Cash on Delivery"; // Mặc định
    }
}
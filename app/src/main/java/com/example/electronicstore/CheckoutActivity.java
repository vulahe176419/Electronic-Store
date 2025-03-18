package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.util.Objects;

public class CheckoutActivity extends AppCompatActivity {
    private TextView subtotalText, shippingText, totalText;
    private RadioGroup paymentMethodsGroup, addressRadioGroup;
    private double subtotal;
    private final double shipping = 15000;
    private double total;
    private final String uid = "P66DVircwtPMiHxmkBeLvLOxbV13";
    private DatabaseReference databaseReference;

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
        addressRadioGroup = findViewById(R.id.addressRadioGroup);

        databaseReference = FirebaseDatabase.getInstance().getReference("addresses");

        DecimalFormat formatter = new DecimalFormat("#,###");
        subtotalText.setText(formatter.format(subtotal) + " VND");
        shippingText.setText(formatter.format(shipping) + " VND");
        totalText.setText(formatter.format(total) + " VND");

        loadAddressesFromRealtimeDatabase();

        Button confirmPaymentButton = findViewById(R.id.confirmPaymentButton);
        confirmPaymentButton.setOnClickListener(v -> {
            String paymentMethod = getSelectedPaymentMethod();
            String selectedAddress = getSelectedAddress();
            if (selectedAddress == null) {
                Toast.makeText(this, "Please select an address!", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Payment confirmed with " + paymentMethod + " at " + selectedAddress, Toast.LENGTH_SHORT).show();
            Intent confirmationIntent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
            confirmationIntent.putExtra("total", total);
            confirmationIntent.putExtra("paymentMethod", paymentMethod);
            confirmationIntent.putExtra("address", selectedAddress);
            startActivity(confirmationIntent);
            finish();
        });

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadAddressesFromRealtimeDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                addressRadioGroup.removeAllViews();
                int idCounter = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.child("userId").getValue(String.class);
                    if (userId != null && userId.equals(uid)) {
                        String addressLine1 = snapshot.child("addressLine1").getValue(String.class);
                        if (addressLine1 != null) {
                            RadioButton radioButton = new RadioButton(CheckoutActivity.this);
                            radioButton.setId(View.generateViewId());
                            radioButton.setText(addressLine1);
                            radioButton.setTag(addressLine1);
                            addressRadioGroup.addView(radioButton);

                            if (idCounter == 0) {
                                radioButton.setChecked(true);
                            }
                            idCounter++;
                        }
                    }
                }
                if (addressRadioGroup.getChildCount() == 0) {
                    Toast.makeText(CheckoutActivity.this, "No addresses found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CheckoutActivity.this, "Error loading addresses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private String getSelectedAddress() {
        int selectedId = addressRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return null; // Không có địa chỉ nào được chọn
        }
        RadioButton selectedRadioButton = findViewById(selectedId);
        return (String) selectedRadioButton.getTag(); // Lấy addressLine1 từ tag
    }
}
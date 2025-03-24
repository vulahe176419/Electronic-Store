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

import com.example.electronicstore.model.Cart;
import com.example.electronicstore.model.Order;
import com.example.electronicstore.model.OrderDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class CheckoutActivity extends AppCompatActivity {
    private TextView subtotalText, shippingText, totalText;
    private RadioGroup paymentMethodsGroup, addressRadioGroup;
    private double subtotal;
    private final double shipping = 15000;
    private double total;
    private String uid;
    private DatabaseReference addressesReference;
    private DatabaseReference cartsReference;
    private DatabaseReference ordersReference;
    private DatabaseReference orderDetailsReference;
    private FirebaseAuth auth;
    private List<Cart> carts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        uid = user.getUid();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Checkout");

        Intent intent = getIntent();
        subtotal = intent.getDoubleExtra("subtotal", 0.0);
        total = subtotal + shipping;
        carts = intent.getParcelableArrayListExtra("carts");

        subtotalText = findViewById(R.id.subtotalText);
        shippingText = findViewById(R.id.shippingText);
        totalText = findViewById(R.id.totalText);
        paymentMethodsGroup = findViewById(R.id.paymentMethodsGroup);
        addressRadioGroup = findViewById(R.id.addressRadioGroup);

        addressesReference = FirebaseDatabase.getInstance().getReference("addresses");
        cartsReference = FirebaseDatabase.getInstance().getReference("carts");
        ordersReference = FirebaseDatabase.getInstance().getReference("orders");
        orderDetailsReference = FirebaseDatabase.getInstance().getReference("orderDetails");

        DecimalFormat formatter = new DecimalFormat("#,###");
        subtotalText.setText(formatter.format(subtotal) + " VND");
        shippingText.setText(formatter.format(shipping) + " VND");
        totalText.setText(formatter.format(total) + " VND");

        loadAddressesFromRealtimeDatabase();

        Button confirmPaymentButton = findViewById(R.id.confirmPaymentButton);

        confirmPaymentButton.setOnClickListener(v -> confirmCheckout());

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadAddressesFromRealtimeDatabase() {
        addressesReference.addValueEventListener(new ValueEventListener() {
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

    private void confirmCheckout() {
        String paymentMethod = getSelectedPaymentMethod();
        String selectedAddress = getSelectedAddress();
        if (selectedAddress == null) {
            Toast.makeText(this, "Please select an address!", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId = ordersReference.push().getKey();
        if (orderId == null) {
            Toast.makeText(this, "Failed to generate order ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        String trackingNumber = "Track" + new Random().nextInt(100);
        String orderDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Order order = new Order();
        order.setUserId(uid);
        order.setOrderDate(orderDate);
        order.setTotalPrice((long) total);
        order.setAddress(selectedAddress);
        order.setStatus("pending");
        order.setTrackingNumber(trackingNumber);

        ordersReference.child(orderId).setValue(order)
                .addOnSuccessListener(aVoid -> {
                    addOrderDetails(orderId);

                    createOrderNotification(uid, orderId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void addOrderDetails(String orderId) {
        for (Cart cart : carts) {
            String detailId = orderDetailsReference.push().getKey();
            if (detailId == null) {
                continue;
            }

            OrderDetail orderDetail = new OrderDetail(
                    orderId,
                    cart.getProductId(),
                    cart.getQuantity()
            );

            orderDetailsReference.child(detailId).setValue(orderDetail);
        }

        deleteUserCarts();
    }

    private void deleteUserCarts() {
        cartsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String cartUserId = snapshot.child("userId").getValue(String.class);
                    if (cartUserId != null && cartUserId.equals(uid)) {
                        snapshot.getRef().removeValue();
                    }
                }
                Intent confirmationIntent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
                confirmationIntent.putExtra("total", total);
                confirmationIntent.putExtra("paymentMethod", getSelectedPaymentMethod());
                confirmationIntent.putExtra("address", getSelectedAddress());
                startActivity(confirmationIntent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CheckoutActivity.this, "Failed to clear cart: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
        return "Cash on Delivery";
    }

    private void createOrderNotification(String userId, String orderId) {
        String title = "Order Placed Successfully!";
        String message = "Your order #" + orderId + " has been placed. You can track its status in Orders.";

        com.example.electronicstore.utils.NotificationUtils.createNotification(userId, title, message);
    }

    private String getSelectedAddress() {
        int selectedId = addressRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return null;
        }
        RadioButton selectedRadioButton = findViewById(selectedId);
        return (String) selectedRadioButton.getTag();
    }
}
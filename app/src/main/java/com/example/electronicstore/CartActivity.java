package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicstore.adapter.CartAdapter;
import com.example.electronicstore.model.Cart;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemChangeListener {
    private CartAdapter adapter;
    private List<Cart> carts;
    private TextView subtotalText, totalText;
    private DatabaseReference databaseReference;
    private double subtotal = 0.0;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to view your cart!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CartActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Shopping Cart");

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        RecyclerView recyclerView = findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carts = new ArrayList<>();
        adapter = new CartAdapter(carts, this);
        recyclerView.setAdapter(adapter);

        subtotalText = findViewById(R.id.subtotalText);
        totalText = findViewById(R.id.totalText);

        databaseReference = FirebaseDatabase.getInstance().getReference("carts");
        loadProductsFromRealtimeDatabase(user.getUid());

        Button checkoutButton = findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("subtotal", subtotal);
            intent.putParcelableArrayListExtra("carts", new ArrayList<>(carts));
            startActivity(intent);
        });
    }

    private void loadProductsFromRealtimeDatabase(String userId) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                carts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();

                    String cartUserId = snapshot.child("userId").getValue(String.class);
                    if (cartUserId != null && cartUserId.equals(userId)) {
                        Integer cartId = snapshot.child("cartId").getValue(Integer.class);
                        if (cartId == null) cartId = 0;

                        String productId = snapshot.child("productId").getValue(String.class);
                        if (productId == null) productId = "";

                        String productName = snapshot.child("productName").getValue(String.class);
                        if (productName == null) productName = "";

                        Double price = snapshot.child("price").getValue(Double.class);
                        if (price == null) price = 0.0;

                        Integer quantity = snapshot.child("quantity").getValue(Integer.class);
                        if (quantity == null) quantity = 0;

                        String imageUrl = snapshot.child("image").getValue(String.class);
                        if (imageUrl == null) imageUrl = "";

                        Cart item = new Cart(
                                id,
                                cartId,
                                imageUrl,
                                price,
                                productId,
                                productName,
                                quantity,
                                cartUserId
                        );
                        carts.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
                updateTotal();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CartActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotal() {
        subtotal = 0;
        for (Cart item : carts) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        double shipping = 15000;
        double total = subtotal + shipping;
        DecimalFormat formatter = new DecimalFormat("#,###");
        subtotalText.setText(formatter.format(subtotal) + " VND");
        totalText.setText(formatter.format(total) + " VND");
    }

    @Override
    public void onQuantityChanged(Cart item) {
        databaseReference.child(item.getId()).child("quantity").setValue(item.getQuantity())
                .addOnSuccessListener(aVoid -> updateTotal())
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onItemRemoved(Cart item) {
        databaseReference.child(item.getId()).removeValue()
                .addOnSuccessListener(aVoid -> updateTotal())
                .addOnFailureListener(e -> Toast.makeText(this, "Xóa lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
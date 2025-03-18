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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Shopping Cart");

        RecyclerView recyclerView = findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carts = new ArrayList<>();
        adapter = new CartAdapter(carts, this);
        recyclerView.setAdapter(adapter);

        subtotalText = findViewById(R.id.subtotalText);
        totalText = findViewById(R.id.totalText);

        databaseReference = FirebaseDatabase.getInstance().getReference("carts");
        loadProductsFromRealtimeDatabase();

        Button checkoutButton = findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("subtotal", subtotal);
            startActivity(intent);
        });
    }

    private void loadProductsFromRealtimeDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                carts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey(); // Lấy ID của document

                    Integer cartId = snapshot.child("CartId").getValue(Integer.class);
                    if (cartId == null) cartId = 0;

                    Integer productId = snapshot.child("ProductId").getValue(Integer.class);
                    if (productId == null) productId = 0;

                    String productName = snapshot.child("ProductName").getValue(String.class);
                    if (productName == null) productName = "";

                    Double price = snapshot.child("Price").getValue(Double.class);
                    if (price == null) price = 0.0;

                    Integer quantity = snapshot.child("Quantity").getValue(Integer.class);
                    if (quantity == null) quantity = 0;

                    String imageUrl = snapshot.child("Image").getValue(String.class);
                    if (imageUrl == null) imageUrl = "";

                    Integer userId = snapshot.child("UserId").getValue(Integer.class);
                    if (userId == null) userId = 0;

                    Cart item = new Cart(
                            id,
                            cartId,
                            imageUrl,
                            price,
                            productId,
                            productName,
                            quantity,
                            userId
                    );
                    carts.add(item);
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
        databaseReference.child(item.getId()).child("Quantity").setValue(item.getQuantity())
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
package com.example.electronicstore;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.R;
import com.example.electronicstore.adapter.OrderProductAdapter;
import com.example.electronicstore.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {
    private String orderId;
    private DatabaseReference databaseReference;
    private ValueEventListener orderListener;
    private TextView txtStatus, txtTrackingNumber, txtOrderDate, txtAddress, txtTotalPrice;
    private ImageButton btnBack;

    private RecyclerView recyclerViewProducts;
    private OrderProductAdapter productAdapter;
    private List<Pair<Order, Integer>> orderWithQuantityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Ánh xạ các view
        txtStatus = findViewById(R.id.txtStatus);
        txtTrackingNumber = findViewById(R.id.txtTrackingNumber);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtAddress = findViewById(R.id.txtAddress);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnBack = findViewById(R.id.btnBack);;
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);

        // Thiết lập RecyclerView
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        orderWithQuantityList = new ArrayList<>();
        productAdapter = new OrderProductAdapter(this, orderWithQuantityList);
        recyclerViewProducts.setAdapter(productAdapter);

        // Nhận orderId từ Intent
        orderId = getIntent().getStringExtra("orderId");
        Log.d("OrderDetailActivity", "Order ID: " + orderId);

        if (orderId == null) {
            Toast.makeText(this, "Order code not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Tải dữ liệu đơn hàng và danh sách sản phẩm
        loadOrderDetails();
        loadOrderProducts();

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());

    }

    private void loadOrderDetails() {
        orderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String orderDate = snapshot.child("orderDate").getValue(String.class);
                    Long totalPrice = snapshot.child("totalPrice").getValue(Long.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String trackingNumber = snapshot.child("trackingNumber").getValue(String.class);

                    // Hiển thị trạng thái
                    String statusText;
                    switch (status != null ? status : "") {
                        case "delivered":
                            statusText = "Order delivered";
                            break;
                        case "pending":
                            statusText = "Order is processing";
                            break;
                        case "returned":
                            statusText = "Paid Order";
                            break;
                        default:
                            statusText = "Status: " + (status != null ? status : "N/A");
                    }
                    txtStatus.setText(statusText);

                    txtTrackingNumber.setText("Shipping code: " + (trackingNumber != null ? trackingNumber : "N/A"));
                    txtOrderDate.setText("Order Date: " + (orderDate != null ? orderDate : "N/A"));
                    txtAddress.setText(address != null ? address : "N/A");
                    txtTotalPrice.setText("Pay: ₫" + (totalPrice != null ? String.format("%,d", totalPrice) : "0"));

                } else {
                    Toast.makeText(OrderDetailActivity.this, "Order not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Data loading error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.child("orders").child(orderId).addValueEventListener(orderListener);
    }

    private void loadOrderProducts() {
        databaseReference.child("orderDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderWithQuantityList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderIdFromSnapshot = dataSnapshot.child("orderId").getValue(String.class);
                    String productId = dataSnapshot.child("productId").getValue(String.class);
                    Integer quantity = dataSnapshot.child("quantity").getValue(Integer.class);

                    if (orderId.equals(orderIdFromSnapshot) && productId != null && quantity != null) {
                        Order order = new Order();
                        order.setProductId(productId);
                        orderWithQuantityList.add(new Pair<>(order, quantity));
                        productAdapter.setProducts(orderWithQuantityList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Data loading error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            databaseReference.child("orders").child(orderId).removeEventListener(orderListener);
        }
    }
}
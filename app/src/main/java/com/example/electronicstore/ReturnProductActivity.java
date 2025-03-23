package com.example.electronicstore;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReturnProductActivity extends AppCompatActivity {

    private String orderId, productId;
    private ImageView imgProduct;
    private TextView txtProductName, txtQuantity;
    private EditText edtReturnReason;
    private Button btnSubmitReturn;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_product);

        // Nhận dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        productId = getIntent().getStringExtra("productId");

        // Khởi tạo các view
        imgProduct = findViewById(R.id.imgProduct);
        txtProductName = findViewById(R.id.txtProductName);
        txtQuantity = findViewById(R.id.txtQuantity);
        edtReturnReason = findViewById(R.id.edtReturnReason);
        btnSubmitReturn = findViewById(R.id.btnSubmitReturn);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        // Tải thông tin sản phẩm
        loadProductInfo();

        // Xử lý nút gửi yêu cầu trả hàng
        btnSubmitReturn.setOnClickListener(v -> submitReturnRequest());
    }

    private void loadProductInfo() {
        if (productId == null || orderId == null) {
            Toast.makeText(this, "No product information found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin sản phẩm từ "products"
        databaseReference.child("products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    txtProductName.setText(name != null ? name : "Không xác định");
                    if (imageUrl != null) {
                        Glide.with(ReturnProductActivity.this).load(imageUrl).into(imgProduct);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReturnProductActivity.this, "Product loading error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Lấy số lượng từ "orderDetails"
        databaseReference.child("orderDetails").orderByChild("orderId").equalTo(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int quantity = 0;
                        for (DataSnapshot detailSnapshot : snapshot.getChildren()) {
                            String detailProductId = detailSnapshot.child("productId").getValue(String.class);
                            if (productId.equals(detailProductId)) {
                                Integer qty = detailSnapshot.child("quantity").getValue(Integer.class);
                                quantity = qty != null ? qty : 1;
                                break;
                            }
                        }
                        txtQuantity.setText("Quantity: " + quantity);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ReturnProductActivity.this, "Quantity Load Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitReturnRequest() {
        String reason = edtReturnReason.getText().toString().trim();
        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter reason for return!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        String requestId = databaseReference.child("returnRequests").push().getKey();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> returnRequest = new HashMap<>();
        returnRequest.put("requestId", requestId);
        returnRequest.put("orderId", orderId);
        returnRequest.put("productId", productId);
        returnRequest.put("userId", userId);
        returnRequest.put("reason", reason);
        returnRequest.put("status", "pending");
        returnRequest.put("requestDate", date);

        // Lấy totalPrice từ đơn hàng để hoàn tiền
        databaseReference.child("orders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long totalPrice = snapshot.child("totalPrice").getValue(Long.class);
                returnRequest.put("refundAmount", totalPrice);

                databaseReference.child("returnRequests").child(requestId).setValue(returnRequest)
                        .addOnSuccessListener(aVoid -> {
                            databaseReference.child("orders").child(orderId).child("status").setValue("returned")
                                    .addOnSuccessListener(aVoid1 -> {
                                        processRefund(userId, totalPrice);
                                        Toast.makeText(ReturnProductActivity.this, "Return request successful!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(ReturnProductActivity.this, "Status update error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(ReturnProductActivity.this, "Error sending request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReturnProductActivity.this, "Error loading order data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processRefund(String userId, Long totalPrice) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        String refundId = dbRef.child("refunds").push().getKey();
        Map<String, Object> refund = new HashMap<>();
        refund.put("refundId", refundId);
        refund.put("userId", userId);
        refund.put("orderId", orderId);
        refund.put("amount", totalPrice);
        refund.put("status", "pending");
        refund.put("refundDate", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        dbRef.child("refunds").child(refundId).setValue(refund)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Refund request submitted!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Refund processing error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
package com.example.electronicstore.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.electronicstore.R;
import com.example.electronicstore.RateProduct;
import com.example.electronicstore.ReturnProductActivity;
import com.example.electronicstore.model.Order;
import com.example.electronicstore.model.Product;
import com.example.electronicstore.OrderDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final Map<String, Product> productMap;
    private final Map<String, Integer> totalItemsMap;
    private String currentFilter;
    private DatabaseReference reviewsRef;
    private FirebaseAuth auth;

    public OrderAdapter(Context context, List<Order> orderList, Map<String, Product> productMap,
                        Map<String, Integer> totalItemsMap, String currentFilter) {
        this.context = context;
        this.orderList = orderList;
        this.productMap = productMap;
        this.totalItemsMap = totalItemsMap;
        this.currentFilter = currentFilter;
        this.reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");
        this.auth = FirebaseAuth.getInstance();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, originalPrice, totalItems, totalPrice, deliveryStatus;
        ImageView productImage;
        Button cancelButton, returnButton, reviewButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            originalPrice = itemView.findViewById(R.id.originalPrice);
            totalItems = itemView.findViewById(R.id.totalItems);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            deliveryStatus = itemView.findViewById(R.id.deliveryStatus);
            cancelButton = itemView.findViewById(R.id.cancelButton);
            returnButton = itemView.findViewById(R.id.returnButton);
            reviewButton = itemView.findViewById(R.id.reviewButton);
        }
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        Product product = productMap.get(order.getProductId());

        if (product != null) {
            holder.productName.setText(product.getName());
            holder.originalPrice.setText("" + product.getPrice());
            Glide.with(context).load(product.getImageUrl()).into(holder.productImage);
        } else {
            holder.productName.setText("No products found");
            holder.originalPrice.setText("0");
        }

        Integer totalItems = totalItemsMap.get(order.getOrderId());
        holder.totalItems.setText("Total product: " + (totalItems != null ? totalItems : 0));
        holder.totalPrice.setText("Total amount: ₫" + order.getTotalPrice());
        holder.deliveryStatus.setText("Status: " + order.getStatus());

        // Lấy userId hiện tại
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        String status = order.getStatus();

        // Hiển thị nút dựa trên trạng thái
        holder.cancelButton.setVisibility("pending".equals(status) ? View.VISIBLE : View.GONE);
        holder.returnButton.setVisibility("delivered".equals(status) ? View.VISIBLE : View.GONE);

        // Kiểm tra xem đã có đánh giá từ userId cho productId chưa
        if ("delivered".equals(status) && userId != null) {
            reviewsRef.orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean hasReviewed = false;
                            for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                                String reviewedProductId = reviewSnapshot.child("productId").getValue(String.class);
                                if (order.getProductId().equals(reviewedProductId)) {
                                    hasReviewed = true;
                                    break;
                                }
                            }
                            holder.reviewButton.setVisibility(hasReviewed ? View.GONE : View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Mặc định hiển thị nút nếu có lỗi
                            holder.reviewButton.setVisibility(View.VISIBLE);
                            Toast.makeText(context, "Error checking reviews: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            holder.reviewButton.setVisibility(View.GONE);
        }

        // Sự kiện nhấn vào item để mở chi tiết đơn hàng
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            context.startActivity(intent);
        });

        // Xử lý nút Hủy đơn hàng
        holder.cancelButton.setOnClickListener(v -> {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("orders").child(order.getOrderId());
            dbRef.child("status").setValue("canceled")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Order has been cancelled!", Toast.LENGTH_SHORT).show();
                        orderList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error cancelling order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Xử lý nút Đánh giá
        holder.reviewButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, RateProduct.class);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("productId", order.getProductId());
            ((Activity) context).startActivityForResult(intent, 1001); // Sử dụng startActivityForResult
        });

        // Xử lý nút Trả hàng
        holder.returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReturnProductActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("productId", order.getProductId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateFilter(String filter) {
        this.currentFilter = filter;
        notifyDataSetChanged();
    }
}
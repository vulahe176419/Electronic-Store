package com.example.electronicstore.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.electronicstore.R;
import com.example.electronicstore.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.OrderProductViewHolder> {
    private Context context;
    private List<Pair<Order, Integer>> orderWithQuantityList;
    private DatabaseReference productsRef;

    public OrderProductAdapter(Context context, List<Pair<Order, Integer>> orderWithQuantityList) {
        this.context = context;
        this.orderWithQuantityList = orderWithQuantityList;
        this.productsRef = FirebaseDatabase.getInstance().getReference("products");
    }

    @NonNull
    @Override
    public OrderProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderProductViewHolder holder, int position) {
        Pair<Order, Integer> orderWithQuantity = orderWithQuantityList.get(position);
        Order order = orderWithQuantity.first;
        Integer quantity = orderWithQuantity.second;

        String productId = order.getProductId();

        // Lấy thông tin sản phẩm từ Firebase
        productsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String productName = snapshot.child("name").getValue(String.class);
                    Long price = snapshot.child("price").getValue(Long.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    holder.productName.setText(productName != null ? productName : "N/A");
                    holder.productPrice.setText(price != null ? "" + String.format("%,d", price) : "0");
                    holder.quantity.setText("x" + quantity);

                    if (imageUrl != null) {
                        Glide.with(context).load(imageUrl).into(holder.productImage);
                    } else {
                        holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderWithQuantityList.size();
    }

    public void setProducts(List<Pair<Order, Integer>> orderWithQuantityList) {
        this.orderWithQuantityList = orderWithQuantityList;
        notifyDataSetChanged();
    }

    public static class OrderProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantity;

        public OrderProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantity = itemView.findViewById(R.id.quantity);
        }
    }
}
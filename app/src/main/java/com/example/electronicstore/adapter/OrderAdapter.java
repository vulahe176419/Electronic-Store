package com.example.electronicstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.electronicstore.R;
import com.example.electronicstore.RateProduct;
import com.example.electronicstore.model.Order;
import com.example.electronicstore.model.Product;

import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private Map<String, Product> productMap;
    private Map<String, Integer> totalItemsMap; // Thêm map để lưu tổng số sản phẩm
    private String currentFilter;

    public OrderAdapter(Context context, List<Order> orderList, Map<String, Product> productMap, Map<String, Integer> totalItemsMap, String currentFilter) {
        this.context = context;
        this.orderList = orderList;
        this.productMap = productMap;
        this.totalItemsMap = totalItemsMap;
        this.currentFilter = currentFilter;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, originalPrice, totalItems, totalPrice, deliveryStatus; // Thay discountedPrice bằng totalItems
        ImageView productImage;
        Button returnButton, reviewButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            originalPrice = itemView.findViewById(R.id.originalPrice);
            totalItems = itemView.findViewById(R.id.totalItems); // Thêm totalItems
            totalPrice = itemView.findViewById(R.id.totalPrice);
            deliveryStatus = itemView.findViewById(R.id.deliveryStatus);
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
            holder.originalPrice.setText("₫" + product.getPrice());
        } else {
            holder.productName.setText("Không tìm thấy sản phẩm");
            holder.originalPrice.setText("₫0");
        }

        // Hiển thị tổng số sản phẩm
        Integer totalItems = totalItemsMap.get(order.getOrderId());
        holder.totalItems.setText("Tổng sản phẩm: " + (totalItems != null ? totalItems : 0));

        holder.totalPrice.setText("Tổng số tiền: ₫" + order.getTotalPrice());
        holder.deliveryStatus.setText("Trạng thái: " + order.getStatus());

        if (product != null) {
            Glide.with(context).load(product.getImageUrl()).into(holder.productImage);
        }

        // Hiển thị nút dựa trên trạng thái
        holder.returnButton.setVisibility("returned".equals(order.getStatus()) ? View.VISIBLE : View.GONE);
        holder.reviewButton.setVisibility("delivered".equals(order.getStatus()) ? View.VISIBLE : View.GONE);

        holder.reviewButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, RateProduct.class);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("productId", order.getProductId());
            context.startActivity(intent);
        });

        holder.returnButton.setOnClickListener(v -> {
            // Logic trả hàng
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
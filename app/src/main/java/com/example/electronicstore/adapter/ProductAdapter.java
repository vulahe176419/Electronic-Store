package com.example.electronicstore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.R;
import com.example.electronicstore.model.Product;
import com.example.electronicstore.ProductManagerActivity;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product, String productId);
    }

    public ProductAdapter(List<Product> products, ProductManagerActivity productManagerActivity) {
        this.products = products;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_manager, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.productName.setText(product.getName() != null ? product.getName() : "N/A");
        holder.productPrice.setText(formatPrice(product.getPrice()));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Picasso.get().load(product.getImageUrl()).into(holder.productImage);
        }

        if (product.isAvailable()) {
            holder.productAvailability.setText("Available");
            holder.productAvailability.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
        } else {
            holder.productAvailability.setText("Out of Stock");
            holder.productAvailability.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
        }

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product, product.getPid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void setProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyItemRangeChanged(0, newProducts.size());
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productAvailability;
        Button editButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productAvailability = itemView.findViewById(R.id.productAvailability);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}

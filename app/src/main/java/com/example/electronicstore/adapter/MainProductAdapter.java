package com.example.electronicstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.electronicstore.ProductDetailActivity;
import com.example.electronicstore.R;
import com.example.electronicstore.model.Product;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainProductAdapter extends RecyclerView.Adapter<MainProductAdapter.ViewHolder> {
    private List<Product> products;
    private final Context context;

    public MainProductAdapter(List<Product> products, Context context) {
        this.products = products;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productName.setText(product.getName());

        Picasso.get().load(product.getImageUrl()).into(holder.productImage);
        holder.productPrice.setText(formatPrice(product.getPrice()));
        Glide.with(context).load(product.getImageUrl()).into(holder.productImage);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("name", product.getName());
            intent.putExtra("price", + product.getPrice());
            intent.putExtra("description", product.getDetail());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("isAvailable", product.isAvailable());
            intent.putExtra("productId", product.getPid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
        }
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}
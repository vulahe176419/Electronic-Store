package com.example.electronicstore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.electronicstore.R;
import com.example.electronicstore.model.Cart;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private final List<Cart> carts;
    private final OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged(Cart item);
        void onItemRemoved(Cart item);
    }

    public CartAdapter(List<Cart> carts, OnCartItemChangeListener listener) {
        this.carts = carts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cart item = carts.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.itemImage);
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.itemName.setText(item.getProductName());
        holder.itemPrice.setText((formatter.format(item.getPrice()) + " VND"));
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));

        holder.decreaseButton.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                holder.itemQuantity.setText(String.valueOf(item.getQuantity()));
                if (listener != null) listener.onQuantityChanged(item);
            }
        });

        holder.increaseButton.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            holder.itemQuantity.setText(String.valueOf(item.getQuantity()));
            if (listener != null) listener.onQuantityChanged(item);
        });

        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Cart removedItem = carts.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, carts.size());
                if (listener != null) listener.onItemRemoved(removedItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemPrice, itemQuantity;
        Button decreaseButton, increaseButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
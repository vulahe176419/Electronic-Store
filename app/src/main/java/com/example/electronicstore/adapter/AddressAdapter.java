package com.example.electronicstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.AddAddressActivity;
import com.example.electronicstore.R;
import com.example.electronicstore.model.Address;
import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<Address> addressList = new ArrayList<>();

    private final Context context;
    private int selectedPosition = -1;

    public AddressAdapter(Context context, List<Address> addressList) {
        this.context = context;
        if (addressList != null) {
            this.addressList = addressList;
        }
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        if (address == null) return;

        holder.nameTextView.setText(address.getName() != null ? address.getName() : "");
        holder.addressTextView.setText(address.getAddressLine1() != null ? address.getAddressLine1() : "");
        holder.phoneTextView.setText(address.getPostalCode() != null ? address.getPostalCode() : "");
        holder.defaultLabel.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

        // Accessibility
        holder.nameTextView.setContentDescription("Name address: " + (address.getName() != null ? address.getName() : ""));
        holder.addressTextView.setContentDescription("Address: " + (address.getAddressLine1() != null ? address.getAddressLine1() : ""));
        holder.phoneTextView.setContentDescription("Phone Number: " + (address.getPostalCode() != null ? address.getPostalCode() : ""));


        holder.editButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, AddAddressActivity.class);
                intent.putExtra("address", addressList.get(adapterPosition));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public void updateSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, addressTextView, phoneTextView, defaultLabel;
        RadioButton radioSelect;
        Button editButton;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            addressTextView = itemView.findViewById(R.id.address_text_view);
            phoneTextView = itemView.findViewById(R.id.phone_text_view);
            defaultLabel = itemView.findViewById(R.id.default_label);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }

}
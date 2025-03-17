package com.example.electronicstore;

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
import com.example.electronicstore.model.Address;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private OnAddressSelectedListener listener;
    private Context context;
    private int selectedPosition = -1;

    public AddressAdapter(Context context, List<Address> addressList, OnAddressSelectedListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
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
        holder.nameTextView.setText(address.getName());
        holder.addressTextView.setText(address.getAddressLine1());
        holder.phoneTextView.setText(address.getPostalCode());
        holder.defaultLabel.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

        holder.nameTextView.setContentDescription("Tên địa chỉ: " + address.getName());
        holder.addressTextView.setContentDescription("Địa chỉ: " + address.getAddressLine1());
        holder.phoneTextView.setContentDescription("Số điện thoại: " + address.getPostalCode());
        holder.defaultLabel.setContentDescription("Địa chỉ mặc định: " + (address.isDefault() ? "Đã chọn" : "Chưa chọn"));

        // Xử lý RadioButton
        holder.radioSelect.setChecked(position == selectedPosition);
        holder.radioSelect.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressSelected(address);
                selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
            }
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddAddressActivity.class);
            intent.putExtra("address", address);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
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
            radioSelect = itemView.findViewById(R.id.radio_select);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }

    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }
}
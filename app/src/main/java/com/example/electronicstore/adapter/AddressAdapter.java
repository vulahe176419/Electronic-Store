package com.example.electronicstore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.R;
import com.example.electronicstore.model.AddressOrder;
import java.util.List;
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<AddressOrder> addressOrderList;

    public AddressAdapter(List<AddressOrder> addressOrderList) {
        this.addressOrderList = addressOrderList;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_item_layout, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressOrder addressOrder = addressOrderList.get(position);
        holder.addressText.setText(addressOrder.getAddressLine1());
    }

    @Override
    public int getItemCount() {
        return addressOrderList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView addressText;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            addressText = itemView.findViewById(R.id.addressText);
        }
    }
}

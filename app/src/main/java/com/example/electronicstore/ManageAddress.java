package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.electronicstore.model.Address;
import java.util.ArrayList;
import java.util.List;

public class ManageAddress extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_address);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(ManageAddress.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recycler_view_addresses);
        ImageView backButton = findViewById(R.id.back_button);
        Button addAddressButton = findViewById(R.id.add_address_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList, this::onAddressSelected);
        recyclerView.setAdapter(addressAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("addresses");

        loadAddresses();

        backButton.setOnClickListener(v -> finish());

        addAddressButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageAddress.this, AddAddressActivity.class);
            startActivity(intent);
        });
    }

    private void loadAddresses() {
        String userId = mAuth.getCurrentUser().getUid();
        databaseReference.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        addressList.clear();
                        List<Address> defaultAddresses = new ArrayList<>();
                        List<Address> nonDefaultAddresses = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Address address = snapshot.getValue(Address.class);
                            if (address != null) {
                                address.setKey(snapshot.getKey());
                                if (address.isDefault()) {
                                    defaultAddresses.add(address);
                                } else {
                                    nonDefaultAddresses.add(address);
                                }
                            }
                        }

                        // Đặt địa chỉ mặc định lên đầu
                        addressList.addAll(defaultAddresses);
                        addressList.addAll(nonDefaultAddresses);
                        addressAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ManageAddress.this, "Lỗi tải danh sách địa chỉ: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onAddressSelected(Address selectedAddress) {
        // Xử lý khi người dùng chọn một địa chỉ (ví dụ: trong màn hình thanh toán)
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_address", selectedAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
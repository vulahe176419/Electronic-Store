package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.adapter.AddressAdapter;
import com.example.electronicstore.model.Address;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ManageAddress extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private ValueEventListener addressListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_address);

        initializeFirebase();
        initializeViews();
        setupRecyclerView();
        loadAddresses();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("addresses");
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_addresses);
        progressBar = findViewById(R.id.progress_bar);
        ImageView backButton = findViewById(R.id.back_button);
        Button addAddressButton = findViewById(R.id.add_address_button);

        progressBar.setVisibility(View.VISIBLE); // Hiển thị khi bắt đầu tải dữ liệu

        backButton.setOnClickListener(v -> finish());
        addAddressButton.setOnClickListener(v ->
                startActivity(new Intent(this, AddAddressActivity.class)));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Tối ưu hiệu suất nếu kích thước item không thay đổi
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList, this::onAddressSelected);
        recyclerView.setAdapter(addressAdapter);
    }

    private void loadAddresses() {
        String userId = mAuth.getCurrentUser().getUid();
        addressListener = new ValueEventListener() {
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

                addressList.addAll(defaultAddresses);
                addressList.addAll(nonDefaultAddresses);
                addressAdapter.notifyDataSetChanged();

                if (addressList.isEmpty()) {
                    Toast.makeText(ManageAddress.this, "Chưa có địa chỉ nào", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageAddress.this,
                        "Lỗi tải danh sách địa chỉ: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        };
        databaseReference.orderByChild("userId").equalTo(userId).addValueEventListener(addressListener);
    }

    private void onAddressSelected(Address selectedAddress) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_address", selectedAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addressListener != null && databaseReference != null) {
            databaseReference.removeEventListener(addressListener); // Đảm bảo gỡ listener
        }
    }
}
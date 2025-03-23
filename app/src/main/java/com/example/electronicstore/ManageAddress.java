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

    private RecyclerView recyclerView; // RecyclerView hiển thị danh sách địa chỉ
    private AddressAdapter addressAdapter; // Adapter cho RecyclerView
    private List<Address> addressList; // Danh sách địa chỉ
    private DatabaseReference databaseReference; // Tham chiếu đến Firebase
    private FirebaseAuth mAuth; // Đối tượng FirebaseAuth
    private ProgressBar progressBar; // ProgressBar hiển thị khi tải dữ liệu
    private ValueEventListener addressListener; // Listener cho dữ liệu địa chỉ
    private static final int REQUEST_CODE_ADD_ADDRESS = 100; // Mã yêu cầu khi thêm địa chỉ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_address);

        initializeFirebase(); // Khởi tạo Firebase
        initializeViews(); // Khởi tạo các view
        setupRecyclerView(); // Thiết lập RecyclerView
        loadAddresses(); // Tải danh sách địa chỉ
    }

    // Khởi tạo Firebase và kiểm tra đăng nhập
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("addresses");
    }

    // Ánh xạ và thiết lập sự kiện cho các view
    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_addresses);
        progressBar = findViewById(R.id.progress_bar);
        ImageView backButton = findViewById(R.id.back_button);
        Button addAddressButton = findViewById(R.id.add_address_button);

        progressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar khi bắt đầu tải

        backButton.setOnClickListener(v -> finish()); // Xử lý nút Back
        addAddressButton.setOnClickListener(v ->
                startActivityForResult(new Intent(this, AddAddressActivity.class), REQUEST_CODE_ADD_ADDRESS)); // Mở AddAddressActivity
    }

    // Thiết lập RecyclerView và Adapter
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Tối ưu hiệu suất
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList);
        recyclerView.setAdapter(addressAdapter);
    }

    // Tải danh sách địa chỉ từ Firebase
    private void loadAddresses() {
        String userId = mAuth.getCurrentUser().getUid();
        addressListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                addressList.clear();
                List<Address> defaultAddresses = new ArrayList<>();
                List<Address> nonDefaultAddresses = new ArrayList<>();

                // Phân loại địa chỉ mặc định và không mặc định
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

                // Thêm địa chỉ vào danh sách, ưu tiên địa chỉ mặc định
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

    // Xử lý kết quả trả về từ AddAddressActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_ADDRESS && resultCode == RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selected_address"); // Lấy địa chỉ từ MapActivity
            if (selectedAddress != null) {
                // Tạo đối tượng Address mới để lưu vào Firebase
                Address newAddress = new Address();
                newAddress.setAddressLine1(selectedAddress);
                newAddress.setUserId(mAuth.getCurrentUser().getUid());
                newAddress.setDefault(addressList.isEmpty()); // Đặt mặc định nếu chưa có địa chỉ nào

                // Lưu địa chỉ mới vào Firebase
                String key = databaseReference.push().getKey();
                if (key != null) {
                    databaseReference.child(key).setValue(newAddress)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Địa chỉ đã được thêm", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi thêm địa chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addressListener != null && databaseReference != null) {
            databaseReference.removeEventListener(addressListener); // Gỡ listener để tránh rò rỉ bộ nhớ
        }
    }
}
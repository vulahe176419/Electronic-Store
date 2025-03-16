package com.example.electronicstore;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.electronicstore.model.Address;
import java.io.IOException;
import java.util.List;

public class AddAddressActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_SELECT_MAP = 1002;

    private EditText editName, editPhone, editAddress;
    private CheckBox checkDefault;
    private TextInputLayout tilName, tilPhone, tilAddress;
    private Button deleteButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private Address addressToEdit;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(AddAddressActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("addresses");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Khởi tạo LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d("AddAddress", "LocationResult is null");
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d("AddAddress", "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                    try {
                        Geocoder geocoder = new Geocoder(AddAddressActivity.this);
                        List<android.location.Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            editAddress.setText(addresses.get(0).getAddressLine(0));
                        } else {
                            editAddress.setText("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(AddAddressActivity.this, "Lỗi khi lấy địa chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    // Dừng cập nhật vị trí sau khi nhận được kết quả
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Địa chỉ mới");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        editName = findViewById(R.id.edit_name);
        editPhone = findViewById(R.id.edit_phone);
        editAddress = findViewById(R.id.edit_address);
        checkDefault = findViewById(R.id.check_default);
        tilName = findViewById(R.id.til_name);
        tilPhone = findViewById(R.id.til_phone);
        tilAddress = findViewById(R.id.til_address);
        deleteButton = findViewById(R.id.delete_button);
        Button btnUseCurrentLocation = findViewById(R.id.btn_use_current_location);
        Button btnSelectLocationOnMap = findViewById(R.id.btn_select_location_on_map);
        Button btnComplete = findViewById(R.id.complete_button);

        if (getIntent().hasExtra("address")) {
            addressToEdit = (Address) getIntent().getSerializableExtra("address");
            populateFields(addressToEdit);
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        btnUseCurrentLocation.setOnClickListener(v -> {
            Log.d("AddAddress", "Button clicked");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                requestCurrentLocation();
            }
        });

        btnSelectLocationOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(AddAddressActivity.this, MapActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_MAP);
        });

        btnComplete.setOnClickListener(v -> saveAddress());

        deleteButton.setOnClickListener(v -> deleteAddress());
    }

    private void requestCurrentLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1); // Chỉ lấy vị trí 1 lần

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("AddAddress", "Permissions not granted");
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                .addOnFailureListener(e -> {
                    Log.d("AddAddress", "Failed to request location: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateFields(Address address) {
        editName.setText(address.getName() != null ? address.getName() : "");
        editPhone.setText(address.getPostalCode() != null ? address.getPostalCode() : "");
        editAddress.setText(address.getAddressLine1() != null ? address.getAddressLine1() : "");
        checkDefault.setChecked(address.isDefault());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_MAP && resultCode == RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selected_address");
            if (selectedAddress != null) {
                editAddress.setText(selectedAddress);
            } else {
                Toast.makeText(this, "Không thể lấy địa chỉ từ vị trí đã chọn", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAddress() {
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String addressLine1 = editAddress.getText().toString().trim();

        tilName.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);

        boolean hasError = false;
        if (name.isEmpty()) {
            tilName.setError("Vui lòng nhập họ và tên");
            hasError = true;
        }
        if (phone.isEmpty()) {
            tilPhone.setError("Vui lòng nhập số điện thoại");
            hasError = true;
        }
        if (addressLine1.isEmpty()) {
            tilAddress.setError("Vui lòng nhập địa chỉ");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Address address = new Address(
                name,
                userId,
                addressLine1,
                phone,
                checkDefault.isChecked()
        );

        if (addressToEdit != null) {
            address.setKey(addressToEdit.getKey());
            updateAddress(address);
        } else {
            addNewAddress(address);
        }
    }

    private void addNewAddress(Address address) {
        String key = mDatabase.push().getKey();
        address.setKey(key);

        if (address.isDefault()) {
            resetDefaultAddress(() -> mDatabase.child(key).setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thêm địa chỉ thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
        } else {
            mDatabase.child(key).setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thêm địa chỉ thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateAddress(Address address) {
        if (address.isDefault()) {
            resetDefaultAddress(() -> mDatabase.child(address.getKey()).setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật địa chỉ thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
        } else {
            mDatabase.child(address.getKey()).setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật địa chỉ thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteAddress() {
        if (addressToEdit != null) {
            mDatabase.child(addressToEdit.getKey()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Xóa địa chỉ thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void resetDefaultAddress(Runnable onComplete) {
        mDatabase.orderByChild("userId").equalTo(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Address existingAddress = snapshot.getValue(Address.class);
                            if (existingAddress != null && existingAddress.isDefault()) {
                                existingAddress.setDefault(false);
                                mDatabase.child(snapshot.getKey()).setValue(existingAddress);
                            }
                        }
                        onComplete.run();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AddAddressActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("AddAddress", "Location permission granted");
                requestCurrentLocation();
            } else {
                Log.d("AddAddress", "Location permission denied");
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
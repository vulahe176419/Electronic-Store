package com.example.electronicstore;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import java.util.regex.Pattern;

public class AddAddressActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_SELECT_MAP = 1002;
    private static final int REQUEST_ENABLE_LOCATION = 1003;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final long LOCATION_TIMEOUT = 5000; // 5 giây

    private EditText editName, editPhone, editAddress;
    private CheckBox checkDefault;
    private TextInputLayout tilName, tilPhone, tilAddress;
    private Button deleteButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private Address addressToEdit;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Handler timeoutHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        initializeFirebase();
        initializeViews();
        setupLocationServices();
        setupListeners();

        if (getIntent().hasExtra("address")) {
            addressToEdit = (Address) getIntent().getSerializableExtra("address");
            populateFields(addressToEdit);
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        mDatabase = FirebaseDatabase.getInstance().getReference("addresses");
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New address");
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
    }

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        timeoutHandler = new Handler(Looper.getMainLooper());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(AddAddressActivity.this, "Unable to update location", Toast.LENGTH_SHORT).show();
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateAddressFromLocation(location);
                    stopLocationUpdates();
                } else {
                    Toast.makeText(AddAddressActivity.this, "Waiting for new position...", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void setupListeners() {
        findViewById(R.id.btn_use_current_location).setOnClickListener(v -> requestLocation());
        findViewById(R.id.btn_select_location_on_map).setOnClickListener(v ->
                startActivityForResult(new Intent(this, MapActivity.class), REQUEST_SELECT_MAP));
        findViewById(R.id.complete_button).setOnClickListener(v -> saveAddress());
        deleteButton.setOnClickListener(v -> deleteAddress());
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else if (!isLocationEnabled()) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, REQUEST_ENABLE_LOCATION);
        } else {
            requestCurrentLocation();
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Updating location...", Toast.LENGTH_SHORT).show();

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500) // Cập nhật mỗi 1 giây
                .setFastestInterval(100); // Nhận cập nhật nhanh nhất có thể

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                .addOnSuccessListener(aVoid -> {
                    // Bắt đầu yêu cầu, chờ callback
                    timeoutHandler.postDelayed(() -> {
                        if (fusedLocationClient != null) {
                            fusedLocationClient.removeLocationUpdates(locationCallback);
                            Toast.makeText(this, "Position timed out", Toast.LENGTH_SHORT).show();
                        }
                    }, LOCATION_TIMEOUT);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Location request error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            timeoutHandler.removeCallbacksAndMessages(null); // Hủy timeout
        }
    }

    private void updateAddressFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this);
            List<android.location.Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                editAddress.setText(addresses.get(0).getAddressLine(0));
            } else {
                editAddress.setText("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
        }
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
                Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (isLocationEnabled()) {
                requestCurrentLocation();
            } else {
                Toast.makeText(this, "Location services are not yet enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAddress() {
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String addressLine1 = editAddress.getText().toString().trim();

        if (!validateInputs(name, phone, addressLine1)) return;

        String userId = mAuth.getCurrentUser().getUid();
        Address address = new Address(name, userId, addressLine1, phone, checkDefault.isChecked());

        if (addressToEdit != null) {
            address.setKey(addressToEdit.getKey());
            updateAddress(address);
        } else {
            addNewAddress(address);
        }
    }

    private boolean validateInputs(String name, String phone, String address) {
        tilName.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);

        boolean isValid = true;
        if (name.isEmpty()) {
            tilName.setError("Please enter your full name");
            isValid = false;
        }
        if (phone.isEmpty()) {
            tilPhone.setError("Please enter phone number");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            tilPhone.setError("Invalid phone number (10 digits)");
            isValid = false;
        }
        if (address.isEmpty()) {
            tilAddress.setError("Please enter address");
            isValid = false;
        }
        return isValid;
    }

    private void addNewAddress(Address address) {
        String key = mDatabase.push().getKey();
        address.setKey(key);

        if (address.isDefault()) {
            resetDefaultAddress(() -> mDatabase.child(key).setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Add address successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
        } else {
            mDatabase.child(key).setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Add address successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateAddress(Address address) {
        if (address.isDefault()) {
            resetDefaultAddress(() -> mDatabase.child(address.getKey()).setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Address update successful", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
        } else {
            mDatabase.child(address.getKey()).setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Address update successful", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteAddress() {
        if (addressToEdit != null) {
            mDatabase.child(addressToEdit.getKey()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Address deleted successfully", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AddAddressActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(this, "Location access denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}
package com.example.electronicstore;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap; // Đối tượng GoogleMap để quản lý bản đồ
    private Marker selectedMarker; // Marker hiển thị vị trí được chọn
    private LatLng selectedLatLng; // Tọa độ của vị trí được chọn
    private Button btnConfirmLocation; // Nút Confirm để xác nhận địa chỉ
    private SearchView searchView; // SearchView để tìm kiếm địa chỉ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map); // Gắn layout activity_map.xml

        // Khởi tạo bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Ánh xạ và thiết lập nút Confirm
        btnConfirmLocation = findViewById(R.id.btn_confirm_location);
        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                convertLatLngToAddress(selectedLatLng); // Chuyển tọa độ thành địa chỉ và trả về
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });

        // Ánh xạ và thiết lập SearchView
        searchView = findViewById(R.id.search_view);
        setupSearchView(); // Thiết lập sự kiện tìm kiếm
    }

    // Thiết lập sự kiện cho SearchView để tìm kiếm địa chỉ
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAddress(query); // Tìm kiếm địa chỉ khi người dùng nhấn Enter
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // Không xử lý khi người dùng nhập từng ký tự
            }
        });
    }

    // Tìm kiếm địa chỉ và di chuyển bản đồ đến vị trí tương ứng
    private void searchAddress(String addressQuery) {
        if (addressQuery == null || addressQuery.trim().isEmpty()) {
            Toast.makeText(this, "Please enter a valid address", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = geocoder.getFromLocationName(addressQuery, 1); // Tìm kiếm địa chỉ
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedLatLng = new LatLng(address.getLatitude(), address.getLongitude()); // Lấy tọa độ

                // Cập nhật bản đồ
                if (selectedMarker != null) {
                    selectedMarker.remove(); // Xóa marker cũ
                }
                selectedMarker = mMap.addMarker(new MarkerOptions()
                        .position(selectedLatLng)
                        .title(addressQuery)); // Thêm marker mới
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f)); // Di chuyển camera
                btnConfirmLocation.setEnabled(true); // Kích hoạt nút Confirm
            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("MapActivity", "Geocoder error: " + e.getMessage());
            Toast.makeText(this, "Error searching address", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Thiết lập vị trí mặc định (Hà Nội)
        LatLng defaultLocation = new LatLng(21.0285, 105.8542);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));
        mMap.setOnMapClickListener(this); // Gắn listener để xử lý khi người dùng nhấn vào bản đồ
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if (selectedMarker != null) {
            selectedMarker.remove(); // Xóa marker cũ
        }
        // Thêm marker tại vị trí người dùng nhấn
        selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected location"));
        selectedLatLng = latLng; // Lưu tọa độ
        btnConfirmLocation.setEnabled(true); // Kích hoạt nút Confirm
    }

    // Chuyển tọa độ thành địa chỉ và trả về kết quả
    private void convertLatLngToAddress(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_address", address); // Trả về địa chỉ
                setResult(RESULT_OK, resultIntent);
                finish(); // Đóng Activity
            } else {
                Toast.makeText(this, "Cannot get address from this location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("MapActivity", "Geocoder error: " + e.getMessage());
            Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
        }
    }
}
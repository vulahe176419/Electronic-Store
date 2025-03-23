package com.example.electronicstore;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.electronicstore.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RateProduct extends AppCompatActivity {
    private EditText edtReviewTitle, edtComment;
    private RatingBar ratingBar;
    private CheckBox chkAnonymous;
    private Button btnSubmit, btnAddImage, btnAddVideo;
    private ImageButton btnBack;
    private RecyclerView recyclerViewMediaPreview;
    private ImageView imgProduct;
    private TextView txtProductName, txtQuantity;
    private final List<Uri> mediaList = new ArrayList<>();
    private MediaPreviewAdapter mediaPreviewAdapter;
    private DatabaseReference databaseReference;
    private ActivityResultLauncher<String> pickImage;
    private ActivityResultLauncher<String> pickVideo;
    private String orderId, productId;
    private final List<String> mediaUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_product); // Gắn layout cho activity

        // Khởi tạo Cloudinary
        initCloudinary();

        // Nhận dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        productId = getIntent().getStringExtra("productId");

        // Khởi tạo các view
        imgProduct = findViewById(R.id.imgProduct);
        txtProductName = findViewById(R.id.txtProductName);
        txtQuantity = findViewById(R.id.txtQuantity);
        edtReviewTitle = findViewById(R.id.edtReviewTitle);
        edtComment = findViewById(R.id.edtComment);
        ratingBar = findViewById(R.id.ratingBar);
        chkAnonymous = findViewById(R.id.chkAnonymous);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnAddVideo = findViewById(R.id.btnAddVideo);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewMediaPreview = findViewById(R.id.recyclerViewMediaPreview);

        // Thiết lập sự kiện cho nút Back
        btnBack.setOnClickListener(v -> finish());

        // Thiết lập RecyclerView để hiển thị media
        recyclerViewMediaPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mediaPreviewAdapter = new MediaPreviewAdapter(mediaList);
        recyclerViewMediaPreview.setAdapter(mediaPreviewAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference(); // Khởi tạo tham chiếu Firebase

        // Khởi tạo ActivityResultLauncher để chọn hình ảnh
        pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        mediaList.add(uri); // Thêm URI ảnh vào danh sách
                        mediaPreviewAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                    }
                });

        // Tạm thời vô hiệu hóa chọn video
        pickVideo = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> Toast.makeText(this, "Video upload is not supported", Toast.LENGTH_SHORT).show());

        // Sự kiện nhấn các nút
        btnAddImage.setOnClickListener(v -> pickImage.launch("image/*")); // Mở trình chọn ảnh
        btnAddVideo.setOnClickListener(v -> pickVideo.launch("video/*")); // Mở trình chọn video
        btnSubmit.setOnClickListener(v -> submitReview()); // Gửi đánh giá

        // Tải thông tin sản phẩm
        loadProductInfo();
    }

    // Khởi tạo Cloudinary với thông tin cấu hình
    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "duuzdzbwm");
        config.put("api_key", "471421576273992");
        config.put("api_secret", "4wHu1ymjwA1HMXnV2RVWy3ADNzo");
        MediaManager.init(this, config); // Khởi tạo MediaManager
    }

    // Tải thông tin sản phẩm từ Firebase
    private void loadProductInfo() {
        if (productId == null || orderId == null) {
            Toast.makeText(this, "No product information found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin sản phẩm từ node "products"
        databaseReference.child("products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    txtProductName.setText(name != null ? name : "Unknown"); // Hiển thị tên sản phẩm
                    if (imageUrl != null) {
                        Glide.with(RateProduct.this).load(imageUrl).into(imgProduct); // Tải ảnh sản phẩm
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateProduct.this, "Product loading error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Lấy số lượng từ node "orderDetails"
        databaseReference.child("orderDetails").orderByChild("orderId").equalTo(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int quantity = 0;
                        for (DataSnapshot detailSnapshot : snapshot.getChildren()) {
                            String detailProductId = detailSnapshot.child("productId").getValue(String.class);
                            if (productId.equals(detailProductId)) {
                                Integer qty = detailSnapshot.child("quantity").getValue(Integer.class);
                                quantity = qty != null ? qty : 1; // Gán số lượng, mặc định là 1 nếu null
                                break;
                            }
                        }
                        txtQuantity.setText("Quantity: " + quantity); // Hiển thị số lượng
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RateProduct.this, "Quantity Load Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Gửi đánh giá
    private void submitReview() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Lấy thông tin người dùng hiện tại
        String userId = currentUser != null ? currentUser.getUid() : "2DEHJfC8PoOefz32QBW1Jh3kEtI3"; // ID người dùng, mặc định nếu chưa đăng nhập
        String userName; // Tên người dùng sẽ được xác định

        // Kiểm tra ẩn danh và lấy tên người dùng
        if (chkAnonymous.isChecked()) {
            userName = "Ẩn danh"; // Nếu chọn ẩn danh
        } else {
            // Nếu không ẩn danh, lấy displayName từ FirebaseAuth
            userName = currentUser != null && currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()
                    ? currentUser.getDisplayName() : "Người dùng không xác định"; // Lấy tên thật hoặc mặc định nếu không có
        }

        String reviewTitle = edtReviewTitle.getText().toString().trim(); // Tiêu đề đánh giá
        String comment = edtComment.getText().toString().trim(); // Bình luận
        float rating = ratingBar.getRating(); // Số sao
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()); // Ngày hiện tại

        // Kiểm tra dữ liệu đầu vào
        if (rating == 0) {
            Toast.makeText(this, "Please select number of stars!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (reviewTitle.isEmpty() || comment.isEmpty()) {
            Toast.makeText(this, "Please enter title and description!", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaUrls.clear(); // Xóa danh sách URL cũ
        if (!mediaList.isEmpty()) {
            uploadMediaToCloudinary(0, userId, userName, rating, reviewTitle, date, comment); // Upload media nếu có
        } else {
            saveReviewToDatabase(userId, userName, rating, reviewTitle, date, mediaUrls, comment); // Lưu trực tiếp nếu không có media
        }
    }

    // Upload media lên Cloudinary
    private void uploadMediaToCloudinary(int index, String userId, String userName, float rating, String reviewTitle,
                                         String date, String comment) {
        if (index >= mediaList.size()) {
            // Đã upload hết media, lưu review vào database
            saveReviewToDatabase(userId, userName, rating, reviewTitle, date, mediaUrls, comment);
            return;
        }

        Uri uri = mediaList.get(index); // Lấy URI của media
        try {
            MediaManager.get().upload(uri)
                    .unsigned("my_unsigned_preset") // Upload Preset của Cloudinary
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            runOnUiThread(() -> Toast.makeText(RateProduct.this, "Uploading media " + (index + 1), Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Có thể hiển thị tiến trình nếu muốn
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url"); // Lấy URL sau khi upload
                            mediaUrls.add(imageUrl); // Thêm vào danh sách URL

                            // Tiếp tục upload media tiếp theo
                            uploadMediaToCloudinary(index + 1, userId, userName, rating, reviewTitle, date, comment);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> Toast.makeText(RateProduct.this, "Upload error: " + error.getDescription(), Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> Toast.makeText(RateProduct.this, "Upload rescheduled: " + error.getDescription(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .dispatch();
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(RateProduct.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    // Lưu đánh giá vào Firebase Database
    private void saveReviewToDatabase(String userId, String userName, float rating, String reviewTitle, String date,
                                      List<String> mediaUrls, String comment) {
        Review review = new Review(null, userId, userName, rating, productId, reviewTitle, date, mediaUrls,
                comment, null, null, 0); // Tạo đối tượng Review

        databaseReference.child("reviews").push().setValue(review) // Lưu vào node "reviews"
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product review submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Thoát sau khi gửi thành công
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error submitting review: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Adapter để hiển thị danh sách media đã chọn
    private class MediaPreviewAdapter extends RecyclerView.Adapter<MediaPreviewAdapter.MediaViewHolder> {
        private final List<Uri> mediaUris;

        public MediaPreviewAdapter(List<Uri> mediaUris) {
            this.mediaUris = mediaUris; // Khởi tạo danh sách
        }

        @NonNull
        @Override
        public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_preview, parent, false);
            return new MediaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
            Uri uri = mediaUris.get(position); // Lấy URI tại vị trí
            holder.imagePreview.setImageURI(uri); // Hiển thị ảnh preview

            holder.btnRemove.setOnClickListener(v -> {
                mediaUris.remove(position); // Xóa media khỏi danh sách
                notifyItemRemoved(position); // Thông báo xóa
                notifyItemRangeChanged(position, mediaUris.size()); // Cập nhật giao diện
                Toast.makeText(RateProduct.this, "Media deleted", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return mediaUris.size(); // Trả về số lượng media
        }

        // ViewHolder để lưu các thành phần giao diện
        public class MediaViewHolder extends RecyclerView.ViewHolder {
            ImageView imagePreview;
            ImageView btnRemove;

            public MediaViewHolder(@NonNull View itemView) {
                super(itemView);
                imagePreview = itemView.findViewById(R.id.imgMediaPreview);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }
}
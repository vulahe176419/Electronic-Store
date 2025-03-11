package com.example.electronicstore;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.example.electronicstore.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RateProduct extends AppCompatActivity {
    private EditText edtReviewTitle, edtComment;
    private RatingBar ratingBar;
    private CheckBox chkAnonymous;
    private Button btnSubmit, btnAddImage, btnAddVideo;
    private RecyclerView recyclerViewMediaPreview;
    private List<Uri> mediaList = new ArrayList<>();
    private MediaPreviewAdapter mediaPreviewAdapter;
    private DatabaseReference reviewsRef;
    private ActivityResultLauncher<String> pickImage;
    private ActivityResultLauncher<String> pickVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_product); // Sử dụng đúng layout

        // Khởi tạo các view
        edtReviewTitle = findViewById(R.id.edtReviewTitle);
        edtComment = findViewById(R.id.edtComment);
        ratingBar = findViewById(R.id.ratingBar);
        chkAnonymous = findViewById(R.id.chkAnonymous);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnAddVideo = findViewById(R.id.btnAddVideo);
        recyclerViewMediaPreview = findViewById(R.id.recyclerViewMediaPreview);

        // Thiết lập RecyclerView để hiển thị media
        recyclerViewMediaPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mediaPreviewAdapter = new MediaPreviewAdapter(mediaList);
        recyclerViewMediaPreview.setAdapter(mediaPreviewAdapter);

        reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");

        // Khởi tạo ActivityResultLauncher để chọn hình ảnh
        pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        mediaList.add(uri);
                        mediaPreviewAdapter.notifyDataSetChanged();
                    }
                });

        // Khởi tạo ActivityResultLauncher để chọn video
        pickVideo = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        mediaList.add(uri);
                        mediaPreviewAdapter.notifyDataSetChanged();
                    }
                });

        btnAddImage.setOnClickListener(v -> pickImage.launch("image/*")); // Chọn hình ảnh
        btnAddVideo.setOnClickListener(v -> pickVideo.launch("video/*")); // Chọn video
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        String userName = chkAnonymous.isChecked() ? "Ẩn danh" : "Người dùng";
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "2DEHJfC8PoOefz32QBW1Jh3kEtI3"; // Giá trị mặc định
        String reviewTitle = edtReviewTitle.getText().toString().trim();
        String comment = edtComment.getText().toString().trim();
        float rating = ratingBar.getRating();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        if (reviewTitle.isEmpty() || comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và mô tả!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> mediaStringList = new ArrayList<>();
        for (Uri uri : mediaList) {
            mediaStringList.add(uri.toString());
        }

        // Sử dụng productId cố định để test
        String productId = "2"; // Thay bằng productId bạn muốn test

        Review review = new Review(null, userId, userName, rating, productId, reviewTitle, date, mediaStringList,
                comment, null, null, 0);

        reviewsRef.push().setValue(review)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Adapter để hiển thị danh sách media đã chọn
    private class MediaPreviewAdapter extends RecyclerView.Adapter<MediaPreviewAdapter.MediaViewHolder> {
        private List<Uri> mediaUris;

        public MediaPreviewAdapter(List<Uri> mediaUris) {
            this.mediaUris = mediaUris;
        }

        @NonNull
        @Override
        public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_preview, parent, false);
            return new MediaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
            Uri uri = mediaUris.get(position);
            holder.imagePreview.setImageURI(uri);

            // Xử lý sự kiện xóa
            holder.btnRemove.setOnClickListener(v -> {
                mediaUris.remove(position);
                notifyItemRemoved(position);  // Cập nhật chính xác vị trí đã xóa
                notifyItemRangeChanged(position, mediaUris.size());  // Cập nhật các item còn lại
                Toast.makeText(RateProduct.this, "Đã xóa media", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return mediaUris.size();
        }

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
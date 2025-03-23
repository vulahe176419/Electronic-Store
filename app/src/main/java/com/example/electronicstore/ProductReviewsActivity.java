package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.electronicstore.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewReviews;
    private TextView txtNoReviews;
    private Button btnFilterAll, btnFilterMedia, btnFilterStar, btnFilterVariant;
    private ImageView btnBack; // Nút Back mới
    private DatabaseReference reviewsRef, usersRef;
    private final List<Review> reviewList = new ArrayList<>();
    private ReviewAdapter reviewAdapter;
    private String productId;
    private String currentUserId;
    private boolean isAdmin = false;
    private final HashMap<String, Boolean> userLikesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_reviews);

        // Khởi tạo các view
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        txtNoReviews = findViewById(R.id.txtNoReviews);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterMedia = findViewById(R.id.btnFilterMedia);
        btnFilterStar = findViewById(R.id.btnFilterStar);
        btnFilterVariant = findViewById(R.id.btnFilterVariant);
        btnBack = findViewById(R.id.btn_back); // Khởi tạo nút Back

        // Lấy productId từ Intent
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "Product ID not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập RecyclerView
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerViewReviews.setAdapter(reviewAdapter);

        // Khởi tạo tham chiếu Firebase
        reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Lấy ID người dùng hiện tại nếu đã đăng nhập
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Tải dữ liệu ban đầu
        loadUserLikes();
        checkUserRole();
        loadReviews();

        // Sự kiện nhấn nút Back
        btnBack.setOnClickListener(v -> finish()); // Quay lại màn hình trước đó

        // Sự kiện nhấn nút "Tất cả"
        btnFilterAll.setOnClickListener(v -> {
            txtNoReviews.setVisibility(View.GONE); // Ẩn thông báo không có đánh giá
            recyclerViewReviews.setVisibility(View.VISIBLE); // Hiển thị danh sách
            updateRecyclerViewDescription(reviewList.size()); // Cập nhật mô tả
            reviewAdapter.updateList(reviewList); // Cập nhật dữ liệu cho adapter
        });

        // Sự kiện nhấn nút "Có hình ảnh/video"
        btnFilterMedia.setOnClickListener(v -> {
            txtNoReviews.setVisibility(View.GONE);
            recyclerViewReviews.setVisibility(View.VISIBLE);
            List<Review> filteredList = new ArrayList<>();
            for (Review review : reviewList) {
                if (review.getMediaUrls() != null && !review.getMediaUrls().isEmpty()) {
                    filteredList.add(review); // Lọc các đánh giá có media
                }
            }
            if (filteredList.isEmpty()) {
                txtNoReviews.setVisibility(View.VISIBLE);
                txtNoReviews.setText("There are no reviews with photos/videos.");
                recyclerViewReviews.setVisibility(View.GONE);
                updateRecyclerViewDescription(0);
            } else {
                reviewAdapter.updateList(filteredList);
                updateRecyclerViewDescription(filteredList.size());
            }
        });

        // Sự kiện nhấn nút "Sao"
        btnFilterStar.setOnClickListener(v -> {
            showStarFilterDialog();
        });

        // Sự kiện nhấn nút "Phân loại"
        btnFilterVariant.setOnClickListener(v -> {
            showVariantFilterDialog();
        });
    }

    // Tải trạng thái thích của người dùng từ Firebase
    private void loadUserLikes() {
        if (currentUserId == null) return; // Thoát nếu chưa đăng nhập

        DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference("user_likes").child(currentUserId);
        userLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String reviewId = data.getKey();
                    Boolean hasLiked = data.getValue(Boolean.class);
                    if (hasLiked != null) {
                        userLikesMap.put(reviewId, hasLiked); // Lưu trạng thái thích vào map
                    }
                }
                reviewAdapter.notifyDataSetChanged(); // Cập nhật giao diện
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductReviewsActivity.this, "Error loading like status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Kiểm tra vai trò người dùng (admin hay không)
    private void checkUserRole() {
        if (currentUserId != null) {
            usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.child("role").getValue(String.class);
                    isAdmin = "admin".equals(role); // Gán true nếu là admin
                    reviewAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProductReviewsActivity.this, "Role check error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    isAdmin = false;
                    reviewAdapter.notifyDataSetChanged();
                }
            });
        } else {
            isAdmin = false;
            reviewAdapter.notifyDataSetChanged();
        }
    }

    // Tải danh sách đánh giá từ Firebase
    private void loadReviews() {
        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear(); // Xóa danh sách cũ
                for (DataSnapshot data : snapshot.getChildren()) {
                    Review review = data.getValue(Review.class);
                    if (review != null) {
                        review.setId(data.getKey());
                        if (productId.equals(review.getProductId())) { // Chỉ thêm đánh giá của sản phẩm hiện tại
                            reviewList.add(review);
                        }
                    }
                }
                reviewAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                if (reviewList.isEmpty()) {
                    txtNoReviews.setVisibility(View.VISIBLE);
                    txtNoReviews.setText("There are no reviews for this product.");
                    recyclerViewReviews.setVisibility(View.GONE);
                    updateRecyclerViewDescription(0);
                } else {
                    txtNoReviews.setVisibility(View.GONE);
                    recyclerViewReviews.setVisibility(View.VISIBLE);
                    updateRecyclerViewDescription(reviewList.size());
                }
                Toast.makeText(ProductReviewsActivity.this, "Find " + reviewList.size() + " review", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductReviewsActivity.this, "Error loading review: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cập nhật mô tả cho RecyclerView (trợ năng)
    private void updateRecyclerViewDescription(int count) {
        recyclerViewReviews.setContentDescription("List of product reviews. Currently available " + count + " review.");
        recyclerViewReviews.setHasFixedSize(true); // Đảm bảo kích thước không thay đổi
    }

    // Hiển thị dialog lọc theo số sao
    private void showStarFilterDialog() {
        Map<Integer, Integer> starCountMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            starCountMap.put(i, 0); // Khởi tạo số lượng đánh giá cho mỗi sao
        }
        for (Review review : reviewList) {
            int rating = Math.round(review.getRating());
            starCountMap.put(rating, starCountMap.getOrDefault(rating, 0) + 1); // Đếm số đánh giá theo sao
        }

        String[] starOptions = {
                "1 Star (" + starCountMap.get(1) + " review)",
                "2 Star (" + starCountMap.get(2) + " review)",
                "3 Star (" + starCountMap.get(3) + " review)",
                "4 Star (" + starCountMap.get(4) + " review)",
                "5 Star (" + starCountMap.get(5) + " review)"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select number of stars");

        final int[] selectedStar = {-1}; // Lưu lựa chọn sao

        builder.setSingleChoiceItems(starOptions, -1, (dialog, which) -> {
            selectedStar[0] = which + 1; // Cập nhật sao được chọn
        });

        builder.setPositiveButton("Áp dụng", (dialog, which) -> {
            if (selectedStar[0] == -1) { // Nếu không chọn sao nào
                txtNoReviews.setVisibility(View.GONE);
                recyclerViewReviews.setVisibility(View.VISIBLE);
                updateRecyclerViewDescription(reviewList.size());
                reviewAdapter.updateList(reviewList);
                return;
            }

            List<Review> filteredList = new ArrayList<>();
            for (Review review : reviewList) {
                int rating = Math.round(review.getRating());
                if (rating == selectedStar[0]) {
                    filteredList.add(review); // Lọc theo số sao
                }
            }

            if (filteredList.isEmpty()) {
                txtNoReviews.setVisibility(View.VISIBLE);
                txtNoReviews.setText("There are no reviews for this star rating.");
                recyclerViewReviews.setVisibility(View.GONE);
                updateRecyclerViewDescription(0);
            } else {
                txtNoReviews.setVisibility(View.GONE);
                recyclerViewReviews.setVisibility(View.VISIBLE);
                reviewAdapter.updateList(filteredList);
                updateRecyclerViewDescription(filteredList.size());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Hiển thị dialog lọc theo phân loại
    private void showVariantFilterDialog() {
        Map<String, Boolean> variantMap = new HashMap<>();
        for (Review review : reviewList) {
            String variant = review.getVariant();
            if (variant != null && !variant.isEmpty()) {
                variantMap.put(variant, false); // Lưu các phân loại
            }
        }

        if (variantMap.isEmpty()) {
            Toast.makeText(this, "There are no categories to filter", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select category");

        String[] variants = variantMap.keySet().toArray(new String[0]);
        boolean[] checkedItems = new boolean[variants.length];
        builder.setMultiChoiceItems(variants, checkedItems, (dialog, which, isChecked) -> {
            variantMap.put(variants[which], isChecked); // Cập nhật trạng thái chọn
        });

        builder.setPositiveButton("Apply", (dialog, which) -> {
            List<Review> filteredList = new ArrayList<>();
            for (Review review : reviewList) {
                String variant = review.getVariant();
                if (variant != null && variantMap.containsKey(variant) && variantMap.get(variant)) {
                    filteredList.add(review); // Lọc theo phân loại được chọn
                }
            }
            if (filteredList.isEmpty()) {
                txtNoReviews.setVisibility(View.VISIBLE);
                txtNoReviews.setText("There are no reviews for this category.");
                recyclerViewReviews.setVisibility(View.GONE);
                updateRecyclerViewDescription(0);
            } else {
                txtNoReviews.setVisibility(View.GONE);
                recyclerViewReviews.setVisibility(View.VISIBLE);
                reviewAdapter.updateList(filteredList);
                updateRecyclerViewDescription(filteredList.size());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Adapter cho RecyclerView hiển thị danh sách đánh giá
    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
        private List<Review> reviews;

        public ReviewAdapter(List<Review> reviews) {
            this.reviews = reviews;
        }

        public void updateList(List<Review> newList) {
            this.reviews = newList; // Cập nhật danh sách mới
            notifyDataSetChanged(); // Thông báo thay đổi dữ liệu
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return new ReviewViewHolder(view); // Tạo ViewHolder mới
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Review review = reviews.get(position); // Lấy đánh giá tại vị trí
            holder.txtUserName.setText(review.getUserName()); // Hiển thị tên người dùng
            holder.ratingBarReview.setRating(review.getRating()); // Hiển thị số sao
            holder.txtVariant.setText("Categories: " + (review.getVariant() != null ? review.getVariant() : "do not have")); // Hiển thị phân loại
            holder.txtComment.setText(review.getComment()); // Hiển thị bình luận
            holder.txtComment.setTextSize(14); // Đặt kích thước chữ

            holder.btnLike.setText(String.valueOf(review.getLikes())); // Hiển thị số lượt thích
            final boolean hasLiked = userLikesMap.getOrDefault(review.getId(), false); // Trạng thái thích của người dùng
            holder.btnLike.setActivated(hasLiked); // Đặt trạng thái nút thích

            // Sự kiện nhấn nút thích
            holder.btnLike.setOnClickListener(v -> {
                if (currentUserId == null) {
                    Toast.makeText(ProductReviewsActivity.this, "Please login to like!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final boolean currentlyLiked = userLikesMap.getOrDefault(review.getId(), false);
                final int newLikes = currentlyLiked ? review.getLikes() - 1 : review.getLikes() + 1; // Cập nhật số lượt thích
                userLikesMap.put(review.getId(), !currentlyLiked); // Cập nhật trạng thái thích

                // Cập nhật lên Firebase
                reviewsRef.child(review.getId()).child("likes").setValue(newLikes)
                        .addOnSuccessListener(aVoid -> {
                            review.setLikes(newLikes);
                            holder.btnLike.setText(String.valueOf(newLikes));
                            holder.btnLike.setActivated(!currentlyLiked);

                            DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference("user_likes").child(currentUserId);
                            userLikesRef.child(review.getId()).setValue(!currentlyLiked); // Cập nhật trạng thái thích của người dùng
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ProductReviewsActivity.this, "Error updating likes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            userLikesMap.put(review.getId(), currentlyLiked); // Hoàn tác nếu lỗi
                        });
            });

            // Hiển thị phản hồi của người bán nếu có
            if (review.getSellerReply() != null && !review.getSellerReply().isEmpty()) {
                holder.txtSellerReply.setText("Seller Feedback: " + review.getSellerReply());
                holder.txtSellerReply.setVisibility(View.VISIBLE);
            } else {
                holder.txtSellerReply.setVisibility(View.GONE);
            }

            // Hiển thị nút trả lời nếu là admin
            if (isAdmin) {
                holder.btnReply.setVisibility(View.VISIBLE);
                holder.btnReply.setEnabled(true);
            } else {
                holder.btnReply.setVisibility(View.GONE);
                holder.btnReply.setEnabled(false);
            }

            holder.btnReply.setOnClickListener(v -> showReplyDialog(review)); // Sự kiện nhấn nút trả lời

            // Hiển thị hình ảnh/video nếu có
            HorizontalScrollView horizontalScrollView = holder.itemView.findViewById(R.id.horizontalScrollView);
            holder.mediaContainer.removeAllViews(); // Xóa các view cũ

            List<String> mediaUrls = review.getMediaUrls();
            if (mediaUrls != null && !mediaUrls.isEmpty()) {
                holder.mediaContainer.setVisibility(View.VISIBLE);
                if (horizontalScrollView != null) {
                    horizontalScrollView.setVisibility(View.VISIBLE);
                }
                for (String mediaUrl : mediaUrls) {
                    ImageView imageView = new ImageView(ProductReviewsActivity.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            dpToPx(200), dpToPx(150) // Kích thước cố định
                    );
                    layoutParams.setMargins(8, 0, 8, 0);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Cắt ảnh theo tỷ lệ
                    imageView.setContentDescription("Image review");

                    // Tải ảnh bằng Glide
                    Glide.with(ProductReviewsActivity.this)
                            .load(mediaUrl)
                            .override(dpToPx(200), dpToPx(150))
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .centerCrop()
                            .into(imageView);

                    holder.mediaContainer.addView(imageView); // Thêm ảnh vào container
                }
            } else {
                holder.mediaContainer.setVisibility(View.GONE);
                if (horizontalScrollView != null) {
                    horizontalScrollView.setVisibility(View.GONE);
                }
            }
        }

        // Chuyển đổi dp sang px
        private int dpToPx(int dp) {
            float density = getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }

        @Override
        public int getItemCount() {
            return reviews.size(); // Trả về số lượng đánh giá
        }

        // ViewHolder để lưu các thành phần giao diện của mỗi item
        public class ReviewViewHolder extends RecyclerView.ViewHolder {
            ImageView imgUserAvatar;
            TextView txtUserName, txtVariant, txtComment, txtSellerReply;
            RatingBar ratingBarReview;
            TextView btnLike;
            Button btnReply;
            LinearLayout mediaContainer;

            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
                txtUserName = itemView.findViewById(R.id.txtUserName);
                ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
                txtVariant = itemView.findViewById(R.id.txtVariant);
                txtComment = itemView.findViewById(R.id.txtComment);
                txtSellerReply = itemView.findViewById(R.id.txtSellerReply);
                btnLike = itemView.findViewById(R.id.btnLike);
                btnReply = itemView.findViewById(R.id.btnReply);
                mediaContainer = itemView.findViewById(R.id.mediaContainer);
            }
        }
    }

    // Hiển thị dialog để trả lời đánh giá
    private void showReplyDialog(Review review) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Feedback review");

        EditText input = new EditText(this);
        input.setHint("Enter your feedback...");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String reply = input.getText().toString().trim();
            if (!reply.isEmpty()) {
                reviewsRef.child(review.getId()).child("sellerReply").setValue(reply) // Gửi phản hồi lên Firebase
                        .addOnSuccessListener(aVoid -> Toast.makeText(ProductReviewsActivity.this, "Response successful!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ProductReviewsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Error", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Kiểm tra đăng nhập khi activity bắt đầu
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login to use this feature!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)); // Chuyển đến màn hình đăng nhập
            finish();
        }
    }
}
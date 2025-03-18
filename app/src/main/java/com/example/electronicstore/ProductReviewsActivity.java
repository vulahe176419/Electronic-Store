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
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        txtNoReviews = findViewById(R.id.txtNoReviews);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterMedia = findViewById(R.id.btnFilterMedia);
        btnFilterStar = findViewById(R.id.btnFilterStar);
        btnFilterVariant = findViewById(R.id.btnFilterVariant);

        productId = "2";

        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerViewReviews.setAdapter(reviewAdapter);

        reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        loadUserLikes();

        checkUserRole();
        loadReviews();

        // Đặt viền xám đậm cho btnFilterAll khi vừa vào màn hình
        resetFilters();
        btnFilterAll.setBackgroundResource(R.drawable.filter_button_selected_background);

        btnFilterAll.setOnClickListener(v -> {
            resetFilters();
            btnFilterAll.setBackgroundResource(R.drawable.filter_button_selected_background);
            txtNoReviews.setVisibility(View.GONE);
            recyclerViewReviews.setVisibility(View.VISIBLE);
            updateRecyclerViewDescription(reviewList.size());
            reviewAdapter.updateList(reviewList);
        });

        btnFilterMedia.setOnClickListener(v -> {
            resetFilters();
            btnFilterMedia.setBackgroundResource(R.drawable.filter_button_selected_background);
            txtNoReviews.setVisibility(View.GONE);
            recyclerViewReviews.setVisibility(View.VISIBLE);
            List<Review> filteredList = new ArrayList<>();
            for (Review review : reviewList) {
                if (review.getMediaUrls() != null && !review.getMediaUrls().isEmpty()) {
                    filteredList.add(review);
                }
            }
            if (filteredList.isEmpty()) {
                txtNoReviews.setVisibility(View.VISIBLE);
                txtNoReviews.setText("Không có đánh giá nào có hình ảnh/video");
                recyclerViewReviews.setVisibility(View.GONE);
                updateRecyclerViewDescription(0);
            } else {
                reviewAdapter.updateList(filteredList);
                updateRecyclerViewDescription(filteredList.size());
            }
        });

        btnFilterStar.setOnClickListener(v -> {
            resetFilters();
            btnFilterStar.setBackgroundResource(R.drawable.filter_button_selected_background);
            showStarFilterDialog();
        });

        btnFilterVariant.setOnClickListener(v -> {
            resetFilters();
            btnFilterVariant.setBackgroundResource(R.drawable.filter_button_selected_background);
            showVariantFilterDialog();
        });
    }

    private void loadUserLikes() {
        if (currentUserId == null) return;

        DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference("user_likes").child(currentUserId);
        userLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String reviewId = data.getKey();
                    Boolean hasLiked = data.getValue(Boolean.class);
                    if (hasLiked != null) {
                        userLikesMap.put(reviewId, hasLiked);
                    }
                }
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductReviewsActivity.this, "Lỗi tải trạng thái thích: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole() {
        if (currentUserId != null) {
            usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.child("role").getValue(String.class);
                    isAdmin = "admin".equals(role);
                    reviewAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProductReviewsActivity.this, "Lỗi kiểm tra vai trò: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    isAdmin = false;
                    reviewAdapter.notifyDataSetChanged();
                }
            });
        } else {
            isAdmin = false;
            reviewAdapter.notifyDataSetChanged();
        }
    }

    private void resetFilters() {
        btnFilterAll.setBackgroundResource(R.drawable.filter_button_background);
        btnFilterMedia.setBackgroundResource(R.drawable.filter_button_background);
        btnFilterStar.setBackgroundResource(R.drawable.filter_button_background);
        btnFilterVariant.setBackgroundResource(R.drawable.filter_button_background);
    }

    private void loadReviews() {
        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Review review = data.getValue(Review.class);
                    if (review != null) {
                        review.setId(data.getKey());
                        if (productId != null && (review.getProductId() == null || productId.equals(review.getProductId()))) {
                            reviewList.add(review);
                        }
                    }
                }
                reviewAdapter.notifyDataSetChanged();
                if (reviewList.isEmpty()) {
                    txtNoReviews.setVisibility(View.VISIBLE);
                    txtNoReviews.setText("Không có đánh giá nào cho sản phẩm này");
                    recyclerViewReviews.setVisibility(View.GONE);
                    updateRecyclerViewDescription(0);
                } else {
                    txtNoReviews.setVisibility(View.GONE);
                    recyclerViewReviews.setVisibility(View.VISIBLE);
                    updateRecyclerViewDescription(reviewList.size());
                }
                Toast.makeText(ProductReviewsActivity.this, "Tìm thấy " + reviewList.size() + " đánh giá", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductReviewsActivity.this, "Lỗi tải đánh giá: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerViewDescription(int count) {
        recyclerViewReviews.setContentDescription("Danh sách đánh giá sản phẩm. Hiện tại có " + count + " đánh giá.");
        // Đảm bảo RecyclerView không thay đổi kích thước
        recyclerViewReviews.setHasFixedSize(true);
    }

    private void showStarFilterDialog() {
        Map<Integer, Integer> starCountMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            starCountMap.put(i, 0);
        }
        for (Review review : reviewList) {
            int rating = Math.round(review.getRating());
            starCountMap.put(rating, starCountMap.getOrDefault(rating, 0) + 1);
        }

        String[] starOptions = {
                "1 sao (" + starCountMap.get(1) + " đánh giá)",
                "2 sao (" + starCountMap.get(2) + " đánh giá)",
                "3 sao (" + starCountMap.get(3) + " đánh giá)",
                "4 sao (" + starCountMap.get(4) + " đánh giá)",
                "5 sao (" + starCountMap.get(5) + " đánh giá)"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn số sao");

        final int[] selectedStar = {-1};

        builder.setSingleChoiceItems(starOptions, -1, (dialog, which) -> {
            selectedStar[0] = which + 1;
        });

        builder.setPositiveButton("Áp dụng", (dialog, which) -> {
            if (selectedStar[0] == -1) {
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
                    filteredList.add(review);
                }
            }

            if (filteredList.isEmpty()) {
                txtNoReviews.setVisibility(View.VISIBLE);
                txtNoReviews.setText("Không có đánh giá nào cho số sao này");
                recyclerViewReviews.setVisibility(View.GONE);
                updateRecyclerViewDescription(0);
            } else {
                txtNoReviews.setVisibility(View.GONE);
                recyclerViewReviews.setVisibility(View.VISIBLE);
                reviewAdapter.updateList(filteredList);
                updateRecyclerViewDescription(filteredList.size());
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showVariantFilterDialog() {
        Map<String, Boolean> variantMap = new HashMap<>();
        for (Review review : reviewList) {
            String variant = review.getVariant();
            if (variant != null && !variant.isEmpty()) {
                variantMap.put(variant, false);
            }
        }

        if (variantMap.isEmpty()) {
            Toast.makeText(this, "Không có phân loại để lọc", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn phân loại");

        String[] variants = variantMap.keySet().toArray(new String[0]);
        boolean[] checkedItems = new boolean[variants.length];
        builder.setMultiChoiceItems(variants, checkedItems, (dialog, which, isChecked) -> {
            variantMap.put(variants[which], isChecked);
        });

        builder.setPositiveButton("Áp dụng", (dialog, which) -> {
            List<Review> filteredList = new ArrayList<>();
            for (Review review : reviewList) {
                String variant = review.getVariant();
                if (variant != null && variantMap.containsKey(variant) && variantMap.get(variant)) {
                    filteredList.add(review);
                }
            }
            if (filteredList.isEmpty()) {
                txtNoReviews.setVisibility(View.VISIBLE);
                txtNoReviews.setText("Không có đánh giá nào cho phân loại này");
                recyclerViewReviews.setVisibility(View.GONE);
                updateRecyclerViewDescription(0);
            } else {
                txtNoReviews.setVisibility(View.GONE);
                recyclerViewReviews.setVisibility(View.VISIBLE);
                reviewAdapter.updateList(filteredList);
                updateRecyclerViewDescription(filteredList.size());
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
        private List<Review> reviews;

        public ReviewAdapter(List<Review> reviews) {
            this.reviews = reviews;
        }

        public void updateList(List<Review> newList) {
            this.reviews = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Review review = reviews.get(position);
            holder.txtUserName.setText(review.getUserName());
            holder.ratingBarReview.setRating(review.getRating());
            holder.txtVariant.setText("Phân loại: " + (review.getVariant() != null ? review.getVariant() : "Không có"));
            holder.txtComment.setText(review.getComment());
            holder.txtComment.setTextSize(14);

            holder.btnLike.setText(String.valueOf(review.getLikes()));
            final boolean hasLiked = userLikesMap.getOrDefault(review.getId(), false);
            holder.btnLike.setActivated(hasLiked);

            holder.btnLike.setOnClickListener(v -> {
                if (currentUserId == null) {
                    Toast.makeText(ProductReviewsActivity.this, "Vui lòng đăng nhập để thích!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final boolean currentlyLiked = userLikesMap.getOrDefault(review.getId(), false);
                final int newLikes = currentlyLiked ? review.getLikes() - 1 : review.getLikes() + 1;
                userLikesMap.put(review.getId(), !currentlyLiked);

                // Cập nhật Firebase
                reviewsRef.child(review.getId()).child("likes").setValue(newLikes)
                        .addOnSuccessListener(aVoid -> {
                            review.setLikes(newLikes);
                            holder.btnLike.setText(String.valueOf(newLikes));
                            holder.btnLike.setActivated(!currentlyLiked);

                            DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference("user_likes").child(currentUserId);
                            userLikesRef.child(review.getId()).setValue(!currentlyLiked);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ProductReviewsActivity.this, "Lỗi cập nhật lượt thích: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            userLikesMap.put(review.getId(), currentlyLiked); // Rollback
                        });
            });

            if (review.getSellerReply() != null && !review.getSellerReply().isEmpty()) {
                holder.txtSellerReply.setText("Phản hồi của Người bán: " + review.getSellerReply());
                holder.txtSellerReply.setVisibility(View.VISIBLE);
            } else {
                holder.txtSellerReply.setVisibility(View.GONE);
            }

            if (isAdmin) {
                holder.btnReply.setVisibility(View.VISIBLE);
                holder.btnReply.setEnabled(true);
            } else {
                holder.btnReply.setVisibility(View.GONE);
                holder.btnReply.setEnabled(false);
            }

            holder.btnReply.setOnClickListener(v -> showReplyDialog(review));

            HorizontalScrollView horizontalScrollView = holder.itemView.findViewById(R.id.horizontalScrollView);
            holder.mediaContainer.removeAllViews();

            List<String> mediaUrls = review.getMediaUrls();
            if (mediaUrls != null && !mediaUrls.isEmpty()) {
                holder.mediaContainer.setVisibility(View.VISIBLE);
                if (horizontalScrollView != null) {
                    horizontalScrollView.setVisibility(View.VISIBLE);
                }
                for (String mediaUrl : mediaUrls) {
                    ImageView imageView = new ImageView(ProductReviewsActivity.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            dpToPx(200), // Chiều rộng cố định
                            dpToPx(150)  // Chiều cao cố định (tỷ lệ 4:3, bạn có thể điều chỉnh)
                    );
                    layoutParams.setMargins(8, 0, 8, 0);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Giữ tỷ lệ, cắt bỏ phần thừa
                    imageView.setContentDescription("Hình ảnh đánh giá");

                    Glide.with(ProductReviewsActivity.this)
                            .load(mediaUrl)
                            .override(dpToPx(200), dpToPx(150)) // Ép kích thước cố định
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .centerCrop() // Đảm bảo tỷ lệ không bị méo
                            .into(imageView);

                    holder.mediaContainer.addView(imageView);
                }
            } else {
                holder.mediaContainer.setVisibility(View.GONE);
                if (horizontalScrollView != null) {
                    horizontalScrollView.setVisibility(View.GONE);
                }
            }
        }

        private int dpToPx(int dp) {
            float density = getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

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

    private void showReplyDialog(Review review) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Phản hồi đánh giá");

        EditText input = new EditText(this);
        input.setHint("Nhập phản hồi của bạn...");
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String reply = input.getText().toString().trim();
            if (!reply.isEmpty()) {
                reviewsRef.child(review.getId()).child("sellerReply").setValue(reply)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ProductReviewsActivity.this, "Phản hồi thành công!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ProductReviewsActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)); // Thay bằng activity đăng nhập của bạn
            finish();
        }
    }
}
package com.example.electronicstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.electronicstore.R;

import java.util.List;

public class MediaPreviewAdapter extends RecyclerView.Adapter<MediaPreviewAdapter.ViewHolder> {
    private final Context context;
    private final List<?> mediaList;
    private final OnItemRemoveListener removeListener;

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }


    public MediaPreviewAdapter(Context context, List<?> mediaList, OnItemRemoveListener listener) {
        this.context = context;
        this.mediaList = mediaList;
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_preview, parent, false);
        return new ViewHolder(view); // Tạo ViewHolder mới
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object media = mediaList.get(position); // Lấy media tại vị trí
        String mediaUriString = media instanceof Uri ? media.toString() : (String) media; // Chuyển đổi URI

        // Tải media bằng Glide
        Glide.with(context)
                .load(mediaUriString)
                .placeholder(android.R.drawable.ic_menu_gallery) // Ảnh placeholder
                .error(android.R.drawable.ic_dialog_alert) // Ảnh lỗi
                .into(holder.imgMediaPreview);

        // Xác định loại media
        String mimeType = context.getContentResolver().getType(Uri.parse(mediaUriString));
        if (mimeType != null && mimeType.startsWith("image")) {
            holder.txtMediaType.setText("Image");
        } else if (mimeType != null && mimeType.startsWith("video")) {
            holder.txtMediaType.setText("Video");
        } else {
            holder.txtMediaType.setText("Media");
        }

        // Sự kiện nhấn để xem media
        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (mimeType != null && mimeType.startsWith("video")) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(mediaUriString), "video/*");
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(mediaUriString), "image/*");
            }
            context.startActivity(intent);
        });

        // Hiển thị và xử lý nút xóa
        if (removeListener != null) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> removeListener.onRemove(position)); // Gọi listener khi nhấn xóa
        } else {
            holder.btnRemove.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size(); // Trả về số lượng media
    }

    // ViewHolder để lưu các thành phần giao diện
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMediaPreview;
        TextView txtMediaType;
        ImageView btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMediaPreview = itemView.findViewById(R.id.imgMediaPreview);
            txtMediaType = itemView.findViewById(R.id.txtMediaType);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
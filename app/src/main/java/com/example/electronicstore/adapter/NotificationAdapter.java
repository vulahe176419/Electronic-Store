package com.example.electronicstore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.R;
import com.example.electronicstore.model.Notification;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications");

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(notification.getTime());
        holder.imgNotification.setImageResource(R.drawable.ic_notification);

        // Nếu đã đọc, đổi màu text
        if (notification.isRead()) {
            holder.tvTitle.setTextColor(0xFF888888);
        } else {
            holder.tvTitle.setTextColor(0xFF000000);
        }

        // Bấm để đánh dấu là đã đọc
        holder.itemView.setOnClickListener(v -> markAsRead(notification));

        // Nhấn giữ để xóa thông báo
        holder.itemView.setOnLongClickListener(v -> {
            deleteNotification(notification);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private void markAsRead(Notification notification) {
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRef.child(notification.getId()).child("isRead").setValue(true);
            notifyDataSetChanged();
        }
    }

    private void deleteNotification(Notification notification) {
        notificationRef.child(notification.getId()).removeValue();
        notifications.remove(notification);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView imgNotification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            imgNotification = itemView.findViewById(R.id.img_notification);
        }
    }
}

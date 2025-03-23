package com.example.electronicstore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicstore.R;
import com.example.electronicstore.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<User> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user, String userId);
    }

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String loggedInEmail = (currentUser != null) ? currentUser.getEmail() : "";

        if (user.getEmail().toLowerCase().contains("admin")) {
            holder.userRole.setText("Admin");
            holder.userRole.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.orange));
        } else {
            holder.userRole.setText("Normal User");
            holder.userRole.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
        }

        if (user.getEmail().equalsIgnoreCase(loggedInEmail)) {
            holder.yourAccount.setVisibility(View.VISIBLE);
        } else {
            holder.yourAccount.setVisibility(View.GONE);
        }

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user, user.getUid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userEmail;
        TextView userRole;
        TextView yourAccount;
        Button editButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            editButton = itemView.findViewById(R.id.edit_button);
            userRole = itemView.findViewById(R.id.user_role);
            yourAccount = itemView.findViewById(R.id.your_account);
        }
    }
}
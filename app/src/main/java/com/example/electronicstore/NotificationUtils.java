package com.example.electronicstore.utils;

import com.example.electronicstore.model.Notification;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationUtils {
    private static DatabaseReference notificationRef = FirebaseDatabase.getInstance()
            .getReference("notifications");

    public static void createNotification(String userId, String title, String message) {
        String notificationId = notificationRef.push().getKey();
        if (notificationId == null) return;

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Notification notification = new Notification(
                notificationId,
                title,
                message,
                time,
                false,
                userId
        );

        notificationRef.child(notificationId).setValue(notification)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved notification
                })
                .addOnFailureListener(e -> {
                    // Failed to save notification
                });
    }

    public static void createProfileUpdateNotification(String userId) {
        String title = "Profile Updated";
        String message = "Your profile information has been successfully updated.";
        createNotification(userId, title, message);
    }

    // New method for address creation
    public static void createAddressCreationNotification(String userId, String addressLine1) {
        String title = "New Address Added";
        String message = "A new address has been added: " + addressLine1;
        createNotification(userId, title, message);
    }

    // New method for default address change
    public static void createDefaultAddressChangeNotification(String userId, String addressLine1) {
        String title = "Default Address Changed";
        String message = "Your default address has been updated to: " + addressLine1;
        createNotification(userId, title, message);
    }
}
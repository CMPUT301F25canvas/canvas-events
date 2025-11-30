package com.example.lotteryeventsystem;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationsManager {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String CHANNEL_ID = "app_notifications_channel";

    private static void write(String docName, String eventId, String userId) {
        Map<String, Object> data = createNotifDetails(docName);
        db.collection("notifications")
                .document(docName)
                .collection(eventId)
                .document(userId)
                .set(data);
        db.collection("notifications")
                .document(docName)
                .update("event_collection", FieldValue.arrayUnion(eventId))
                .addOnFailureListener(e -> {
                    // If document doesn't exist, create it
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("event_collection", new ArrayList<String>() {{
                        add(eventId);
                    }});
                    db.collection("notifications").document(docName).set(newData);
                });
    }

    /**
     * Sent when user accepts the invite
     * @param eventId The id of the event
     * @param userId The user id to whom the notification was sent to
     */
    public static void sendInviteAccepted(Context context, String eventId, String userId) {
        write("invite_accepted_notification", eventId, userId);
    }

    /**
     * Sent when the user invite is cancelled by the organizer
     * @param eventId The id of the event
     * @param userId The user id to whom the notification was sent to
     */
    public static void sendInviteCancelled(Context context, String eventId, String userId) {
        write("invite_cancelled_notification", eventId, userId);
    }

    /**
     * Sent when user rejected their invite
     * @param eventId The id of the event
     * @param userId The user id to whom the notification was sent to
     */
    public static void sendInviteRejected(Context context, String eventId, String userId) {
        write("invite_rejected_notification", eventId, userId);
    }

    /**
     * Sent when user joins the waitlist
     * @param eventId The id of the event
     * @param userId The user id to whom the notification was sent to
     */
    public static void sendJoinedWaitlist(Context context, String eventId, String userId) {
        write("joined_waitlist_notification", eventId, userId);
        pushLocalNotification(context, "joined_waitlist_notification", eventId, userId);
    }

    /**
     * Sent when user was not drawn in the lottery
     * @param eventId The id of the event
     * @param userId The user id to whom the notification was sent to
     */
    public static void sendNotSelected(Context context, String eventId, String userId) {
        write("not_selected_notification", eventId, userId);
    }

    /**
     * Sent when user was drawn in the lotetry
     * @param eventId The id of the event
     * @param userId The user id to whom the notification was sent to
     */
    public static void sendSelected(Context context, String eventId, String userId) {
        write("selected_notification", eventId, userId);
    }

    /**
     * Creates the notification details map for Firestore.
     * @return A Map<String, Object> containing the initial notification fields.
     */
    private static Map<String, Object> createNotifDetails(String docName) {
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", Timestamp.now());
        if (docName.equals("selected_notification")) {
            data.put("response", "None");
        } else {
            data.put("seen_status", false);
        }

        return data;
    }

    /**
     * Fetches the notification template from Firestore, fills in the details, and
     * displays the popup notification on the user's device.
     * @param context The Context used to display the notification.
     * @param notifType The notification document name under the "notifications" collection
     * @param eventId The ID of the event for which this notification is being sent.
     * @param userId The ID of the user receiving the notification (not used for UI, but kept for consistency/logging).
     */
    public static void pushLocalNotification(Context context, String notifType, String eventId, String userId) {

        db.collection("notifications")
                .document(notifType)
                .get()
                .addOnSuccessListener(notifDoc -> {

                    if (!notifDoc.exists()) return;

                    String title = notifDoc.getString("title");
                    String contentTemplate = notifDoc.getString("content");

                    // Get event name
                    db.collection("events")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(eventDoc -> {

                                if (!eventDoc.exists()) return;

                                String eventName = eventDoc.getString("name");
                                String finalContent = contentTemplate.replace("{{eventName}}", eventName);
                                showAndroidNotification(context, title, finalContent);
                            });
                });
    }

    /**
     * Builds and displays a Android notification on the app.
     * @param context The Context used to display the notification.
     * @param title The title text of the notification.
     * @param content The message displayed inside the notification.
     */
    private static void showAndroidNotification(Context context, String title, String content) {

        // Create channel for Android 8+
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "App Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        int notificationId = (int) System.currentTimeMillis();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }
}

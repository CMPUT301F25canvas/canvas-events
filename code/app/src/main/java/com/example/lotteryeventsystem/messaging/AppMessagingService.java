package com.example.lotteryeventsystem.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lotteryeventsystem.MainActivity;
import com.example.lotteryeventsystem.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Receives FCM pushes and mirrors them into Firestore while showing a system notification.
 */
public class AppMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "app_notifications_channel";
    private static final String TAG = "AppMessagingService";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        saveToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData() != null ? remoteMessage.getData() : new HashMap<>();

        String title = valueOr(remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : null,
                data.get("title"),
                getString(R.string.notification_title_fallback));
        String body = valueOr(remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : null,
                data.get("body"),
                "");

        writeToFirestoreIfPossible(data);
        showSystemNotification(title, body);
    }

    private void saveToken(String token) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null || deviceId.isEmpty()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        payload.put("createdAt", FieldValue.serverTimestamp());
        payload.put("platform", "android");
        firestore.collection("users")
                .document(deviceId)
                .collection("fcmTokens")
                .document(token)
                .set(payload)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save FCM token", e));
    }

    private void writeToFirestoreIfPossible(Map<String, String> data) {
        String templateId = data.get("templateId");
        String eventId = data.get("eventId");
        if (templateId == null || templateId.isEmpty() || eventId == null || eventId.isEmpty()) {
            return;
        }
        String recipientId = data.get("recipientId");
        if (recipientId == null || recipientId.isEmpty()) {
            recipientId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        DocumentReference doc = firestore.collection("notifications")
                .document(templateId)
                .collection("events")
                .document(eventId)
                .collection("recipients")
                .document();
        Map<String, Object> payload = new HashMap<>();
        payload.put("recipientId", recipientId);
        payload.put("recipientName", data.get("recipientName"));
        payload.put("eventId", eventId);
        payload.put("eventName", data.get("eventName"));
        payload.put("title", valueOr(data.get("title"), getString(R.string.notification_title_fallback)));
        payload.put("body", valueOr(data.get("body"), ""));
        payload.put("type", data.get("type"));
        payload.put("source", valueOr(data.get("source"), "ORGANIZER"));
        payload.put("templateId", templateId);
        payload.put("waitlistEntryId", data.get("waitlistEntryId"));
        payload.put("status", normalizeStatus(data.get("status")));
        payload.put("createdAt", FieldValue.serverTimestamp());
        doc.set(payload)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mirror push to Firestore", e));
    }

    private void showSystemNotification(String title, String body) {
        createChannelIfNeeded();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "App Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                manager.createNotificationChannel(channel);
            }
        }
    }

    private String normalizeStatus(String statusValue) {
        if (statusValue == null || statusValue.isEmpty()) {
            return "UNREAD";
        }
        try {
            return statusValue.toUpperCase(Locale.US);
        } catch (Exception e) {
            return "UNREAD";
        }
    }

    private String valueOr(String primary, String fallback, String defaultValue) {
        if (primary != null && !primary.isEmpty()) {
            return primary;
        }
        if (fallback != null && !fallback.isEmpty()) {
            return fallback;
        }
        return defaultValue;
    }

    private String valueOr(String primary, String defaultValue) {
        if (primary != null && !primary.isEmpty()) {
            return primary;
        }
        return defaultValue;
    }
}

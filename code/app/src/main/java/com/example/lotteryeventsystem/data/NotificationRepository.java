package com.example.lotteryeventsystem.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.model.NotificationMessage;
import com.example.lotteryeventsystem.model.NotificationStatus;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Handles all notification reads/writes against Firestore.
 */
public class NotificationRepository {

    public interface NotificationFeedListener {
        void onChanged(@Nullable List<NotificationMessage> messages, @Nullable Exception error);
    }

    private final FirebaseFirestore firestore;

    public NotificationRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public NotificationRepository(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Creates a notification entry for each entrant in the list.
     *
     * @param eventId        event id the notification is about
     * @param eventName      display name for the event
     * @param title          title to show in the feed
     * @param message        long-form body
     * @param type           a short string like INVITE or BROADCAST
     * @param entrants       recipients
     * @param callback       completion callback with how many notifications were written
     */
    public void sendNotificationsToEntrants(String eventId,
                                            @Nullable String eventName,
                                            String title,
                                            String message,
                                            String type,
                                            List<WaitlistEntry> entrants,
                                            RepositoryCallback<Integer> callback) {
        if (entrants == null || entrants.isEmpty()) {
            callback.onComplete(0, null);
            return;
        }
        WriteBatch batch = firestore.batch();
        int added = 0;
        for (WaitlistEntry entry : entrants) {
            String recipientId = entry.getEntrantId();
            if (recipientId == null || recipientId.isEmpty()) {
                recipientId = entry.getId();
            }
            if (recipientId == null || recipientId.isEmpty()) {
                continue;
            }
            DocumentReference doc = firestore.collection("notifications").document();
            Map<String, Object> payload = new HashMap<>();
            payload.put("recipientId", recipientId);
            payload.put("recipientName", entry.getEntrantName());
            payload.put("eventId", eventId);
            payload.put("eventName", eventName);
            payload.put("title", title);
            payload.put("body", message);
            payload.put("type", type);
            payload.put("waitlistEntryId", entry.getId());
            payload.put("status", NotificationStatus.PENDING.name());
            payload.put("createdAt", FieldValue.serverTimestamp());
            batch.set(doc, payload);
            added++;
        }
        if (added == 0) {
            callback.onComplete(0, null);
            return;
        }
        final int total = added;
        batch.commit()
                .addOnSuccessListener(unused -> callback.onComplete(total, null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    /**
     * Live subscription to notifications for a given user/device.
     */
    public ListenerRegistration listenToUserNotifications(String recipientId,
                                                          NotificationFeedListener listener) {
        return firestore.collection("notifications")
                .whereEqualTo("recipientId", recipientId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        listener.onChanged(null, error);
                        return;
                    }
                    List<NotificationMessage> messages = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
                            NotificationMessage message = mapMessage(snapshot);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                    }
                    listener.onChanged(messages, null);
                });
    }

    /**
     * Mark a notification as handled/read.
     */
    public void updateNotificationStatus(String notificationId,
                                         NotificationStatus status,
                                         RepositoryCallback<Void> callback) {
        firestore.collection("notifications")
                .document(notificationId)
                .update("status", status.name())
                .addOnSuccessListener(unused -> callback.onComplete(null, null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    @Nullable
    private NotificationMessage mapMessage(DocumentSnapshot snapshot) {
        NotificationMessage message = snapshot.toObject(NotificationMessage.class);
        if (message == null) {
            message = new NotificationMessage();
        }
        message.setId(snapshot.getId());
        message.setStatus(parseStatus(snapshot.getString("status")));
        message.setCreatedAt(snapshot.getTimestamp("createdAt"));
        message.setRespondBy(snapshot.getTimestamp("respondBy"));
        return message;
    }

    private NotificationStatus parseStatus(@Nullable String statusValue) {
        if (statusValue == null) {
            return NotificationStatus.UNREAD;
        }
        try {
            return NotificationStatus.valueOf(statusValue.toUpperCase(Locale.US));
        } catch (IllegalArgumentException e) {
            return NotificationStatus.UNREAD;
        }
    }
}

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
import java.util.concurrent.atomic.AtomicInteger;

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
     * @param source         who sent it (ORGANIZER/ADMIN/MARKETING); defaults to ORGANIZER if null
     * @param entrants       recipients
     * @param callback       completion callback with how many notifications were written
     */
    public void sendNotificationsToEntrants(String eventId,
                                            @Nullable String eventName,
                                            String title,
                                            String message,
                                            String type,
                                            @Nullable String source,
                                            NotificationStatus status,
                                            List<WaitlistEntry> entrants,
                                            RepositoryCallback<Integer> callback) {
        String templateId = templateIdFor(type, status);
        fetchTemplate(templateId, (templateTitle, templateBody) -> sendUsingTemplate(templateId,
                templateTitle,
                templateBody,
                eventId,
                eventName,
                title,
                message,
                type,
                source,
                status,
                entrants,
                callback));
    }

    private void sendUsingTemplate(String templateId,
                                   @Nullable String templateTitle,
                                   @Nullable String templateBody,
                                   String eventId,
                                   @Nullable String eventName,
                                   String fallbackTitle,
                                   String fallbackBody,
                                   String type,
                                   @Nullable String source,
                                   NotificationStatus status,
                                   List<WaitlistEntry> entrants,
                                   RepositoryCallback<Integer> callback) {
        if (entrants == null || entrants.isEmpty()) {
            callback.onComplete(0, null);
            return;
        }
        NotificationStatus safeStatus = status != null ? status : NotificationStatus.UNREAD;
        String safeSource = source != null ? source : "ORGANIZER";
        WriteBatch batch = firestore.batch();
        AtomicInteger added = new AtomicInteger(0);
        AtomicInteger processed = new AtomicInteger(0);
        int totalRecipients = entrants.size();

        for (WaitlistEntry entry : entrants) {
            String recipientId = entry.getEntrantId();
            if (recipientId == null || recipientId.isEmpty()) {
                recipientId = entry.getId();
            }
            if (recipientId == null || recipientId.isEmpty()) {
                processed.incrementAndGet();
                continue;
            }

            String finalRecipientId = recipientId;
            fetchNotificationPrefs(recipientId, safeSource, (allowPush, allowSource) -> {
                if (allowPush && allowSource) {
                    String renderedTitle = renderText(templateTitle, fallbackTitle, eventName);
                    String renderedBody = renderText(templateBody, fallbackBody, eventName);
                    String safeEventId = eventId != null ? eventId : "unknown_event";
                    DocumentReference userDoc = firestore.collection("notifications")
                            .document(templateId)
                            .collection("events")
                            .document(safeEventId)
                            .collection("recipients")
                            .document();
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("recipientId", finalRecipientId);
                    payload.put("recipientName", entry.getEntrantName());
                    payload.put("eventId", eventId);
                    payload.put("eventName", eventName);
                    payload.put("title", renderedTitle);
                    payload.put("body", renderedBody);
                    payload.put("type", type);
                    payload.put("source", safeSource);
                    payload.put("templateId", templateId);
                    payload.put("waitlistEntryId", entry.getId());
                    payload.put("status", safeStatus.name());
                    payload.put("createdAt", FieldValue.serverTimestamp());
                    batch.set(userDoc, payload);
                    added.incrementAndGet();
                }
                if (processed.incrementAndGet() == totalRecipients) {
                    int totalAdded = added.get();
                    if (totalAdded == 0) {
                        callback.onComplete(0, null);
                        return;
                    }
                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onComplete(totalAdded, null))
                            .addOnFailureListener(e -> callback.onComplete(null, e));
                }
            });
        }
    }

    /**
     * Live subscription to notifications for a given user/device.
     */
    public ListenerRegistration listenToUserNotifications(String recipientId,
                                                          NotificationFeedListener listener) {
        return firestore.collectionGroup("recipients")
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
     * Admin view of all notifications.
     */
    public ListenerRegistration listenToAllNotifications(NotificationFeedListener listener) {
        return firestore.collectionGroup("recipients")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(200)
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
    public void updateNotificationStatus(NotificationMessage message,
                                         NotificationStatus status,
                                         RepositoryCallback<Void> callback) {
        if (message == null
                || message.getTemplateId() == null
                || message.getTemplateId().isEmpty()
                || message.getEventId() == null
                || message.getEventId().isEmpty()
                || message.getId() == null
                || message.getId().isEmpty()) {
            callback.onComplete(null, new IllegalArgumentException("Missing identifiers to update notification status"));
            return;
        }
        DocumentReference recipientDoc = firestore.collection("notifications")
                .document(message.getTemplateId())
                .collection("events")
                .document(message.getEventId())
                .collection("recipients")
                .document(message.getId());
        recipientDoc.update("status", status.name())
                .addOnSuccessListener(unused -> {
                    String outcomeTemplate = outcomeTemplateFor(status);
                    if (outcomeTemplate == null) {
                        callback.onComplete(null, null);
                        return;
                    }
                    writeOutcomeNotification(outcomeTemplate, message, status, callback);
                })
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    @Nullable
    private NotificationMessage mapMessage(DocumentSnapshot snapshot) {
        NotificationMessage message = snapshot.toObject(NotificationMessage.class);
        if (message == null) {
            message = new NotificationMessage();
        }
        message.setId(snapshot.getId());
        message.setRecipientId(snapshot.getString("recipientId"));
        message.setStatus(parseStatus(snapshot.getString("status")));
        message.setCreatedAt(snapshot.getTimestamp("createdAt"));
        message.setRespondBy(snapshot.getTimestamp("respondBy"));
        message.setType(snapshot.getString("type"));
        message.setSource(snapshot.getString("source"));
        message.setTemplateId(snapshot.getString("templateId"));
        if ((message.getTemplateId() == null || message.getTemplateId().isEmpty()) && snapshot.getReference() != null) {
            DocumentReference templateRef = snapshot.getReference()
                    .getParent()     // recipients
                    .getParent()     // eventId doc
                    .getParent()     // events collection
                    .getParent();    // template doc
            if (templateRef != null) {
                message.setTemplateId(templateRef.getId());
            }
        }
        if ((message.getEventId() == null || message.getEventId().isEmpty()) && snapshot.getReference() != null) {
            DocumentReference eventRef = snapshot.getReference()
                    .getParent()   // recipients
                    .getParent();  // eventId doc
            if (eventRef != null) {
                message.setEventId(eventRef.getId());
            }
        }
        return message;
    }

    public interface NotificationPrefsCallback {
        void onComplete(boolean allowPush, boolean allowSource);
    }

    private void fetchNotificationPrefs(String recipientId,
                                        String source,
                                        NotificationPrefsCallback callback) {
        firestore.collection("users")
                .document(recipientId)
                .collection("preferences")
                .document("notifications")
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean allowPush = snapshot.getBoolean("allow_push") == null || snapshot.getBoolean("allow_push");
                    boolean allowOrganizer = snapshot.getBoolean("organizer") == null || snapshot.getBoolean("organizer");
                    boolean allowAdmin = snapshot.getBoolean("admin") == null || snapshot.getBoolean("admin");
                    boolean allowMarketing = snapshot.getBoolean("marketing") != null && snapshot.getBoolean("marketing");

                    boolean allowSource;
                    if ("ADMIN".equalsIgnoreCase(source)) {
                        allowSource = allowAdmin;
                    } else if ("MARKETING".equalsIgnoreCase(source)) {
                        allowSource = allowMarketing;
                    } else {
                        allowSource = allowOrganizer; // default organizer
                    }
                    callback.onComplete(allowPush, allowSource);
                })
                .addOnFailureListener(e -> {
                    // Fail open so users still receive critical messages if prefs unreadable.
                    callback.onComplete(true, true);
                });
    }

    private interface TemplateCallback {
        void onComplete(@Nullable String title, @Nullable String body);
    }

    private void fetchTemplate(String templateId, TemplateCallback callback) {
        firestore.collection("notifications")
                .document(templateId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String title = snapshot.getString("title");
                    String body = snapshot.getString("content");
                    callback.onComplete(title, body);
                })
                .addOnFailureListener(e -> callback.onComplete(null, null));
    }

    private String renderText(@Nullable String template, @Nullable String fallback, @Nullable String eventName) {
        String base = template != null ? template : (fallback != null ? fallback : "");
        if (eventName != null && !eventName.isEmpty()) {
            return base.replace("{{eventName}}", eventName);
        }
        return base;
    }

    private String templateIdFor(String type, NotificationStatus status) {
        if (type == null) {
            return "selected_notification";
        }
        switch (type.toUpperCase(Locale.US)) {
            case "INVITE":
                return "selected_notification";
            case "RESULT":
                return "not_selected_notification";
            case "BROADCAST":
                return "selected_notification";
            case "CANCELLED":
                return "invite_cancelled_notification";
            case "DECLINED":
                return "invite_rejected_notification";
            case "ACCEPTED":
            case "REGISTERED":
                return "invite_accepted_notification";
            case "WAITLIST":
                return "joined_waitlist_notification";
            default:
                if (status == NotificationStatus.DECLINED) {
                    return "invite_rejected_notification";
                }
                if (status == NotificationStatus.REGISTERED || status == NotificationStatus.ACCEPTED) {
                    return "invite_accepted_notification";
                }
                if (status == NotificationStatus.NOT_SELECTED) {
                    return "not_selected_notification";
                }
                if (status == NotificationStatus.WAITING) {
                    return "joined_waitlist_notification";
                }
                return "selected_notification";
        }
    }

    @Nullable
    private String outcomeTemplateFor(NotificationStatus status) {
        if (status == NotificationStatus.DECLINED) {
            return "invite_rejected_notification";
        }
        if (status == NotificationStatus.ACCEPTED || status == NotificationStatus.REGISTERED) {
            return "invite_accepted_notification";
        }
        return null;
    }

    private void writeOutcomeNotification(String templateId,
                                          NotificationMessage base,
                                          NotificationStatus status,
                                          RepositoryCallback<Void> callback) {
        fetchTemplate(templateId, (templateTitle, templateBody) -> {
            String renderedTitle = renderText(templateTitle, base.getTitle(), base.getEventName());
            String renderedBody = renderText(templateBody, base.getBody(), base.getEventName());
            String safeEventId = base.getEventId() != null ? base.getEventId() : "unknown_event";
            DocumentReference doc = firestore.collection("notifications")
                    .document(templateId)
                    .collection("events")
                    .document(safeEventId)
                    .collection("recipients")
                    .document();
            Map<String, Object> payload = new HashMap<>();
            payload.put("recipientId", base.getRecipientId());
            payload.put("recipientName", base.getRecipientName());
            payload.put("eventId", base.getEventId());
            payload.put("eventName", base.getEventName());
            payload.put("title", renderedTitle);
            payload.put("body", renderedBody);
            payload.put("type", base.getType());
            payload.put("source", base.getSource());
            payload.put("templateId", templateId);
            payload.put("waitlistEntryId", base.getWaitlistEntryId());
            payload.put("status", status.name());
            payload.put("createdAt", FieldValue.serverTimestamp());
            doc.set(payload)
                    .addOnSuccessListener(unused -> callback.onComplete(null, null))
                    .addOnFailureListener(e -> callback.onComplete(null, e));
        });
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

package com.example.lotteryeventsystem;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class NotificationMessage {

    public enum NotificationStatus {
        UNREAD,
        SEEN,
        PENDING,
        ACCEPTED,
        DECLINED
    }

    private String type;
    private String eventId;
    private String title;
    private String body;
    private long timestamp;
    private NotificationStatus status;   // NEW
    private String response;            // NEW
    private String userId;              // NEW (doc id)

    public static NotificationMessage fromFirestore(
            String type,
            String eventId,
            String title,
            String body,
            DocumentSnapshot userDoc
    ) {
        NotificationMessage m = new NotificationMessage();

        // Basic fields
        m.type = type;
        m.eventId = eventId;
        m.title = title;
        m.body = body;

        // UserId = doc ID (needed for markAsSeen later)
        m.userId = userDoc.getId();

        // Timestamp
        Timestamp tsObj = userDoc.getTimestamp("timestamp");
        m.timestamp = (tsObj != null) ? tsObj.toDate().getTime() : 0;

        // Read response (only for selected_notification)
        m.response = userDoc.getString("response");

        // Determine status
        if ("selected_notification".equals(type)) {
            // Ignore seen_status completely

            if (m.response == null || m.response.equals("None")) {
                m.status = NotificationStatus.PENDING;
            } else if (m.response.equals("Accepted")) {
                m.status = NotificationStatus.ACCEPTED;
            } else if (m.response.equals("Rejected")) {
                m.status = NotificationStatus.DECLINED;
            } else {
                // Unknown (fallback)
                m.status = NotificationStatus.PENDING;
            }

        } else {
            // All other notification types still use seen_status
            Boolean seenVal = userDoc.getBoolean("seen_status");

            if (seenVal != null && seenVal) {
                m.status = NotificationStatus.SEEN;
            } else {
                m.status = NotificationStatus.UNREAD;
            }
        }

        return m;
    }

    // ---- Getters ----

    public String getType() { return type; }
    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public long getTimestamp() { return timestamp; }
    public NotificationStatus getStatus() { return status; }
    public String getResponse() { return response; }
    public String getUserId() { return userId; }

    public boolean isPending() { return status == NotificationStatus.PENDING; }
    public boolean isAccepted() { return status == NotificationStatus.ACCEPTED; }
    public boolean isRejected() { return status == NotificationStatus.DECLINED; }
    public boolean isUnread() { return status == NotificationStatus.UNREAD; }
    public boolean isSeen() { return status == NotificationStatus.SEEN; }
}

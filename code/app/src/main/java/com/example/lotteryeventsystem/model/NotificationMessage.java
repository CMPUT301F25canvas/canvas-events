package com.example.lotteryeventsystem.model;

import com.google.firebase.Timestamp;

/**
 * Simple record of an event-related notification.
 */
public class NotificationMessage {
    private String id;
    private String title;
    private String body;
    private String eventId;
    private String eventName;
    private String recipientId;
    private String recipientName;
    private String waitlistEntryId;
    private String type;
    private String source;
    private NotificationStatus status = NotificationStatus.UNREAD;
    private Timestamp createdAt;
    private Timestamp respondBy;

    public NotificationMessage() {
        // Needed for Firestore.
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getWaitlistEntryId() {
        return waitlistEntryId;
    }

    public void setWaitlistEntryId(String waitlistEntryId) {
        this.waitlistEntryId = waitlistEntryId;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getRespondBy() {
        return respondBy;
    }

    public void setRespondBy(Timestamp respondBy) {
        this.respondBy = respondBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Whether the message should surface a call to action.
     */
    public boolean requiresResponse() {
        return "INVITE".equalsIgnoreCase(type) || status == NotificationStatus.PENDING;
    }
}

package com.example.lotteryeventsystem.model;

import com.google.firebase.Timestamp;

/**
 * Event (Firestore doc: /events/{eventId})
 * TODO(outstanding): add validation and builder if needed.
 */
public class Event {
    public String id;
    public String title;
    public String description;
    public String organizerId;
    public String posterUrl;
    public Timestamp startAt;
    public Timestamp endAt;
    public Timestamp regOpenAt;
    public Timestamp regCloseAt;
    public Long capacity;
    public Long waitingCount;

    public Event() {}
}

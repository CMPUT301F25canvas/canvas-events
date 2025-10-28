package com.example.lotteryeventsystem.model;

import com.google.firebase.Timestamp;

public class Event {
    public String id, title, description, organizerId, posterUrl;
    public Timestamp startAt, endAt, regOpenAt, regCloseAt;
    public Long capacity, waitingCount;

    public Event() {}
}

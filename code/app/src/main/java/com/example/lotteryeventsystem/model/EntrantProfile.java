package com.example.lotteryeventsystem.model;

import com.google.firebase.Timestamp;

public class EntrantProfile {
    public String uid, name, email, phone, deviceId;
    public Timestamp createdAt;
    public EntrantProfile() {}
}

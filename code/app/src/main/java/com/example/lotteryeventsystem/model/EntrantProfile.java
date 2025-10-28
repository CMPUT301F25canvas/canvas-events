package com.example.lotteryeventsystem.model;

import com.google.firebase.Timestamp;

/**
 * Entrant profile (Firestore doc: /entrants/{uid})
 * TODO(outstanding): enforce minimum fields (name/email) in UI layer.
 */
public class EntrantProfile {
    public String uid;
    public String name;
    public String email;
    public String phone;
    public String deviceId;
    public Timestamp createdAt;

    public EntrantProfile() {}
}

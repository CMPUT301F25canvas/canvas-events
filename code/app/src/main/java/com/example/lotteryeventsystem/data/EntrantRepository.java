package com.example.lotteryeventsystem.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class EntrantRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid() { return FirebaseAuth.getInstance().getCurrentUser().getUid(); }

    public Task<Void> upsertProfile(String name, String email, String phone, String deviceId) {
        Map<String,Object> m = new HashMap<>();
        m.put("uid", uid()); m.put("name", name); m.put("email", email);
        m.put("phone", phone); m.put("deviceId", deviceId);
        m.put("createdAt", FieldValue.serverTimestamp());
        return db.collection("entrants").document(uid()).set(m, SetOptions.merge());
    }
}

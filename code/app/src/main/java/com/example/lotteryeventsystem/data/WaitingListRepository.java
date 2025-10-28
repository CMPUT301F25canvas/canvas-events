package com.example.lotteryeventsystem.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

/** Join/leave waiting list with simple guard checks on reg window + naive waitingCount. */
public class WaitingListRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid() { return FirebaseAuth.getInstance().getCurrentUser().getUid(); }

    public Task<Void> joinWaitingList(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference wlRef = eventRef.collection("waitingList").document(uid());
        return db.runTransaction(tx -> {
            DocumentSnapshot ev = tx.get(eventRef);
            Timestamp now = Timestamp.now();
            Timestamp open = ev.getTimestamp("regOpenAt");
            Timestamp close = ev.getTimestamp("regCloseAt");

            if (open != null && now.compareTo(open) < 0) {
                throw new FirebaseFirestoreException("Registration not open",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION);
            }
            if (close != null && now.compareTo(close) > 0) {
                throw new FirebaseFirestoreException("Registration closed",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION);
            }

            if (!tx.get(wlRef).exists()) {
                Map<String,Object> wl = new HashMap<>();
                wl.put("entrantId", uid());
                wl.put("status", "PENDING");
                wl.put("createdAt", FieldValue.serverTimestamp());
                tx.set(wlRef, wl);

                Long count = ev.getLong("waitingCount");
                tx.update(eventRef, "waitingCount", (count == null ? 0 : count) + 1);
            }
            return null;
        });
    }

    public Task<Void> leaveWaitingList(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference wlRef = eventRef.collection("waitingList").document(uid());
        return db.runTransaction(tx -> {
            if (tx.get(wlRef).exists()) {
                tx.delete(wlRef);
                DocumentSnapshot ev = tx.get(eventRef);
                Long count = ev.getLong("waitingCount");
                tx.update(eventRef, "waitingCount", Math.max(0, (count == null ? 0 : count - 1)));
            }
            return null;
        });
    }
}

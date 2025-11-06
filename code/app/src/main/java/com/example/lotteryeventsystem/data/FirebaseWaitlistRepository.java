package com.example.lotteryeventsystem.data;

import androidx.annotation.NonNull;

import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore backed waitlist repo.
 */
public class FirebaseWaitlistRepository implements WaitlistRepository {
    private final FirebaseFirestore firestore;

    public FirebaseWaitlistRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public FirebaseWaitlistRepository(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void getInvitedEntrants(String eventId, RepositoryCallback<List<WaitlistEntry>> callback) {
        firestore.collection("events")
                .document(eventId)
                .collection("waitlist")
                .whereEqualTo("status", WaitlistStatus.INVITED.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WaitlistEntry> entries = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                            entries.add(mapEntry(snapshot));
                        }
                    }
                    callback.onComplete(entries, null);
                })
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    @Override
    public void getWaitingEntrants(String eventId, RepositoryCallback<List<WaitlistEntry>> callback) {
        firestore.collection("events")
                .document(eventId)
                .collection("waitlist")
                .whereEqualTo("status", WaitlistStatus.WAITING.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WaitlistEntry> entries = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                            entries.add(mapEntry(snapshot));
                        }
                    }
                    callback.onComplete(entries, null);
                })
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    // New method added
    @Override
    public void updateEntrantStatus(String eventId,
                                    String entrantRecordId,
                                    WaitlistStatus status,
                                    RepositoryCallback<WaitlistEntry> callback) {
        DocumentReference docRef = firestore.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(entrantRecordId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status.name());
        if (status == WaitlistStatus.INVITED) {
            updates.put("invitedAt", FieldValue.serverTimestamp());
        }

        docRef.update(updates)
                .continueWithTask(task -> docRef.get())
                .addOnSuccessListener(snapshot -> callback.onComplete(mapEntry(snapshot), null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    @Override
    public void getEntrantsByStatus(String eventId, List<WaitlistStatus> statuses, RepositoryCallback<List<WaitlistEntry>> callback) {
        // Convert WaitlistStatus enums to string list for Firestore query
        List<String> statusStrings = new ArrayList<>();
        for (WaitlistStatus status : statuses) {
            statusStrings.add(status.name());
        }

        // Query the attendees subcollection (not waitlist - based on your structure)
        firestore.collection("events")
                .document(eventId)
                .collection("waitlist")  // Use "attendees" instead of "waitlist"
                .whereIn("status", statusStrings)  // Filter by multiple statuses
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WaitlistEntry> entries = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                            WaitlistEntry entry = mapEntry(snapshot);
                            // Ensure eventId is set on the entry
                            if (entry.getEventId() == null) {
                                entry.setEventId(eventId);
                            }
                            entries.add(entry);
                        }
                    }
                    callback.onComplete(entries, null);
                })
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    @Override
    public void incrementDeclineCount(String eventId) {
        firestore.collection("events")
                .document(eventId)
                .update("declineCount", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    // Not fatal if this fails, so just swallow for now.
                });
    }

    private WaitlistEntry mapEntry(DocumentSnapshot snapshot) {
        WaitlistEntry entry = snapshot.toObject(WaitlistEntry.class);
        if (entry == null) {
            entry = new WaitlistEntry();
        }
        entry.setId(snapshot.getId());
        return entry;
    }
}

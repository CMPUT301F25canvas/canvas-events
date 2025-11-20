package com.example.lotteryeventsystem.data;

import androidx.annotation.NonNull;

import com.example.lotteryeventsystem.User;
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
    /**
     * Enhanced method to get entrants with status filter AND user details from users collection
     * This performs a two-step data retrieval matching waitlist IDs to user IDs
     */
    public void getEntrantsByStatusWithUserDetails(String eventId, List<WaitlistStatus> statuses, RepositoryCallback<List<WaitlistEntry>> callback) {
        List<String> statusStrings = new ArrayList<>();
        for (WaitlistStatus status : statuses) {
            statusStrings.add(status.name());
        }

        firestore.collection("events")
                .document(eventId)
                .collection("waitlist")
                .whereIn("status", statusStrings)
                .get()
                .addOnSuccessListener(waitlistQuerySnapshot -> {
                    List<WaitlistEntry> entries = new ArrayList<>();

                    if (waitlistQuerySnapshot != null && !waitlistQuerySnapshot.isEmpty()) {
                        processWaitlistEntriesWithUserLookup(waitlistQuerySnapshot.getDocuments(), entries, 0, eventId, callback);
                    } else {
                        callback.onComplete(entries, null);
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    /**
     * Recursive helper to process waitlist entries and lookup user details
     */
    private void processWaitlistEntriesWithUserLookup(List<DocumentSnapshot> waitlistDocs, List<WaitlistEntry> entries,
                                                      int index, String eventId, RepositoryCallback<List<WaitlistEntry>> callback) {
        if (index >= waitlistDocs.size()) {
            callback.onComplete(entries, null);
            return;
        }

        DocumentSnapshot waitlistDoc = waitlistDocs.get(index);
        WaitlistEntry entry = new WaitlistEntry();
        entry.setId(waitlistDoc.getId());
        entry.setEventId(eventId);
        entry.setUserId(waitlistDoc.getId()); // User ID is the document ID in waitlist

        if (waitlistDoc.contains("status")) {
            try {
                entry.setStatus(WaitlistStatus.valueOf(waitlistDoc.getString("status")));
            } catch (IllegalArgumentException e) {
                entry.setStatus(WaitlistStatus.INVITED);
            }
        }

        // Lookup user details from users collection using the waitlist document ID as user ID
        String userId = waitlistDoc.getId();
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        User user = userDoc.toObject(User.class);
                        if (user != null) {
                            user.setId(userDoc.getId());
                            entry.setUser(user); // Set the user details with name from users collection
                        }
                    }
                    entries.add(entry);
                    processWaitlistEntriesWithUserLookup(waitlistDocs, entries, index + 1, eventId, callback);
                })
                .addOnFailureListener(e -> {
                    entries.add(entry); // Add entry even if user lookup fails
                    processWaitlistEntriesWithUserLookup(waitlistDocs, entries, index + 1, eventId, callback);
                });
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

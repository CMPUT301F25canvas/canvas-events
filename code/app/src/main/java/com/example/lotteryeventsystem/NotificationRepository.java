package com.example.lotteryeventsystem;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private static final String TAG = "NotificationRepo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<ListenerRegistration> regs = new ArrayList<>();

    private static final String[] TYPES = {
            "selected_notification",
            "invite_accepted_notification",
            "invite_rejected_notification",
            "invite_cancelled_notification",
            "not_selected_notification",
            "joined_waitlist_notification"
    };

    public interface Callback {
        void onResult(List<NotificationMessage> list, @Nullable Exception e);
    }

    /**
     * Listens to all notification types for a given user, and returns the document if when it's created.
     *
     * @param userId User id
     * @param callback callback returning updated notifications
     */
    public void listenToUserNotifications(String userId, Callback callback) {
        clear();
        List<NotificationMessage> all = new ArrayList<>();

        for (String type : TYPES) {

            // Listen to each notification type root document
            ListenerRegistration typeReg =
                    db.collection("notifications")
                            .document(type)
                            .addSnapshotListener((typeDoc, typeErr) -> {

                                if (typeErr != null || typeDoc == null || !typeDoc.exists()) {
                                    Log.e(TAG, "Error reading type " + type, typeErr);
                                    return;
                                }

                                // event_collection = array of eventIds
                                List<String> eventIds = (List<String>) typeDoc.get("event_collection");
                                if (eventIds == null) return;

                                String rawTitle = typeDoc.getString("title");
                                String rawContent = typeDoc.getString("content");

                                for (String eventId : eventIds) {

                                    ListenerRegistration userReg =
                                            typeDoc.getReference()
                                                    .collection(eventId)
                                                    .document(userId)
                                                    .addSnapshotListener((userDoc, userErr) -> {

                                                        if (userErr != null) {
                                                            Log.e(TAG, "Error listening to user doc", userErr);
                                                            return;
                                                        }

                                                        if (userDoc == null || !userDoc.exists()) {
                                                            return;
                                                        }

                                                        // Remove any old copies of this notification
                                                        all.removeIf(n ->
                                                                n.getType().equals(type) &&
                                                                        n.getEventId().equals(eventId)
                                                        );

                                                        // --- Fetch the eventName from /events/{eventId} ---
                                                        db.collection("events")
                                                                .document(eventId)
                                                                .get()
                                                                .addOnSuccessListener(eventSnap -> {

                                                                    String eventName = eventSnap.getString("name");
                                                                    if (eventName == null) {
                                                                        Log.d("TAG", eventSnap.getId());
                                                                    }
                                                                    else {
                                                                        Log.d("TAG", eventName);
                                                                    }
                                                                    if (eventName == null) eventName = "";

                                                                    String cleanTitle = (rawTitle != null ? rawTitle : "")
                                                                            .replace("{{eventName}}", eventName);

                                                                    String cleanBody = (rawContent != null ? rawContent : "")
                                                                            .replace("{{eventName}}", eventName);

                                                                    // Build final notification
                                                                    NotificationMessage msg =
                                                                            NotificationMessage.fromFirestore(
                                                                                    type,
                                                                                    eventId,
                                                                                    cleanTitle,
                                                                                    cleanBody,
                                                                                    userDoc
                                                                            );

                                                                    all.add(msg);

                                                                    // Sort newest → oldest
                                                                    all.sort((a, b) ->
                                                                            Long.compare(b.getTimestamp(), a.getTimestamp())
                                                                    );

                                                                    callback.onResult(new ArrayList<>(all), null);
                                                                });
                                                    });

                                    regs.add(userReg);
                                }
                            });

            regs.add(typeReg);
        }
    }

    /**
     * Removes all Firestore listeners.
     */
    public void clear() {
        for (ListenerRegistration r : regs) r.remove();
        regs.clear();
    }

    /**
     * Marks a specific notification as "seen" by the user.
     */
    public void markAsSeen(String type, String eventId, String userId) {
        db.collection("notifications")
                .document(type)
                .collection(eventId)
                .document(userId)
                .update("seen_status", true)
                .addOnSuccessListener(aVoid ->
                        Log.d("NotificationRepo", "Marked seen"))
                .addOnFailureListener(e ->
                        Log.e("NotificationRepo", "Failed to mark seen", e));
    }
}

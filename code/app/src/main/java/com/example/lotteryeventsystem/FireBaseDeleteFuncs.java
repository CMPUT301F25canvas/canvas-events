package com.example.lotteryeventsystem;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class FireBaseDeleteFuncs {
    /**
     * Deletes an event document and removes the event id from all the
     * enrolled users documents.
     * @param db The FireStore database instance
     * @param eventId The ID of the event document to delete
     * @param context The Android context (To display toast message)
     */
    public static void deleteEvent(FirebaseFirestore db, String eventId, Context context) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        String[] subcollections = {"waitlist"};

        for (String sub : subcollections) {
            eventRef.collection(sub).get().addOnSuccessListener(snapshot -> {
                for (QueryDocumentSnapshot userDoc : snapshot) {
                    String userId = userDoc.getId();
                    db.collection("users").document(userId)
                            .update("enrolled_events", FieldValue.arrayRemove(eventId));
                }
            });
        }

        eventRef.get().addOnSuccessListener(eventSnapshot -> {
            if (eventSnapshot.exists()) {
                String organizerId = eventSnapshot.getString("organizerId");
                if (organizerId != null && !organizerId.isEmpty()) {
                    db.collection("users").document(organizerId)
                            .update("organized_events", FieldValue.arrayRemove(eventId));
                }
            }

            eventRef.delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show());
        });
    }

    /**
     *  Deletes the user profile and deletes all the events they organized. It also removes the
     *  user from all the events they're enrolled in.
     * @param db The FireStore database instance
     * @param userId The ID of the user document to delete
     * @param context The Android context (To display toast message)
     */
    public static void deleteUserProfile(FirebaseFirestore db, String userId, Context context) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> organizedEvents = (List<String>) documentSnapshot.get("organized_events");
                        if (organizedEvents != null && !organizedEvents.isEmpty()) {
                            for (String eventId : organizedEvents) {
                                deleteEvent(db, eventId, context);
                            }
                        }

                        List<String> enrolledEvents = (List<String>) documentSnapshot.get("enrolled_events");
                        if (enrolledEvents != null) {
                            for (String eventId : enrolledEvents) {
                                DocumentReference eventRef = db.collection("events").document(eventId);
                                String[] subcollections = {"waitlist"};
                                for (String sub : subcollections) {
                                    eventRef.collection(sub).document(userId)
                                            .delete();
                                }
                            }
                        }

                        db.collection("users").document(userId)
                                .delete()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Error deleting user", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }
}

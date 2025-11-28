package com.example.lotteryeventsystem;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class NotificationListener {

    private static final String TAG = "NotificationListener";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final List<ListenerRegistration> registrations = new ArrayList<>();
    private static String userId;

    private static final String[] NOTIFICATION_TYPES = {
            "selected_notification",
            "not_selected_notification",
            "invite_cancelled_notification"
    };

    /**
     * Fetches the events the user is registered to, and starts listening to the documents
     * @param context: Context
     */
    public static void startListening(Context context) {
        userId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (userId == null) {
            return;
        }

        stopListening(); // clears previous listeners
        fetchUserEvents(eventIds -> {
            if (eventIds.isEmpty()) {
                return;
            }
            attachListeners(context, userId, eventIds);
        });
    }

    /**
     *  Fetches all the events user is registered to
     * @param callback
     */
    private static void fetchUserEvents(Callback callback) {
        if (userId == null) {
            callback.onResult(new ArrayList<>());
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    List<String> events = (List<String>) doc.get("enrolled_events");
                    if (events == null) events = new ArrayList<>();

                    callback.onResult(events);
                })
                .addOnFailureListener(e -> {
                    callback.onResult(new ArrayList<>());
                });
    }

    /**
     * Connects the listener to the event IDs and initiates listening
     * @param context: Context
     * @param userId The id of the user
     * @param eventIds The events user is registered in
     */
    private static void attachListeners(Context context, String userId, List<String> eventIds) {

        for (String notifType : NOTIFICATION_TYPES) {
            for (String eventId : eventIds) {

                ListenerRegistration reg = db.collection("notifications")
                        .document(notifType)
                        .collection(eventId)
                        .document(userId)
                        .addSnapshotListener((snapshot, error) -> {

                            if (error != null) {
                                return;
                            }

                            if (snapshot == null || !snapshot.exists()) {
                                return;
                            }

                            handleNotification(context, snapshot, notifType, eventId, userId);
                        });

                registrations.add(reg);
            }
        }
    }

    /**
     * Triggered when a new user document is added to the notification collection
     * @param context: Context
     * @param doc: The new document created
     * @param notifType: The type of notification sent
     * @param eventId: The id of the event where the user is supposed to receive the notification
     * @param userId: User id
     */
    private static void handleNotification(Context context,
                                           DocumentSnapshot doc,
                                           String notifType,
                                           String eventId,
                                           String userId) {

        Boolean seen = doc.getBoolean("seen_status");
        if (seen != null && seen) {
            return;
        }
        NotificationsManager.pushLocalNotification(
                context,
                notifType,
                eventId,
                userId
        );

        doc.getReference().update("seen_status", true);
    }

    /**
     * Stops the listener
     */
    public static void stopListening() {
        for (ListenerRegistration r : registrations)
            r.remove();

        registrations.clear();
    }

    private interface Callback {
        void onResult(List<String> events);
    }
}

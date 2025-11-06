package com.example.lotteryeventsystem.data;

import androidx.annotation.NonNull;

import com.example.lotteryeventsystem.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Loads event docs from Firestore.
 */
public class FirebaseEventRepository implements EventRepository {
    private final FirebaseFirestore firestore;

    public FirebaseEventRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public FirebaseEventRepository(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void getEventById(String eventId, RepositoryCallback<Event> callback) {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists()) {
                        callback.onComplete(null, null);
                        return;
                    }
                    callback.onComplete(mapEvent(snapshot), null);
                })
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    private Event mapEvent(DocumentSnapshot snapshot) {
        Event event = snapshot.toObject(Event.class);
        if (event == null) {
            event = new Event();
        }
        event.setId(snapshot.getId());
        return event;
    }
}

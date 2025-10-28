package com.example.lotteryeventsystem.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

public class EventRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Get one event by id. */
    public Task<DocumentSnapshot> getEvent(String eventId) {
        return db.collection("events").document(eventId).get();
    }

    /** Live query of events ordered by registration open. Caller must hold the ListenerRegistration to remove(). */
    public ListenerRegistration listenEvents(EventListener<QuerySnapshot> listener) {
        return db.collection("events").orderBy("regOpenAt", Query.Direction.ASCENDING).addSnapshotListener(listener);
    }

    /** Count waiting list size on server. */
    public Task<AggregateQuerySnapshot> getWaitingCount(String eventId) {
        return db.collection("events").document(eventId)
                 .collection("waitingList").count().get(AggregateSource.SERVER);
    }
}

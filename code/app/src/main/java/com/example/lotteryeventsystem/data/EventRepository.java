package com.example.lotteryeventsystem.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

public class EventRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<DocumentSnapshot> getEvent(String eventId) {
        return db.collection("events").document(eventId).get();
    }

    public ListenerRegistration listenEvents(EventListener<QuerySnapshot> listener) {
        return db.collection("events").orderBy("regOpenAt").addSnapshotListener(listener);
    }

    public Task<AggregateQuerySnapshot> getWaitingCount(String eventId) {
        return db.collection("events").document(eventId)
                 .collection("waitingList").count().get(AggregateSource.SERVER);
    }
}

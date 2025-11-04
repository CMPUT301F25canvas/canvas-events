package com.example.lotteryeventsystem;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventRepository {
    /**
     * Class for managing Firestore and the event funciton
     */

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * Adds an Event object to the Firestore "events" collection
     * @param event
     * @return
     */
    public Task<DocumentReference> addEvent (Event event) {
        return db.collection("events")
                .add(event)
                .addOnSuccessListener(docRef ->
                        Log.d("EventRepository", "Event added with ID: " + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.w("EventRepository", "Error adding event", e));
    }



}

package com.example.lotteryeventsystem.data;

import com.example.lotteryeventsystem.model.Event;

/**
 * Gives read access to events wherever they are stored.
 */
public interface EventRepository {
    /**
     * Fetch a single event by its id.
     *
     * @param eventId Firestore document id
     * @param callback callback that receives the event or an error
     */
    void getEventById(String eventId, RepositoryCallback<Event> callback);
}

package com.example.lotteryeventsystem.model;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

/**
 * Basic snapshot of an event document.
 * This is simple on purpose so Firestore can map it.
 */
public class Event {
    private String id;
    private String title;
    private String description;
    private String location;
    private Timestamp registrationOpen;
    private Timestamp registrationClose;
    private Long capacity;
    private String posterUrl;
    private String organizerName;

    public Event() {
        // Firestore likes a public no-arg constructor.
    }

    /**
     * Gets the id string Firestore gives us.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id string. Should match the Firestore doc id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the event title for UI cards.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Stores the event title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Reads the description so we can show more context.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the description text.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gives the event location string.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Saves the location string.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * When the registration window opens up.
     */
    public Timestamp getRegistrationOpen() {
        return registrationOpen;
    }

    /**
     * Sets when people can start registering.
     */
    public void setRegistrationOpen(Timestamp registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    /**
     * When the registration window closes.
     */
    public Timestamp getRegistrationClose() {
        return registrationClose;
    }

    /**
     * Sets the close time for registration.
     */
    public void setRegistrationClose(Timestamp registrationClose) {
        this.registrationClose = registrationClose;
    }

    /**
     * Returns how many people we can take.
     */
    public Long getCapacity() {
        return capacity;
    }

    /**
     * Sets how many entrants the organizer can accept.
     */
    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    /**
     * Returns the poster image url if we have one.
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Sets the poster image url.
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * The organizer name shown on details screens.
     */
    public String getOrganizerName() {
        return organizerName;
    }

    /**
     * Sets the organizer name.
     */
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    /**
     * Friendly text like "20 spots" for the UI.
     */
    @Nullable
    public String friendlyCapacity() {
        if (capacity == null) {
            return null;
        }
        return capacity + " spots";
    }
}

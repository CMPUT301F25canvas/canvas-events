package com.example.lotteryeventsystem;


import java.util.ArrayList;

public class Event {
    private String eventID;
    private String organizerID;
    private String name;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private String posterURL; // Optional
    private boolean geolocationRequirement;
    private Number entrantLimit; // Optional
    private ArrayList<String> waitlist;


    public Event() {

    } // For firestore

    public Event(String eventID, String organizerID, String name, String description,
                 String date, String startTime, String endTime) {
        this.eventID = eventID;
        this.organizerID = organizerID;
        this.name = name;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.entrantLimit = null; // Set null if no limit
    }

    @Override
    public String toString() {
        return name;
    }

    // Getters
    public String getEventID() {
        return eventID;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public boolean getGeolocationRequirement() {
        return geolocationRequirement;
    }

    public Number getEntrantLimit() {
        return entrantLimit;
    }

    // Setters
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public void setOrganizerID(String organizerID) {
        this.organizerID = organizerID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public void setGeolocationRequirement(boolean geolocationRequirement) {
        this.geolocationRequirement = geolocationRequirement;
    }

    public void setEntrantLimit(Integer entrantLimit) {
        this.entrantLimit = entrantLimit;
    }

}

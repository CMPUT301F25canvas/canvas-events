package com.example.lotteryeventsystem;


import java.util.ArrayList;

public class Event {
    private String event_id;
    private String organizer_id;
    private String name;
    private String description;
    private String date;
    private String start_time;
    private String end_time;
    private String posterURL; // Optional
    private boolean geolocation;
    private Number entrant_limit; // Optional
    private ArrayList<String> waitlist;


    public Event() {

    } // For firestore

    public Event(String event_id, String organizer_id, String name, String description,
                 String date, String start_time, String end_time) {
        this.event_id = event_id;
        this.organizer_id = organizer_id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.entrant_limit = null; // Set null if no limit
    }

    // Getters
    public String getEventID() {
        return event_id;
    }

    public String getOrganizerID() {
        return organizer_id;
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
        return start_time;
    }

    public String getEndTime() {
        return end_time;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public boolean getGeolocationRequirement() {
        return geolocation;
    }

    public Number getEntrantLimit() {
        return entrant_limit;
    }

    // Setters
    public void setEventID(String event_id) {
        this.event_id = event_id;
    }

    public void setOrganizerID(String organizer_id) {
        this.organizer_id = organizer_id;
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

    public void setStartTime(String start_time) {
        this.start_time = start_time;
    }

    public void setEndTime(String end_time) {
        this.end_time = end_time;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public void setGeolocationRequirement(boolean geolocation) {
        this.geolocation = geolocation;
    }

    public void setEntrantLimit(Integer entrant_limit) {
        this.entrant_limit = entrant_limit;
    }

}

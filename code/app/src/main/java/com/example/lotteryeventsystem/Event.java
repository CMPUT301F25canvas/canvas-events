package com.example.lotteryeventsystem;


import java.util.ArrayList;
import java.util.List;

public class Event {
    private String eventID;
    private String organizerID;
    private String name;
    private String description;
    private String minAge;
    private String dietaryRestrictions;
    private String otherRestrictions;
    private String date;
    private String startTime;
    private String endTime;
    private String registrationStart;
    private String registrationEnd;
    private Integer sampleSize;
    private List<String> categories;
    private String posterURL; // Optional
    private boolean geolocationRequirement;
    private Integer entrantLimit; // Optional
    private String QRCodeURL;
    private Boolean sampled;


    // For firestore
    public Event() {
    }

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
    public String getMinAge() { return minAge; }
    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public String getOtherRestrictions() { return otherRestrictions; }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getRegistrationStart() {
        return registrationStart;
    }

    public String getRegistrationEnd() {
        return registrationEnd;
    }

    public Integer getSampleSize() {
        return sampleSize;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public boolean getGeolocationRequirement() {
        return geolocationRequirement;
    }

    public Integer getEntrantLimit() {
        return entrantLimit;
    }
    public String getQRCodeURL() {
        return QRCodeURL;
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

    public void setMinAge(String minAge) { this.minAge = minAge; }

    public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }

    public void setOtherRestrictions(String otherRestrictions) { this.otherRestrictions = otherRestrictions; }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setRegistrationStart(String registrationStart) {
        this.registrationStart = registrationStart;
    }

    public void setRegistrationEnd(String registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public void setSampleSize(Integer sampleSize) {
        this.sampleSize = sampleSize;
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

    public void setQRCodeURL(String QRCodeURL) {
        this.QRCodeURL = QRCodeURL;
    }

    public Boolean getSampled() { return sampled; }

    public void setSampled(Boolean sampled) { this.sampled = sampled; }
}

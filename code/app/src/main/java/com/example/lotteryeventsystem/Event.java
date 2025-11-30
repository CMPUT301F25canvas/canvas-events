package com.example.lotteryeventsystem;


import java.util.ArrayList;
import java.util.List;

public class Event {
    private String eventID;
    private String organizerID;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String registrationStart;
    private String registrationEnd;
    private Integer sampleSize;
    private ArrayList<String> categories;
    private String posterURL; // Optional
    private boolean geolocationRequirement;
    private Integer entrantLimit; // Optional
    private String QRCodeURL;
    private String location;
    private Boolean sampled;


    // For firestore
    public Event() {
    }

    public Event(String eventID, String organizerID, String name, String description,
                 String startDate, String endDate, String startTime, String endTime) {
        this.eventID = eventID;
        this.organizerID = organizerID;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;
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

    public ArrayList<String> getCategories() {
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
    public String getLocation() {
        return location;
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

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setCategories(ArrayList<String> categories) {
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
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets whether the event has been sampled.
     * @return true if the event has been sampled, false otherwise
     */
    public Boolean getSampled() { return sampled; }

    /**
     * Sets whether the event has been sampled.
     * @param sampled (true if sampled, false if not sampled)
     */
    public void setSampled(Boolean sampled) { this.sampled = sampled; }
}

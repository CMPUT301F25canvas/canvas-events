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
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String registrationStart;
    private String registrationEnd;
    private Integer sampleSize;
    private boolean isConcertCategory;
    private boolean isSportsCategory;
    private boolean isArtsCategory;
    private boolean isFamilyCategory;
    private String posterURL; // Optional
    private boolean geolocationRequirement;
    private Integer entrantLimit; // Optional
    private String QRCodeURL;
    private String location;


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
    public String getMinAge() { return minAge; }
    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public String getOtherRestrictions() { return otherRestrictions; }

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

    public boolean getIsConcertCategory() {
        return isConcertCategory;
    }

    public boolean getIsSportsCategory() {
        return isSportsCategory;
    }
    public boolean getIsArtsCategory() {
        return isArtsCategory;
    }
    public boolean getIsFamilyCategory() {
        return isFamilyCategory;
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

    public void setMinAge(String minAge) { this.minAge = minAge; }

    public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }

    public void setOtherRestrictions(String otherRestrictions) { this.otherRestrictions = otherRestrictions; }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setIsConcertCategory(boolean isConcertCategory) {
        this.isConcertCategory = isConcertCategory;
    }

    public void setIsSportsCategory(boolean isSportsCategory) {
        this.isSportsCategory = isSportsCategory;
    }

    public void setIsArtsCategory(boolean isArtsCategory) {
        this.isArtsCategory = isArtsCategory;
    }

    public void setIsFamilyCategory(boolean isFamilyCategory) {
        this.isFamilyCategory = isFamilyCategory;
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
}

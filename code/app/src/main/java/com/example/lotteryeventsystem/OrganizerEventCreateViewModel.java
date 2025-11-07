package com.example.lotteryeventsystem;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * View Model class for the OrganizerEventCreateFragment class.
 * Separates the UI components and the data components of the application
 */
public class OrganizerEventCreateViewModel extends ViewModel {
    // Uses MutableLiveData to follow Model-View-ViewModel Android Design
    private MutableLiveData<String> name = new MutableLiveData<>();
    private MutableLiveData<String> description = new MutableLiveData<>();
    private MutableLiveData<String> date = new MutableLiveData<>();
    private MutableLiveData<String> eventStart = new MutableLiveData<>();
    private MutableLiveData<String> eventEnd = new MutableLiveData<>();
    private MutableLiveData<String> registrationStart = new MutableLiveData<>();
    private MutableLiveData<String> registrationEnd = new MutableLiveData<>();
    private MutableLiveData<Integer> sampleSize = new MutableLiveData<>();
    // TODO: add the List of Categories
    private MutableLiveData<String> posterURL = new MutableLiveData<>();
    private MutableLiveData<Boolean> geolocationRequirement = new MutableLiveData<>(false);
    private MutableLiveData<Integer> entrantLimit = new MutableLiveData<>();

    // Getters for LiveData
    public LiveData<String> getName() {
        return name;
    }

    public LiveData<String> getDescription() {
        return description;
    }

    public LiveData<String> getDate() {
        return date;
    }

    public LiveData<String> getEventStart() {
        return eventStart;
    }

    public LiveData<String> getEventEnd() {
        return eventEnd;
    }

    public LiveData<String> getRegistrationStart() {
        return registrationStart;
    }

    public LiveData<String> getRegistrationEnd() {
        return registrationEnd;
    }

    public LiveData<Integer> getSampleSize() {
        return sampleSize;
    }

    public LiveData<String> getPosterURL() {
        return posterURL;
    }

    public LiveData<Boolean> getGeolocationRequirement() {
        return geolocationRequirement;
    }

    public LiveData<Integer> getEntrantLimit() {
        return entrantLimit;
    }

    // Setters
    public void setName(String value) {
        name.setValue(value);
    }

    public void setDescription(String value) {
        description.setValue(value);
    }

    public void setDate(String value) {
        date.setValue(value);
    }

    // Setters
    public void setEventStart(String value) {
        eventStart.setValue(value);
    }

    public void setEventEnd(String value) {
        eventEnd.setValue(value);
    }

    public void setRegistrationStart(String value) {
        registrationStart.setValue(value);
    }

    public void setRegistrationEnd(String value) {
        registrationEnd.setValue(value);
    }

    public void setSampleSize(Integer value) {
        sampleSize.setValue(value);
    }

    public void setPosterURL(String value) {
        posterURL.setValue(value);
    }

    public void setGeolocationRequirement(Boolean value) {
        geolocationRequirement.setValue(value);
    }

    public void setEntrantLimit(Integer value) {
        entrantLimit.setValue(value);
    }

    // Helper methods to get current values
    public String getNameValue() {
        return name.getValue();
    }

    public String getDescriptionValue() {
        return description.getValue();
    }

    public String getDateValue() {
        return date.getValue();
    }

    public String getEventStartValue() {
        return eventStart.getValue();
    }

    public String getEventEndValue() {
        return eventEnd.getValue();
    }

    public String getRegistrationStartValue() {
        return registrationStart.getValue();
    }

    public String getRegistrationEndValue() {
        return registrationEnd.getValue();
    }

    public Integer getSampleSizeValue() {
        return sampleSize.getValue();
    }

    public String getPosterURLValue() {
        return posterURL.getValue();
    }

    public Boolean getGeolocationRequirementValue() {
        Boolean value = geolocationRequirement.getValue();
        return value != null ? value : false;
    }

    public Integer getEntrantLimitValue() {
        return entrantLimit.getValue();
    }

    /**
     * Method to determine if all required values for event creation is filled in correctly
     * @return boolean value
     */
    public boolean isEventValid() {
        return name.getValue() != null && !name.getValue().isEmpty() &&
                description.getValue() != null && !description.getValue().isEmpty() &&
                date.getValue() != null && !date.getValue().isEmpty() &&
                eventStart.getValue() != null && !eventStart.getValue().isEmpty() &&
                eventEnd.getValue() != null && !eventEnd.getValue().isEmpty();
    }

    /**
     * Verifies that the event date is after the current date
     * @param date date of the event
     * @return boolean value
     */
    public boolean eventDateValid(String date, DateTimeFormatter formatter) {
        LocalDate eventDate = LocalDate.parse(date, formatter);
        LocalDate currentDate = LocalDate.now();
        return eventDate.isAfter(currentDate);
    }

    /**
     * Verifies that the registration period is valid
     * Checks to see that the eventDate is after the registration period and that the registration period has not passed yet
     * @param date event date
     * @param regStart start of registration period
     * @param regEnd end of registration period
     * @return boolean value
     */
    public boolean registrationPeriodValid(String date, String regStart, String regEnd, DateTimeFormatter formatter) {
        LocalDate eventDate = LocalDate.parse(date, formatter);
        LocalDate registrationStart = LocalDate.parse(regStart, formatter);
        LocalDate registrationEnd = LocalDate.parse(regEnd, formatter);
        LocalDate currentDate = LocalDate.now();

        return registrationStart.isBefore(registrationEnd) &&
                currentDate.isBefore(registrationEnd) &&
                registrationEnd.isBefore(eventDate);
    }

    /**
     * Verifies that the event timing is correct
     * @param startTime start time of the event
     * @param endTime end time of the event
     * @return boolean value
     */
    public boolean eventTimeValid(String startTime, String endTime, DateTimeFormatter formatter) {
        LocalTime start = LocalTime.parse(startTime, formatter);
        LocalTime end = LocalTime.parse(endTime, formatter);
        return end.isAfter(start);
    }

    /**
     * Method to clear the the values of this class after Event Creation is complete
     */
    public void clearForm() {
        name.setValue(null);
        description.setValue(null);
        date.setValue(null);
        eventStart.setValue(null);
        eventEnd.setValue(null);
        registrationStart.setValue(null);
        registrationEnd.setValue(null);
        sampleSize.setValue(null);
        posterURL.setValue(null);
        geolocationRequirement.setValue(false);
        entrantLimit.setValue(null);
    }


}

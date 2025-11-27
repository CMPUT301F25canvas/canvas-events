package com.example.lotteryeventsystem;

import android.net.Uri;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EventCreationForm {

    private Uri localImageUri = null;

    public Uri getLocalImageUri() {
        return localImageUri;
    }

    public void setLocalImageUri(Uri localImageUri) {
        this.localImageUri = localImageUri;
    }

    /**
     * Method to determine if all required values for event creation is filled in correctly
     * @param event the values to be assessed
     * @return boolean value
     */
    public boolean isEventValid(Event event) {
        return event.getName() != null && !event.getName().isEmpty() &&
                event.getDescription() != null && !event.getDescription().isEmpty() &&
                event.getStartDate() != null && !event.getStartDate().isEmpty() &&
                event.getEndDate() != null && !event.getEndDate().isEmpty() &&
                event.getLocation() != null && !event.getLocation().isEmpty() &&
                event.getStartTime()!= null && !event.getStartTime().isEmpty() &&
                event.getEndTime() != null && !event.getEndTime().isEmpty() &&
                event.getRegistrationStart() != null && !event.getRegistrationStart().isEmpty() &&
                event.getRegistrationEnd() != null && !event.getRegistrationEnd().isEmpty() &&
                event.getSampleSize() != null && (int) event.getSampleSize() > 0 ;
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

    public boolean startEndDateValid(String startDate, String endDate, DateTimeFormatter formatter) {
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        return end.isAfter(start);
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

}

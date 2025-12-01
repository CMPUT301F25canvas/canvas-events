package com.example.lotteryeventsystem;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class EventCreationFormUnitTest {

    private EventCreationForm form;
    private DateTimeFormatter formatter;

    @Before
    public void setup() {
        form = new EventCreationForm();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }


    @Test
    public void testEventValid_AllFieldsSet() {
        Event event = new Event();
        event.setName("Test Event");
        event.setDescription("Description");
        event.setStartDate("2026-05-01");
        event.setEndDate("2026-05-02");
        event.setLocation("Edmonton");
        event.setStartTime("10:00 AM");
        event.setEndTime("11:00 AM");
        event.setRegistrationStart("2026-04-01");
        event.setRegistrationEnd("2026-04-20");
        event.setSampleSize(10);

        assertTrue(form.isEventValid(event));
    }

    @Test
    public void testEventValid_MissingField_ReturnsFalse() {
        Event event = new Event();
        event.setDescription("Description");
        event.setStartDate("2025-05-01");
        event.setEndDate("2025-05-02");
        event.setLocation("Edmonton");
        event.setStartTime("10:00 AM");
        event.setEndTime("11:00 AM");
        event.setRegistrationStart("2025-04-01");
        event.setRegistrationEnd("2025-04-20");
        event.setSampleSize(10);

        assertFalse(form.isEventValid(event));
    }

    @Test
    public void testEventValid_InvalidSampleSize() {
        Event event = new Event();
        event.setName("Test");
        event.setDescription("Desc");
        event.setStartDate("2025-05-01");
        event.setEndDate("2025-05-02");
        event.setLocation("Edmonton");
        event.setStartTime("10:00 AM");
        event.setEndTime("11:00 AM");
        event.setRegistrationStart("2025-04-01");
        event.setRegistrationEnd("2025-04-20");
        event.setSampleSize(0); // invalid

        assertFalse(form.isEventValid(event));
    }

    @Test
    public void testEventDateValid_FutureDate_ReturnsTrue() {
        String future = LocalDate.now().plusDays(1).format(formatter);
        assertTrue(form.eventDateValid(future, formatter));
    }

    @Test
    public void testEventDateValid_PastDate_ReturnsFalse() {
        String past = LocalDate.now().minusDays(1).format(formatter);
        assertFalse(form.eventDateValid(past, formatter));
    }

    @Test
    public void testStartEndDateValid_StartBeforeEnd_ReturnsTrue() {
        assertTrue(form.startEndDateValid("2025-01-01", "2025-01-02", formatter));
    }

    @Test
    public void testStartEndDateValid_StartEqualsEnd_ReturnsTrue() {
        assertTrue(form.startEndDateValid("2025-01-01", "2025-01-01", formatter));
    }

    @Test
    public void testStartEndDateValid_EndBeforeStart_ReturnsFalse() {
        assertFalse(form.startEndDateValid("2025-01-02", "2025-01-01", formatter));
    }


    @Test
    public void testRegistrationPeriodValid_ValidPeriod_ReturnsTrue() {
        LocalDate now = LocalDate.now();

        String event = now.plusDays(10).format(formatter);
        String regStart = now.minusDays(1).format(formatter);
        String regEnd = now.plusDays(5).format(formatter);

        assertTrue(form.registrationPeriodValid(event, regStart, regEnd, formatter));
    }

    @Test
    public void testRegistrationPeriodValid_Invalid_DatesNotBeforeEvent_ReturnsFalse() {
        LocalDate now = LocalDate.now();

        String event = now.plusDays(5).format(formatter);
        String regStart = now.minusDays(1).format(formatter);
        String regEnd = now.plusDays(10).format(formatter);  // wrong: after event date

        assertFalse(form.registrationPeriodValid(event, regStart, regEnd, formatter));
    }

    @Test
    public void testRegistrationPeriodValid_Invalid_RegStartAfterRegEnd() {
        LocalDate now = LocalDate.now();

        String event = now.plusDays(10).format(formatter);
        String regStart = now.plusDays(4).format(formatter);
        String regEnd = now.plusDays(2).format(formatter);

        assertFalse(form.registrationPeriodValid(event, regStart, regEnd, formatter));
    }

    @Test
    public void testEventTimeValid_EndAfterStart_ReturnsTrue() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        assertTrue(form.eventTimeValid("10:00", "12:00", timeFormatter));
    }

    @Test
    public void testEventTimeValid_EndBeforeStart_ReturnsFalse() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        assertFalse(form.eventTimeValid("12:00", "10:00", timeFormatter));
    }
}

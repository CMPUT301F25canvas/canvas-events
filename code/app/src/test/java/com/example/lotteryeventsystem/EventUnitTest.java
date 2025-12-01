package com.example.lotteryeventsystem;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Optional;

public class EventUnitTest {
    private Event event;

    @Before
    public void setUp() {
        event = new Event();
    }

    // Title tests
    @Test
    public void testSetAndGetName() {
        String testName = "Summer Music Festival";
        event.setName(testName);
        assertEquals(testName, event.getName());
    }

    @Test
    public void testTitleIsNullByDefault() {
        assertNull(event.getName());
    }

    // Description tests
    @Test
    public void testSetAndGetDescription() {
        String testDescription = "A fun outdoor music event";
        event.setDescription(testDescription);
        assertEquals(testDescription, event.getDescription());
    }

    @Test
    public void testDescriptionIsNullByDefault() {
        assertNull(event.getDescription());
    }

    // Location tests
    @Test
    public void testSetAndGetLocation() {
        String testLocation = "Central Park, NYC";
        event.setLocation(testLocation);
        assertEquals(testLocation, event.getLocation());
    }

    @Test
    public void testLocationIsNullByDefault() {
        assertNull(event.getLocation());
    }

    // Registration Start tests
    @Test
    public void testSetAndGetRegistrationStart() {
        String testTime = Timestamp.now().toString();
        event.setRegistrationStart(testTime);
        assertEquals(testTime, event.getRegistrationStart());
    }

    @Test
    public void testRegistrationOpenIsNullByDefault() {
        assertNull(event.getRegistrationStart());
    }

    // Registration End tests
    @Test
    public void testSetAndGetRegistrationClose() {
        String testTime = Timestamp.now().toString();
        event.setRegistrationEnd(testTime);
        assertEquals(testTime, event.getRegistrationEnd());
    }

    @Test
    public void testRegistrationCloseIsNullByDefault() {
        assertNull(event.getRegistrationEnd());
    }

    // Capacity tests
    @Test
    public void testSetAndGetCapacity() {
        Integer testCapacity = 500;
        event.setEntrantLimit(testCapacity);
        assertEquals(testCapacity, event.getEntrantLimit());
    }

    @Test
    public void testCapacityIsNullByDefault() {
        assertNull(event.getEntrantLimit());
    }

    @Test
    public void testCapacityCanBeZero() {
        event.setEntrantLimit(0);
        assertEquals((Integer) 0, event.getEntrantLimit());
    }

    // Poster URL tests
    @Test
    public void testSetAndGetPosterUrl() {
        String testUrl = "https://example.com/poster.jpg";
        event.setPosterURL(testUrl);
        assertEquals(testUrl, event.getPosterURL());
    }

    @Test
    public void testPosterUrlIsNullByDefault() {
        assertNull(event.getPosterURL());
    }

    // Organizer ID tests
    @Test
    public void testSetAndGetOrganizerID() {
        String testOrganizerID = "OrganizerID";
        event.setOrganizerID(testOrganizerID);
        assertEquals(testOrganizerID, event.getOrganizerID());
    }

    @Test
    public void testOrganizerIDIsNullByDefault() {
        assertNull(event.getOrganizerID());
    }

    // Integration tests
    @Test
    public void testMultipleSettersOnSameObject() {
        event.setEventID("evt-001");
        event.setName("Event 1");
        event.setDescription("Event Description");
        event.setLocation("Edmonton");

        assertEquals("evt-001", event.getEventID());
        assertEquals("Event 1", event.getName());
        assertEquals("Event Description", event.getDescription());
        assertEquals("Edmonton", event.getLocation());
    }
}
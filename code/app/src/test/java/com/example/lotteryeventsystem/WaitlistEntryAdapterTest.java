package com.example.lotteryeventsystem;

import org.junit.Test;
import static org.junit.Assert.*;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;
import java.util.ArrayList;

public class WaitlistEntryAdapterTest {
    @Test
    public void testAdapterDataStructure_withValidParameters_shouldStoreData() {
        ArrayList<WaitlistEntry> entries = new ArrayList<>();
        entries.add(new WaitlistEntry("1", "user1", "John Doe", WaitlistStatus.CONFIRMED));
        WaitlistEntry entry = entries.get(0);
        assertEquals("1", entry.getId());
        assertEquals("user1", entry.getEntrantId());
        assertEquals("John Doe", entry.getEntrantName());
        assertEquals(WaitlistStatus.CONFIRMED, entry.getStatus());
    }

    @Test
    public void testAdapterDataStructure_withMultipleEntries_shouldMaintainOrder() {
        ArrayList<WaitlistEntry> entries = new ArrayList<>();
        entries.add(new WaitlistEntry("1", "user1", "First User", WaitlistStatus.CONFIRMED));
        entries.add(new WaitlistEntry("2", "user2", "Second User", WaitlistStatus.INVITED));
        entries.add(new WaitlistEntry("3", "user3", "Third User", WaitlistStatus.CANCELLED));
        assertEquals("First User", entries.get(0).getEntrantName());
        assertEquals("Second User", entries.get(1).getEntrantName());
        assertEquals("Third User", entries.get(2).getEntrantName());
    }

    @Test
    public void testWaitlistEntryGetters_shouldReturnCorrectValues() {
        WaitlistEntry entry = new WaitlistEntry("test-id", "test-user", "Test Name", WaitlistStatus.DECLINED);
        assertEquals("test-id", entry.getId());
        assertEquals("test-user", entry.getEntrantId());
        assertEquals("Test Name", entry.getEntrantName());
        assertEquals(WaitlistStatus.DECLINED, entry.getStatus());
    }

    @Test
    public void testAnonymousEntrantData_shouldHaveNullNames() {
        // Test that data is set up correctly for anonymous entrants
        ArrayList<WaitlistEntry> entries = new ArrayList<>();
        entries.add(new WaitlistEntry("1", "user1", null, WaitlistStatus.CONFIRMED));
        entries.add(new WaitlistEntry("2", "user2", null, WaitlistStatus.INVITED));
        assertEquals(2, entries.size());
        assertNull(entries.get(0).getEntrantName());
        assertNull(entries.get(1).getEntrantName());
    }
}
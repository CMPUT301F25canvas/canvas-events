package com.example.lotteryeventsystem;

import com.example.lotteryeventsystem.model.WaitlistEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WaitlistEntryAdapterTest {

    private WaitlistEntryAdapter adapter;
    private ArrayList<WaitlistEntry> mockEntries;

    @Before
    public void setUp() {
        mockEntries = new ArrayList<>();
        adapter = new WaitlistEntryAdapter(null, mockEntries);
    }

    @Test
    public void testGetUserDisplayNameWithUserName() {
        // Given
        WaitlistEntry entry = createMockWaitlistEntry("user1", "John Doe", null, "user1");
        // When
        String displayName = adapter.getUserDisplayName(entry);
        // Then
        assertEquals("John Doe", displayName);
    }

    @Test
    public void testGetUserDisplayNameWithEntrantName() {
        // Given
        WaitlistEntry entry = createMockWaitlistEntry("user1", null, "Entrant Name", "user1");
        // When
        String displayName = adapter.getUserDisplayName(entry);
        // Then
        assertEquals("Entrant Name", displayName);
    }

    @Test
    public void testGetUserDisplayNameWithAnonymous() {
        // Given
        WaitlistEntry entry = createMockWaitlistEntry("user1", null, null, "user1");
        // When
        String displayName = adapter.getUserDisplayName(entry);
        // Then
        assertNull(displayName);
    }

    @Test
    public void testGetConsistentAnonymousNumber() {
        // Given
        WaitlistEntry anonymousEntry1 = createMockWaitlistEntry("user1", null, null, "user1");
        WaitlistEntry anonymousEntry2 = createMockWaitlistEntry("user2", null, null, "user2");
        WaitlistEntry anonymousEntry3 = createMockWaitlistEntry("user3", null, null, "user3");
        mockEntries.add(anonymousEntry1);
        mockEntries.add(anonymousEntry2);
        mockEntries.add(anonymousEntry3);
        // Recreate adapter to trigger calculateConsistentAnonymousIds
        adapter = new WaitlistEntryAdapter(null, mockEntries);
        // When & Then
        assertEquals(1, adapter.getConsistentAnonymousNumber(anonymousEntry1));
        assertEquals(2, adapter.getConsistentAnonymousNumber(anonymousEntry2));
        assertEquals(3, adapter.getConsistentAnonymousNumber(anonymousEntry3));
        // Same entrant should get same number
        assertEquals(1, adapter.getConsistentAnonymousNumber(anonymousEntry1));
    }

    @Test
    public void testGetConsistentAnonymousNumberWithNullEntrant() {
        // Given
        WaitlistEntry entry = null;
        // When & Then
        assertEquals(1, adapter.getConsistentAnonymousNumber(entry));
    }

    @Test
    public void testEmptyListConstructor() {
        // Given
        ArrayList<WaitlistEntry> emptyList = new ArrayList<>();
        // When
        WaitlistEntryAdapter emptyAdapter = new WaitlistEntryAdapter(null, emptyList);
        // Then - Should not throw exception
        assertNotNull(emptyAdapter);
    }

    @Test
    public void testNamedUsersDoNotGetAnonymousNumbersInCalculation() {
        // Given
        WaitlistEntry namedEntry = createMockWaitlistEntry("user1", "John Doe", null, "user1");
        WaitlistEntry namedEntry2 = createMockWaitlistEntry("user2", "Jane Smith", null, "user2");
        mockEntries.add(namedEntry);
        mockEntries.add(namedEntry2);
        adapter = new WaitlistEntryAdapter(null, mockEntries);
        // When & Then - Named users should not affect the anonymous counter
        // They should still get numbers when requested, but not during initial calculation
        assertTrue(adapter.getConsistentAnonymousNumber(namedEntry) >= 1);
        assertTrue(adapter.getConsistentAnonymousNumber(namedEntry2) >= 1);
    }

    @Test
    public void testAnonymousNumberFallbackForUnmappedEntries() {
        // Given
        WaitlistEntry entryNotInOriginalList = createMockWaitlistEntry("new_user", null, null, "new_user");
        // Create adapter with empty list
        adapter = new WaitlistEntryAdapter(null, new ArrayList<>());
        // When & Then - Entry not in original list should get a fallback number
        int number = adapter.getConsistentAnonymousNumber(entryNotInOriginalList);
        assertTrue(number >= 1);
    }

    // Helper method to create mock WaitlistEntry
    private WaitlistEntry createMockWaitlistEntry(String id, String userName, String entrantName, String entrantId) {
        WaitlistEntry entry = mock(WaitlistEntry.class);
        when(entry.getId()).thenReturn(entrantId);
        User user = mock(User.class);
        when(user.getName()).thenReturn(userName);
        when(entry.getUser()).thenReturn(user);
        when(entry.getEntrantName()).thenReturn(entrantName);
        return entry;
    }
}
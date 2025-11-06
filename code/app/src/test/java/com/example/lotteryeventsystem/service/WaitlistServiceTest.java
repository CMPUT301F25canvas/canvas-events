package com.example.lotteryeventsystem.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.data.WaitlistRepository;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quick tests for the waitlist business logic.
 */
public class WaitlistServiceTest {
    private static final String EVENT_ID = "event-123";

    private FakeWaitlistRepository repository;
    private WaitlistService waitlistService;

    @Before
    public void setUp() {
        repository = new FakeWaitlistRepository();
        waitlistService = new WaitlistService(repository, new Random(7));
    }

    @Test
    public void drawReplacement_picksCandidateAndMarksInvited() {
        repository.seedEntry(EVENT_ID, new WaitlistEntry("w1", "u1", "Jess", WaitlistStatus.WAITING));
        repository.seedEntry(EVENT_ID, new WaitlistEntry("w2", "u2", "Mark", WaitlistStatus.WAITING));

        TestCallback callback = new TestCallback();
        waitlistService.drawReplacement(EVENT_ID, callback);

        assertNull(callback.error);
        assertNotNull(callback.replacement);
        assertEquals(WaitlistStatus.INVITED, callback.replacement.getStatus());

        long invitedCount = repository.entriesFor(EVENT_ID).values().stream()
                .filter(entry -> entry.getStatus() == WaitlistStatus.INVITED)
                .count();
        assertEquals(1, invitedCount);
    }

    @Test
    public void drawReplacement_returnsNullWhenNoOptions() {
        TestCallback callback = new TestCallback();
        waitlistService.drawReplacement(EVENT_ID, callback);

        assertNull(callback.error);
        assertNull(callback.replacement);
    }

    @Test
    public void markEntrantDeclined_invitesNextPerson() {
        WaitlistEntry invited = new WaitlistEntry("invited-1", "u1", "Jess", WaitlistStatus.INVITED);
        repository.seedEntry(EVENT_ID, invited);
        repository.seedEntry(EVENT_ID, new WaitlistEntry("waiting-1", "u2", "Avery", WaitlistStatus.WAITING));
        repository.seedEntry(EVENT_ID, new WaitlistEntry("waiting-2", "u3", "Sky", WaitlistStatus.WAITING));

        TestCallback callback = new TestCallback();
        waitlistService.markEntrantDeclined(EVENT_ID, invited.getId(), callback);

        assertNull(callback.error);
        assertNotNull(callback.replacement);
        assertEquals(WaitlistStatus.INVITED, callback.replacement.getStatus());

        WaitlistEntry declined = repository.entriesFor(EVENT_ID).get(invited.getId());
        assertEquals(WaitlistStatus.DECLINED, declined.getStatus());
        assertTrue(repository.declineCount.get() > 0);
    }

    private static class TestCallback implements WaitlistService.WaitlistActionCallback {
        WaitlistEntry replacement;
        Exception error;

        @Override
        public void onSuccess(WaitlistEntry replacement) {
            this.replacement = replacement;
        }

        @Override
        public void onFailure(Exception error) {
            this.error = error;
        }
    }

    private static class FakeWaitlistRepository implements WaitlistRepository {
        private final Map<String, Map<String, WaitlistEntry>> store = new HashMap<>();
        private final AtomicInteger declineCount = new AtomicInteger(0);

        void seedEntry(String eventId, WaitlistEntry entry) {
            store.computeIfAbsent(eventId, ignored -> new HashMap<>())
                    .put(entry.getId(), entry);
        }

        Map<String, WaitlistEntry> entriesFor(String eventId) {
            return store.getOrDefault(eventId, new HashMap<>());
        }

        @Override
        public void getInvitedEntrants(String eventId, RepositoryCallback<List<WaitlistEntry>> callback) {
            callback.onComplete(filterByStatus(eventId, WaitlistStatus.INVITED), null);
        }

        @Override
        public void getWaitingEntrants(String eventId, RepositoryCallback<List<WaitlistEntry>> callback) {
            callback.onComplete(filterByStatus(eventId, WaitlistStatus.WAITING), null);
        }

        @Override
        public void updateEntrantStatus(String eventId,
                                        String entrantRecordId,
                                        WaitlistStatus status,
                                        RepositoryCallback<WaitlistEntry> callback) {
            WaitlistEntry entry = entriesFor(eventId).get(entrantRecordId);
            if (entry == null) {
                callback.onComplete(null, new IllegalArgumentException("entry missing"));
                return;
            }
            entry.setStatus(status);
            callback.onComplete(entry, null);
        }

        @Override
        public void incrementDeclineCount(String eventId) {
            declineCount.incrementAndGet();
        }

        private List<WaitlistEntry> filterByStatus(String eventId, WaitlistStatus status) {
            List<WaitlistEntry> result = new ArrayList<>();
            for (WaitlistEntry entry : entriesFor(eventId).values()) {
                if (entry.getStatus() == status) {
                    result.add(entry);
                }
            }
            return result;
        }
    }
}

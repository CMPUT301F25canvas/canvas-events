package com.example.lotteryeventsystem.data;

import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;

import java.util.List;

/**
 * Waitlist operations go through here so we can swap data sources.
 */
public interface WaitlistRepository {
    void getInvitedEntrants(String eventId, RepositoryCallback<List<WaitlistEntry>> callback);

    void getWaitingEntrants(String eventId, RepositoryCallback<List<WaitlistEntry>> callback);

    void updateEntrantStatus(String eventId,
                             String entrantRecordId,
                             WaitlistStatus status,
                             RepositoryCallback<WaitlistEntry> callback);

    void getEntrantsByStatus(String eventId, List<WaitlistStatus> statuses, RepositoryCallback<List<WaitlistEntry>> callback);
    void incrementDeclineCount(String eventId);
}

package com.example.lotteryeventsystem.service;

import androidx.annotation.Nullable;

import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.data.WaitlistRepository;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;

import java.util.List;
import java.util.Random;

/**
 * Handles waitlist transitions like drawing new entrants.
 */
public class WaitlistService {
    public interface WaitlistActionCallback {
        void onSuccess(@Nullable WaitlistEntry replacement);

        void onFailure(Exception error);
    }

    private final WaitlistRepository repository;
    private final Random random;

    public WaitlistService(WaitlistRepository repository) {
        this(repository, new Random());
    }

    public WaitlistService(WaitlistRepository repository, Random random) {
        this.repository = repository;
        this.random = random;
    }

    /**
     * Called when an invited entrant declines. The next entrant is drawn automatically.
     */
    public void markEntrantDeclined(String eventId,
                                    String entrantRecordId,
                                    WaitlistActionCallback callback) {
        repository.updateEntrantStatus(eventId, entrantRecordId, WaitlistStatus.DECLINED,
                (entry, error) -> {
                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }
                    repository.incrementDeclineCount(eventId);
                    drawReplacement(eventId, callback);
                });
    }

    /**
     * Organizer manually triggers a new draw.
     */
    public void drawReplacement(String eventId,
                                WaitlistActionCallback callback) {
        repository.getWaitingEntrants(eventId, (entries, error) -> {
            if (error != null) {
                callback.onFailure(error);
                return;
            }
            if (entries == null || entries.isEmpty()) {
                callback.onSuccess(null);
                return;
            }
            WaitlistEntry candidate = pickRandom(entries);
            repository.updateEntrantStatus(eventId, candidate.getId(), WaitlistStatus.INVITED,
                    (updated, updateError) -> {
                        if (updateError != null) {
                            callback.onFailure(updateError);
                            return;
                        }
                        callback.onSuccess(updated);
                    });
        });
    }

    private WaitlistEntry pickRandom(List<WaitlistEntry> entries) {
        int index = random.nextInt(entries.size());
        return entries.get(index);
    }
}

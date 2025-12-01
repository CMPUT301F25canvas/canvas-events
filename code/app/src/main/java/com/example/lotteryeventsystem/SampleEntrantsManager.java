package com.example.lotteryeventsystem;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.data.WaitlistRepository;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the sampling process for event entrants, including random selection,
 * status updates, and notification sending. Handles both bulk sampling for initial
 * selection and single sampling for replacement after cancellations.
 * This class coordinates with Firestore for data persistence and the notification
 * system for user communication.
 *
 * @author Emily Lan
 * @version 1.0
 * @see WaitlistRepository
 * @see WaitlistEntry
 * @see WaitlistStatus
 * @see NotificationsManager
 */
public class SampleEntrantsManager {
    private Context context;
    private WaitlistRepository waitlistRepository;
    private String eventId;

    /**
     * Constructs a new SampleEntrantsManager for the specified event.
     *
     * @param context the Android context for displaying toasts and accessing resources
     * @param waitlistRepository the repository for waitlist data operations
     * @param eventId the unique identifier of the event to manage sampling for
     */
    public SampleEntrantsManager(Context context, WaitlistRepository waitlistRepository, String eventId) {
        this.context = context;
        this.waitlistRepository = waitlistRepository;
        this.eventId = eventId;
    }

    /**
     * Selects a random sample of entrants from the waiting list and updates their status.
     * Handles both bulk sampling (initial selection) and single sampling (replacement after deletion).
     * Selected entrants are moved to INVITED status and notified, while non-selected entrants
     * (in bulk mode) are notified of not being chosen.
     *
     * @param sampleSize the number of entrants to sample; use -1 for single sampling after deletion
     * @param currentEvent the event object for bulk sampling, or null for single sampling
     * @param callback the callback to handle completion or errors of the sampling operation
     */
    public void selectRandomSample(int sampleSize, Event currentEvent, SamplingCallback callback) {
        waitlistRepository.getWaitingEntrants(eventId, new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onComplete(List<WaitlistEntry> result, Exception error) {
                boolean isSingleSample = sampleSize == -1;
                if (error != null) {
                    if (!isSingleSample) {
                        Toast.makeText(context, "Error loading entrants: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    if (callback != null) callback.onComplete(error);
                    return;
                }
                if (result == null || result.isEmpty()) {
                    if (!isSingleSample) {
                        Toast.makeText(context, "No waiting entrants found", Toast.LENGTH_SHORT).show();
                    }
                    if (callback != null) callback.onComplete(null);
                    return;
                }
                int actualSampleSize = isSingleSample ? 1 : sampleSize;
                List<WaitlistEntry> selectedEntrants;
                List<WaitlistEntry> notSelectedEntrants;
                if (result.size() <= actualSampleSize) {
                    selectedEntrants = new ArrayList<>(result);
                    notSelectedEntrants = new ArrayList<>();
                    if (!isSingleSample) {
                        String message = "Selected " + selectedEntrants.size() + " entrants and notified all applicants";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedEntrants = getRandomSample(result, actualSampleSize);
                    notSelectedEntrants = isSingleSample ? new ArrayList<>() : getNotSelectedEntrants(result, selectedEntrants);
                    if (!isSingleSample) {
                        String message = "Selected " + selectedEntrants.size() + " entrants and notified all applicants";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                }
                updateEntrantsStatus(selectedEntrants, WaitlistStatus.INVITED);
                sendSelectedNotifications(selectedEntrants);
                if (!isSingleSample) {
                    sendNotSelectedNotifications(notSelectedEntrants);
                }
                if (!isSingleSample && currentEvent != null) {
                    markEventAsSampled(currentEvent);
                }
                if (callback != null) callback.onComplete(null);
            }
        });
    }

    /**
     * Method for sampling a single entrant after deletion.
     * Used to replace an entrant who was canceled or declined their invitation.
     *
     * @param callback the callback to handle completion or errors of the sampling operation
     */
    public void sampleSingleEntrantAfterDeletion(SamplingCallback callback) {
        selectRandomSample(-1, null, callback);
    }

    /**
     * Selects a random sample of entrants from the provided list using shuffling.
     * Ensures the sample size does not exceed the available number of entrants.
     *
     * @param allEntrants the complete list of entrants to sample from
     * @param sampleSize the desired number of entrants to select
     * @return a list containing the randomly selected entrants
     */
    private List<WaitlistEntry> getRandomSample(List<WaitlistEntry> allEntrants, int sampleSize) {
        List<WaitlistEntry> shuffled = new ArrayList<>(allEntrants);
        Collections.shuffle(shuffled);
        int actualSampleSize = Math.min(sampleSize, shuffled.size());
        return shuffled.subList(0, actualSampleSize);
    }

    /**
     * Returns the list of entrants who were not selected from the original pool.
     * Calculated by removing the selected entrants from the complete list.
     *
     * @param allEntrants the complete list of original entrants
     * @param selected the list of entrants who were selected
     * @return a list containing the entrants who were not selected
     */
    private List<WaitlistEntry> getNotSelectedEntrants(List<WaitlistEntry> allEntrants, List<WaitlistEntry> selected) {
        List<WaitlistEntry> notSelected = new ArrayList<>(allEntrants);
        notSelected.removeAll(selected);
        return notSelected;
    }

    /**
     * Updates the status for a list of entrants in the waitlist repository.
     * Processes each entrant asynchronously and logs any errors encountered.
     *
     * @param entrants the list of entrants to update
     * @param status the new status to assign to the entrants
     */
    private void updateEntrantsStatus(List<WaitlistEntry> entrants, WaitlistStatus status) {
        for (WaitlistEntry entrant : entrants) {
            waitlistRepository.updateEntrantStatus(eventId, entrant.getId(), status,
                    new RepositoryCallback<WaitlistEntry>() {
                        @Override
                        public void onComplete(WaitlistEntry result, Exception error) {
                            if (error != null) {
                                Log.e("SampleSelection", "Error updating entrant " + entrant.getId() + ": " + error.getMessage());
                            }
                        }
                    });
        }
    }

    /**
     * Sends selection notifications to all selected entrants.
     * Notifies users that they have been chosen for the event.
     *
     * @param selectedEntrants the list of entrants who were selected
     */
    private void sendSelectedNotifications(List<WaitlistEntry> selectedEntrants) {
        for (WaitlistEntry entrant : selectedEntrants) {
            String userId = entrant.getId();
            if (userId != null && !userId.isEmpty()) {
                NotificationsManager.sendSelected(context, eventId, userId);
            }
        }
    }

    /**
     * Sends non-selection notifications to entrants who were not chosen.
     * Only used during bulk sampling operations.
     *
     * @param notSelectedEntrants the list of entrants who were not selected
     */
    private void sendNotSelectedNotifications(List<WaitlistEntry> notSelectedEntrants) {
        for (WaitlistEntry entrant : notSelectedEntrants) {
            String userId = entrant.getId();
            if (userId != null && !userId.isEmpty()) {
                NotificationsManager.sendNotSelected(context, eventId, userId);
            }
        }
    }

    /**
     * Marks the event as sampled in Firestore to prevent duplicate sampling.
     * Updates both the remote database and the local event object.
     *
     * @param currentEvent the event to mark as sampled
     */
    private void markEventAsSampled(Event currentEvent) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .update("sampled", true)
                .addOnSuccessListener(aVoid -> {
                    // Update local state if needed
                    currentEvent.setSampled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error updating event status", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Callback interface for handling the completion of sampling operations.
     * Provides notification when the sampling process finishes, whether successfully or with errors.
     */
    public interface SamplingCallback {
        void onComplete(Exception error);
    }
}

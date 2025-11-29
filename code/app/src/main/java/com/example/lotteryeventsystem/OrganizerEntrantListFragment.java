package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotteryeventsystem.data.FirebaseWaitlistRepository;
import com.example.lotteryeventsystem.data.NotificationRepository;
import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Fragment that displays event details and provides navigation to different entrant lists
 * for lottery event organizers. This fragment shows event information and allows organizers
 * to view enrolled, canceled, and unenrolled entrants for a specific event.
 *
 * @author Emily Lan
 * @version 1.1
 * @see EntrantListFragment
 * @see Event
 */
public class OrganizerEntrantListFragment extends Fragment {
    private Button btnViewEntrants, btnSample;
    private ImageButton btnViewMap, btnEdit, btnDownloadQR;
    private TextView eventName, eventDescription, eventStartTime, eventEndTime, eventDate, eventCriteria;
    private Event currentEvent;
    private ImageButton btnBack;
    private String eventId;
    private ImageView posterImageView;
    private FirebaseWaitlistRepository waitlistRepository;
    private Boolean isEventSampled = false;
    /**
     * Creates a new instance of OrganizerEntrantListFragment with the specified event ID.
     *
     * @param eventId the unique identifier of the event to display
     * @return a new instance of OrganizerEntrantListFragment
     */
    public static OrganizerEntrantListFragment newInstance(String eventId) {
        OrganizerEntrantListFragment fragment = new OrganizerEntrantListFragment();
        Bundle args = new Bundle();
        args.putString("EVENT_ID", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Initializes UI components and sets up event listeners for navigation buttons.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing previous state, or null if none
     * @return the View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_view_entrant_page, container, false);

        // Get event ID from arguments
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("EVENT_ID");
        }
        waitlistRepository = new FirebaseWaitlistRepository();
        eventName = view.findViewById(R.id.event_name);
        eventDescription = view.findViewById(R.id.event_description);
        eventStartTime = view.findViewById(R.id.event_start_time);
        eventEndTime = view.findViewById(R.id.event_end_time);
        startDate = view.findViewById(R.id.start_date);
        endDate = view.findViewById(R.id.end_date);
        eventCriteria = view.findViewById(R.id.event_criteria);
        btnViewEntrants = view.findViewById(R.id.btnViewEntrants);
        btnBack = view.findViewById(R.id.back_button);
        btnEdit = view.findViewById(R.id.editEvent);
        btnDownloadQR = view.findViewById(R.id.downloadQR);
        posterImageView = view.findViewById(R.id.poster);
        btnSample = view.findViewById(R.id.btnSample);
        btnViewMap = view.findViewById(R.id.btnViewMap);

        loadEventFromFirestore(eventId);
        btnViewMap = view.findViewById(R.id.btnViewMap);

        btnViewEntrants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("EVENT_ID", eventId);
                args.putString("LIST_TYPE", "waiting");
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_organizerEntrantListFragment_to_EntrantListFragment, args);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles back navigation by popping the fragment from the back stack.
             *
             * @param v the View that was clicked
             */
            @Override
            public void onClick(View v) {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            }
        });
        btnSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEventSampled) {
                    Toast.makeText(getContext(), "Entrants have already been sampled.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (currentEvent != null && currentEvent.getSampleSize() != null) {
                    selectRandomSample();
                } else {
                    Toast.makeText(getContext(), "No sample size defined for this event", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentEvent != null && currentEvent.getDate() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate eventDate = LocalDate.parse(currentEvent.getDate(), formatter);
                        LocalDate currentDate = LocalDate.now();
                        if (currentDate.isAfter(eventDate)) {
                            Toast.makeText(getContext(), "Event date has passed", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                // Navigate to OrganizerEventCreateFragment in edit mode
                Bundle args = new Bundle();
                args.putString("EVENT_ID", eventId);
                args.putString("MODE", "edit");
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_organizerEntrantListFragment_to_organizerEventCreateFragment, args);
            }
        });
        btnViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EventMapFragment
                Bundle args = new Bundle();
                args.putString("EVENT_ID", eventId);
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_organizerEntrantListFragment_to_eventMapFragment, args);
            }
        });
        return view;
    }

    private void selectRandomSample() {
        int sampleSize = currentEvent.getSampleSize();

        // Get all waiting entrants
        waitlistRepository.getWaitingEntrants(eventId, new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onComplete(List<WaitlistEntry> result, Exception error) {
                if (error != null) {
                    Toast.makeText(getContext(), "Error loading entrants: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (result == null || result.isEmpty()) {
                    Toast.makeText(getContext(), "No waiting entrants found", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<WaitlistEntry> selectedEntrants;
                List<WaitlistEntry> notSelectedEntrants;
                if (result.size() < sampleSize) {
                    selectedEntrants = new ArrayList<>(result); // Select all
                    notSelectedEntrants = new ArrayList<>();
                    Toast.makeText(getContext(),
                            "Selected " + selectedEntrants.size() + " entrants and notified all applicants",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Select random sample
                    selectedEntrants = getRandomSample(result, sampleSize);
                    notSelectedEntrants = getNotSelectedEntrants(result, selectedEntrants);
                    Toast.makeText(getContext(),
                            "Selected " + selectedEntrants.size() + " entrants and notified all applicants",
                            Toast.LENGTH_SHORT).show();
                }
                    // Update selected entrants status to INVITED
                    updateEntrantsStatus(selectedEntrants, WaitlistStatus.INVITED);
                    // Send notifications
                    sendSelectedNotifications(selectedEntrants);
                    sendNotSelectedNotifications(notSelectedEntrants);
                    markEventAsSampled();
            }
        });
    }

    private void markEventAsSampled() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .update("sampled", true)
                .addOnSuccessListener(aVoid -> {
                    // Update local state
                    isEventSampled = true;
                    if (currentEvent != null) {
                        currentEvent.setSampled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating event status", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Get random sample from the list
     */
    private List<WaitlistEntry> getRandomSample(List<WaitlistEntry> allEntrants, int sampleSize) {
        List<WaitlistEntry> shuffled = new ArrayList<>(allEntrants);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, sampleSize);
    }

    /**
     * Get entrants who were not selected
     */
    private List<WaitlistEntry> getNotSelectedEntrants(List<WaitlistEntry> allEntrants, List<WaitlistEntry> selected) {
        List<WaitlistEntry> notSelected = new ArrayList<>(allEntrants);
        notSelected.removeAll(selected);
        return notSelected;
    }

    /**
     * Update status for a list of entrants
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
     * Send notifications to selected entrants
     */
    public void sendSelectedNotifications(List<WaitlistEntry> selectedEntrants) {
        for (WaitlistEntry entrant : selectedEntrants) {
            String userId = entrant.getId(); // Make sure this gets the actual user ID
            if (userId != null && !userId.isEmpty()) {
                NotificationsManager.sendSelected(requireContext(), eventId, userId);
            }
        }
    }

    /**
     * Send notifications to not selected entrants
     */
    private void sendNotSelectedNotifications(List<WaitlistEntry> notSelectedEntrants) {
        for (WaitlistEntry entrant : notSelectedEntrants) {
            String userId = entrant.getId();
            if (userId != null && !userId.isEmpty()) {
                NotificationsManager.sendNotSelected(requireContext(), eventId, userId);
            }
        }
    }

    /**
     * Loads event details from Firestore database using the provided event ID.
     *
     * @param eventId the unique identifier of the event to load
     */
    private void loadEventFromFirestore(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEvent = new Event();
                        if (documentSnapshot.contains("name")) {
                            currentEvent.setName(documentSnapshot.getString("name"));
                        }
                        if (documentSnapshot.contains("description")) {
                            currentEvent.setDescription(documentSnapshot.getString("description"));
                        }
                        if (documentSnapshot.contains("startDate")) {
                            currentEvent.setStartDate(documentSnapshot.getString("startDate"));
                        }
                        if (documentSnapshot.contains("endDate")) {
                            currentEvent.setEndDate(documentSnapshot.getString("endDate"));
                        }
                        if (documentSnapshot.contains("startTime")) {
                            currentEvent.setStartTime(documentSnapshot.getString("startTime"));
                        }
                        if (documentSnapshot.contains("endTime")) {
                            currentEvent.setEndTime(documentSnapshot.getString("endTime"));
                        }
                        if (documentSnapshot.contains("posterURL")) {
                            currentEvent.setPosterURL(documentSnapshot.getString("posterURL"));
                        }
                        if (documentSnapshot.contains("sampleSize")) {
                            currentEvent.setSampleSize(documentSnapshot.getLong("sampleSize").intValue());
                        }
                        if (documentSnapshot.contains("sampled")) {
                            currentEvent.setSampled(documentSnapshot.getBoolean("sampled"));
                            isEventSampled = Boolean.TRUE.equals(currentEvent.getSampled());
                        } else {
                            // If field doesn't exist, assume false
                            currentEvent.setSampled(false);
                            isEventSampled = false;
                        }
                        String criteria = "";
                        String tmp;
                        if ((tmp = documentSnapshot.getString("minAge")) != null) {
                            criteria += String.format("Min. Age: %s", tmp);
                        }
                        if ((tmp = documentSnapshot.getString("dietaryRestrictions")) != null) {
                            if (!criteria.isBlank()) {
                                criteria += " | ";
                            }
                            criteria += String.format("Dietary Restrictions: %s", tmp);
                        }
                        if ((tmp = documentSnapshot.getString("otherRestrictions")) != null) {
                            if (!criteria.isBlank()) {
                                criteria += " | ";
                            }
                            criteria += String.format("Other Restrictions: %s", tmp);
                        }
                        eventCriteria.setText(criteria);
                        displayEventInfo();
                    } else {
                        showError("Event not found: " + eventId);
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Error loading event: " + e.getMessage());
                });
    }

    /**
     * Displays the loaded event information in the corresponding TextViews.
     * Sets default values for any missing event information.
     */
    private void displayEventInfo() {
        if (currentEvent != null) {
            if (currentEvent.getName() != null) {
                eventName.setText(currentEvent.getName());
            } else {
                eventName.setText("No Title");
            }
            if (currentEvent.getDescription() != null) {
                eventDescription.setText(currentEvent.getDescription());
            } else {
                eventDescription.setText("No Description");
            }
            if (currentEvent.getStartTime() != null) {
                eventStartTime.setText("Start: " + currentEvent.getStartTime());
            } else {
                eventStartTime.setText("Start: Not specified");
            }
            if (currentEvent.getEndTime() != null) {
                eventEndTime.setText("End: " + currentEvent.getEndTime());
            } else {
                eventEndTime.setText("End: Not specified");
            }
            if (currentEvent.getStartDate() != null) {
                startDate.setText("Start Date: " + currentEvent.getStartDate());
            } else {
                startDate.setText("Start Date: Not specified");
            }
            if (currentEvent.getEndDate() != null) {
                endDate.setText("End Date: " + currentEvent.getEndDate());
            } else {
                endDate.setText("End Date: Not specified");
            }
            loadPosterImage();
        }
    }

    private void loadPosterImage() {
        if (currentEvent != null && currentEvent.getPosterURL() != null && !currentEvent.getPosterURL().isEmpty()) {
            loadImageWithPicasso(currentEvent.getPosterURL());
        } else {
            // Set the default placeholder when no poster exists
            posterImageView.setImageResource(R.drawable.qrcodeplaceholder);
        }
    }

    private void loadImageWithPicasso(String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.qrcodeplaceholder) // Show placeholder while loading
                .error(R.drawable.qrcodeplaceholder) // Show placeholder if loading fails
                .into(posterImageView);
    }

    /**
     * Displays an error message in the event information fields.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        eventName.setText("Error loading event");
        eventDescription.setText(message);
    }
}
package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.lotteryeventsystem.data.FirebaseWaitlistRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A Fragment that displays event details and provides navigation to different entrant lists
 * for lottery event organizers. This fragment shows event information and allows organizers
 * to view enrolled, canceled, and unenrolled entrants for a specific event.
 * Provides functionality for sampling entrants, editing events, viewing maps, and managing event details.
 *
 * @author Emily Lan
 * @version 1.2
 * @see EntrantListFragment
 * @see Event
 */
public class OrganizerEntrantListFragment extends Fragment {
    private Button btnViewEntrants, btnSample;
    private ImageButton btnViewMap, btnEdit, btnDownloadQR, btnBack;
    private TextView eventName, eventDescription, eventStartTime, eventEndTime, eventDate, eventCriteria, startDate, endDate;
    private Event currentEvent;
    private String eventId;
    private ImageView posterImageView;
    private FirebaseWaitlistRepository waitlistRepository;
    private Boolean isEventSampled = false;
    private SampleEntrantsManager sampleManager;

    /**
     * Creates a new instance of OrganizerEntrantListFragment with the specified event ID.
     *
     * @param eventId the unique identifier of the event to display
     * @return a new instance of OrganizerEntrantListFragment configured for the specified event
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
     * Initializes UI components, loads event data from Firestore, and sets up event listeners
     * for navigation and action buttons.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views
     * @param container          The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing previous state, or null if none
     * @return the View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_view_entrant_page, container, false);
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
        sampleManager = new SampleEntrantsManager(requireContext(), waitlistRepository, eventId);
        btnViewEntrants.setOnClickListener(new View.OnClickListener() {
            /**
             * Navigates to the EntrantListFragment to view waiting list entrants.
             *
             * @param v the View that was clicked
             */
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
            /**
             * Handles the sampling button click to select random entrants for the event.
             * Prevents multiple sampling operations and validates sample size availability.
             *
             * @param v the View that was clicked
             */
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
            /**
             * Handles edit button click to navigate to event editing fragment.
             * Validates that the event date hasn't passed before allowing edits.
             *
             * @param v the View that was clicked
             */
            @Override
            public void onClick(View v) {
                if (currentEvent != null && currentEvent.getEndDate() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate eventDate = LocalDate.parse(currentEvent.getEndDate(), formatter);
                    LocalDate currentDate = LocalDate.now();
                    if (currentDate.isAfter(eventDate)) {
                        Toast.makeText(getContext(), "Event date has passed", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Bundle args = new Bundle();
                args.putString("EVENT_ID", eventId);
                args.putString("MODE", "edit");
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_organizerEntrantListFragment_to_organizerEventCreateFragment, args);
            }
        });
        btnViewMap.setOnClickListener(new View.OnClickListener() {
            /**
             * Navigates to the EventMapFragment.
             *
             * @param v the View that was clicked
             */
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("EVENT_ID", eventId);
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_organizerEntrantListFragment_to_eventMapFragment, args);
            }
        });
        return view;
    }

    /**
     * Selects a random sample of entrants for the event using the specified sample size.
     * Updates the sampling status upon successful completion.
     */
    private void selectRandomSample() {
        int sampleSize = currentEvent.getSampleSize();
        sampleManager.selectRandomSample(sampleSize, currentEvent, new SampleEntrantsManager.SamplingCallback() {
            /**
             * Callback method invoked when the sampling operation completes.
             * Updates the sampling status if the operation was successful.
             *
             * @param error The exception that occurred during sampling, or null if successful
             */
            @Override
            public void onComplete(Exception error) {
                if (error == null) {
                    // Refresh UI if needed
                    isEventSampled = true;
                }
            }
        });
    }

    /**
     * Loads event details from Firestore database using the provided event ID.
     * Populates the currentEvent object with data retrieved from Firestore.
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
     * Sets default values for any missing event information and loads the event poster image.
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
                eventStartTime.setText("Start Time: " + currentEvent.getStartTime());
            } else {
                eventStartTime.setText("Start Time: Not specified");
            }
            if (currentEvent.getEndTime() != null) {
                eventEndTime.setText("End Time: " + currentEvent.getEndTime());
            } else {
                eventEndTime.setText("End Time: Not specified");
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

    /**
     * Loads and displays the event poster image from the URL stored in currentEvent.
     * Uses a default placeholder image if no poster URL is available or if loading fails.
     */
    private void loadPosterImage() {
        if (currentEvent != null && currentEvent.getPosterURL() != null && !currentEvent.getPosterURL().isEmpty()) {
            loadImageWithPicasso(currentEvent.getPosterURL());
        } else {
            posterImageView.setImageResource(R.drawable.qrcodeplaceholder);
        }
    }

    /**
     * Loads an image from the specified URL using Picasso library.
     * Displays placeholder images during loading and on error.
     *
     * @param imageUrl the URL of the image to load
     */
    private void loadImageWithPicasso(String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.qrcodeplaceholder) // Show placeholder while loading
                .error(R.drawable.qrcodeplaceholder) // Show placeholder if loading fails
                .into(posterImageView);
    }

    /**
     * Displays an error message in the event information fields.
     * Used when event loading fails or event data is unavailable.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        eventName.setText("Error loading event");
        eventDescription.setText(message);
    }
}
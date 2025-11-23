package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A Fragment that displays event details and provides navigation to different entrant lists
 * for lottery event organizers. This fragment shows event information and allows organizers
 * to view enrolled, canceled, and unenrolled entrants for a specific event.
 *
 * @author Emily Lan
 * @version 1.0
 * @see EntrantListFragment
 * @see Event
 */
public class OrganizerEntrantListFragment extends Fragment {
    private Button btnViewEntrants, btnEdit, btnDownloadQR;
    private TextView eventName, eventDescription, eventStartTime, eventEndTime, eventDate;
    private Event currentEvent;
    private ImageButton btnBack;
    private String eventId;

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
        eventName = view.findViewById(R.id.event_name);
        eventDescription = view.findViewById(R.id.event_description);
        eventStartTime = view.findViewById(R.id.event_start_time);
        eventEndTime = view.findViewById(R.id.event_end_time);
        eventDate = view.findViewById(R.id.event_date);
        btnViewEntrants = view.findViewById(R.id.btnViewEntrants);
        btnBack = view.findViewById(R.id.back_button);
        btnEdit = view.findViewById(R.id.editEvent);
        btnDownloadQR = view.findViewById(R.id.downloadQR);
        loadEventFromFirestore(eventId);

        btnViewEntrants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
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
        return view;
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
                        if (documentSnapshot.contains("date")) {
                            currentEvent.setDate(documentSnapshot.getString("date"));
                        }
                        if (documentSnapshot.contains("startTime")) {
                            currentEvent.setStartTime(documentSnapshot.getString("startTime"));
                        }
                        if (documentSnapshot.contains("endTime")) {
                            currentEvent.setEndTime(documentSnapshot.getString("endTime"));
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
            if (currentEvent.getDate() != null) {
                eventDate.setText("Date: " + currentEvent.getDate());
            } else {
                eventDate.setText("Date: Not specified");
            }
        }
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
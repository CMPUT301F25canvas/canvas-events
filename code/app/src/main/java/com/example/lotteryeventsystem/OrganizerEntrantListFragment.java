package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.lotteryeventsystem.data.NotificationRepository;
import com.example.lotteryeventsystem.data.WaitlistRepository;
import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

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
    private Button btnCanceled, btnEnrolled, btnCancelEntrant, btnEdit, btnDownloadQR;
    private Button btnNotifySelected;
    private TextView eventName, eventDescription, eventStartTime, eventEndTime, eventDate;
    private TextView notifyHint;
    private EditText notifyMessageInput;
    private ProgressBar notifyProgress;
    private Event currentEvent;
    private ImageButton btnBack;
    private String eventId;
    private String eventTitle = "";

    private final WaitlistRepository waitlistRepository = ServiceLocator.provideWaitlistRepository();
    private final NotificationRepository notificationRepository = ServiceLocator.provideNotificationRepository();

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
        btnCanceled = view.findViewById(R.id.btnCanceled);
        btnEnrolled = view.findViewById(R.id.btnEnrolled);
        btnCancelEntrant = view.findViewById(R.id.btnCancelEntrant);
        btnBack = view.findViewById(R.id.back_button);
        btnEdit = view.findViewById(R.id.editEvent);
        btnDownloadQR = view.findViewById(R.id.downloadQR);
        btnNotifySelected = view.findViewById(R.id.btnNotifySelected);
        notifyMessageInput = view.findViewById(R.id.notify_message_input);
        notifyProgress = view.findViewById(R.id.notify_progress);
        notifyHint = view.findViewById(R.id.notify_hint);

        btnNotifySelected.setOnClickListener(v -> sendSelectedEntrantNotification());
        loadEventFromFirestore(eventId);

        btnCanceled.setOnClickListener(new View.OnClickListener() {
            /**
             * Navigates to the canceled entrants list fragment.
             *
             * @param v the View that was clicked
             */
            @Override
            public void onClick(View v) {
                EntrantListFragment fragment = EntrantListFragment.newInstance(eventId, "canceled");
                replaceFragment(fragment);
            }
        });

        btnCancelEntrant.setOnClickListener(new View.OnClickListener() {
            /**
             * Navigates to the unenrolled entrants list fragment.
             *
             * @param v the View that was clicked
             */
            @Override
            public void onClick(View v) {
                EntrantListFragment fragment = EntrantListFragment.newInstance(eventId, "unenrolled");
                replaceFragment(fragment);
            }
        });

        btnEnrolled.setOnClickListener(new View.OnClickListener() {
            /**
             * Navigates to the enrolled entrants list fragment.
             *
             * @param v the View that was clicked
             */
            @Override
            public void onClick(View v) {
                EntrantListFragment fragment = EntrantListFragment.newInstance(eventId, "enrolled");
                replaceFragment(fragment);
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
     * Sends a broadcast message to every entrant currently marked as INVITED
     * for this event. This satisfies US 02.07.02 (notify all selected entrants).
     */
    private void sendSelectedEntrantNotification() {
        if (eventId == null || eventId.isEmpty()) {
            showToastMessage(getString(R.string.notify_missing_event));
            return;
        }
        String message = notifyMessageInput.getText().toString().trim();
        if (message.isEmpty()) {
            showToastMessage(getString(R.string.notify_message_required));
            return;
        }
        setNotifyLoading(true);
        waitlistRepository.getEntrantsByStatus(eventId,
                Collections.singletonList(WaitlistStatus.INVITED),
                (entries, error) -> {
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> handleSelectedEntrantsLoaded(entries, error, message));
                });
    }

    private void handleSelectedEntrantsLoaded(List<WaitlistEntry> entries, Exception error, String message) {
        if (error != null) {
            setNotifyLoading(false);
            showToastMessage(getString(R.string.notify_selected_error));
            return;
        }
        if (entries == null || entries.isEmpty()) {
            setNotifyLoading(false);
            showToastMessage(getString(R.string.notify_selected_empty));
            return;
        }
        String title = getString(R.string.notify_selected_title, safeEventName());
        notificationRepository.sendNotificationsToEntrants(
                eventId,
                safeEventName(),
                title,
                message,
                "INVITE",
                entries,
                (count, sendError) -> {
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        setNotifyLoading(false);
                        if (sendError != null || count == null) {
                            showToastMessage(getString(R.string.notify_selected_error));
                            return;
                        }
                        notifyMessageInput.setText("");
                        showToastMessage(getString(R.string.notify_selected_success, count));
                    });
                });
    }

    private String safeEventName() {
        if (currentEvent != null && currentEvent.getName() != null && !currentEvent.getName().isEmpty()) {
            return currentEvent.getName();
        }
        if (eventTitle != null && !eventTitle.isEmpty()) {
            return eventTitle;
        }
        return getString(R.string.event_detail_name_fallback);
    }

    private void setNotifyLoading(boolean loading) {
        if (notifyProgress != null) {
            notifyProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnNotifySelected != null) {
            btnNotifySelected.setEnabled(!loading);
        }
    }

    private void showToastMessage(String message) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Replaces the current fragment with the specified fragment.
     *
     * @param fragment the fragment to display
     */
    private void replaceFragment(Fragment fragment) {
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
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
                eventTitle = currentEvent.getName();
            }
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
                eventStartTime.setText(currentEvent.getStartTime());
            } else {
                eventStartTime.setText("Start: Not specified");
            }
            if (currentEvent.getEndTime() != null) {
                eventEndTime.setText(currentEvent.getEndTime());
            } else {
                eventEndTime.setText("End: Not specified");
            }
            if (currentEvent.getDate() != null) {
                eventDate.setText(currentEvent.getDate());
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

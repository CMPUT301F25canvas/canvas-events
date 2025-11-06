package com.example.lotteryeventsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerEntrantListFragment extends Fragment {
    private Button btnCanceled, btnEnrolled, btnCancelEntrant, btnEdit, btnDownloadQR;
    private TextView eventName, eventDescription, eventStartTime, eventEndTime, eventDate;
    private Event currentEvent;
    private ImageButton btnBack;
    private String eventId;

    // Factory method to create fragment with event ID
    public static OrganizerEntrantListFragment newInstance(String eventId) {
        OrganizerEntrantListFragment fragment = new OrganizerEntrantListFragment();
        Bundle args = new Bundle();
        args.putString("EVENT_ID", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.organizer_view_entrant_page, container, false);

        // Get event ID from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("EVENT_ID");
        }

        // Initialize views using the inflated view
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

        // Load event data from Firestore
        loadEventFromFirestore(eventId);

        // Set up click listeners
        btnCanceled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace with EntrantListFragment for canceled entrants using fragment transaction
                EntrantListFragment fragment = EntrantListFragment.newInstance(eventId, "canceled");
                replaceFragment(fragment);
            }
        });

        btnCancelEntrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace with EntrantListFragment for unenrolled entrants using fragment transaction
                EntrantListFragment fragment = EntrantListFragment.newInstance(eventId, "unenrolled");
                replaceFragment(fragment);
            }
        });

        btnEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace with EntrantListFragment for enrolled entrants using fragment transaction
                EntrantListFragment fragment = EntrantListFragment.newInstance(eventId, "enrolled");
                replaceFragment(fragment);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to previous fragment using fragment manager
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        // Replace the current fragment with a new one using fragment transaction
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, fragment); // Replace with your container ID
            transaction.addToBackStack(null); // Add to back stack so back button works
            transaction.commit();
        }
    }

    private void loadEventFromFirestore(String eventId) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Manual mapping to handle field name differences
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
                        if (documentSnapshot.contains("start_time")) {
                            currentEvent.setStartTime(documentSnapshot.getString("start_time"));
                        }
                        if (documentSnapshot.contains("end_time")) {
                            currentEvent.setEndTime(documentSnapshot.getString("end_time"));
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

    private void displayEventInfo() {
        if (currentEvent != null) {
            // Set event name
            if (currentEvent.getName() != null) {
                eventName.setText(currentEvent.getName());
            } else {
                eventName.setText("No Title");
            }

            // Set event description
            if (currentEvent.getDescription() != null) {
                eventDescription.setText(currentEvent.getDescription());
            } else {
                eventDescription.setText("No Description");
            }

            // Set start time
            if (currentEvent.getStartTime() != null) {
                eventStartTime.setText(currentEvent.getStartTime());
            } else {
                eventStartTime.setText("Start: Not specified");
            }

            // Set end time
            if (currentEvent.getEndTime() != null) {
                eventEndTime.setText(currentEvent.getEndTime());
            } else {
                eventEndTime.setText("End: Not specified");
            }

            // Set date
            if (currentEvent.getDate() != null) {
                eventDate.setText(currentEvent.getDate());
            } else {
                eventDate.setText("Date: Not specified");
            }
        }
    }

    private void showError(String message) {
        eventName.setText("Error loading event");
        eventDescription.setText(message);
    }
}
package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.media.Image;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows details for a single event after scanning a QR code.
 */
public class EventDetailFragment extends Fragment {
    public EventDetailFragment() {};

    private String eventId;
    private Button joinLeaveButton;
    private String deviceId;
    private TextView message;

    public static final String ARG_EVENT_ID = "event_id";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ImageButton backButton = view.findViewById(R.id.back_button);
        message = view.findViewById(R.id.message);

        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp(); // Navigates back to the previous fragment
        });

        if (getArguments() != null) {
            Toast.makeText(getContext(), "Event ID not found.", Toast.LENGTH_SHORT).show();
            eventId = getArguments().getString(ARG_EVENT_ID); // Grab the event ID passed in
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        updateAvailableSpotsMessage(db);

        eventId = getArguments().getString("event_id");
        deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        joinLeaveButton = view.findViewById(R.id.join_leave_button);
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        ((TextView) view.findViewById(R.id.header_title)).setText(doc.getString("name"));
                        ((TextView) view.findViewById(R.id.event_description)).setText(doc.getString("description"));
                        ((TextView) view.findViewById(R.id.event_date)).setText(doc.getString("date"));
                        ((TextView) view.findViewById(R.id.event_start_time)).setText(doc.getString("start_time"));
                        ((TextView) view.findViewById(R.id.event_end_time)).setText(doc.getString("end_time"));
                    }
                });
        if (((MainActivity) requireActivity()).getAdmin()) {
            setupDeleteEventButton(db);
        } else {
            setupJoinLeaveButton(db);
        }
    }

    /**
     * Sets up the "Join/Leave Waiting List" button for an event. If the user is in admin mode, the button is set up as a "Delete event"
     * Checks if the current device/user is already on the event's waitlist in Firestore:
     *  <ul>
     *      <li>If the device is on the waitlist, sets the button text to "Leave Waiting List".</li>
     *      <li>If not, sets the button text to "Join Waiting List".</li>
     *  </ul>
     *
     * @param db the FirebaseFirestore instance used to access the event waitlist collection
     */
    private void setupJoinLeaveButton(FirebaseFirestore db) {
        DocumentReference waitlistRef = db.collection("events").document(eventId).collection("waitlist").document(deviceId);
        DocumentReference userRef = db.collection("users").document(deviceId);

        userRef.get().addOnSuccessListener( documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                List<String> enrolledEvents = Arrays.asList(eventId);
                List<String> organizedEvents = new ArrayList<>();

                Map<String, Object> userData = new HashMap<>();
                userData.put("enrolled_events", enrolledEvents);
                userData.put("organized_events", organizedEvents);

                userRef.set(userData);
            }
        });

        waitlistRef.get().addOnSuccessListener( documentSnapshot -> {
            if (documentSnapshot.exists()) {
                joinLeaveButton.setText("Leave Waiting List");
            } else {
                joinLeaveButton.setText("Join Waiting List");
            }
        });

        joinLeaveButton.setOnClickListener(v -> {
            DocumentReference eventRef = db.collection("events").document(eventId);

            eventRef.get().addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) return;

                Object limitObj = documentSnapshot.get("entrantLimit");
                long entrantLimit;

                if (limitObj == null) {
                    entrantLimit = Long.MAX_VALUE; // no limit
                } else if (limitObj instanceof Number) {
                    entrantLimit = ((Number) limitObj).longValue();
                } else {
                    // assume it's a string containing a number
                    try {
                        entrantLimit = Long.parseLong(limitObj.toString());
                    } catch (NumberFormatException e) {
                        entrantLimit = Long.MAX_VALUE; // fallback
                    }
                }

                // Count how many users are currently in the waitlist
                Long finalEntrantLimit = entrantLimit;
                eventRef.collection("waitlist").get().addOnSuccessListener(waitlistSnapshot -> {
                    int currentEnrollment = waitlistSnapshot.size();


                    if (joinLeaveButton.getText().toString().equals("Join Waiting List")) {
                        if (currentEnrollment >= finalEntrantLimit) {
                            // Event full
                            message.setText("EVENT FULL");
                        } else {
                            // Add to waitlist
                            Map<String, Object> eventData = new HashMap<>();
                            eventData.put("status", "WAITING");
                            waitlistRef.set(eventData).addOnSuccessListener(aVoid -> {
                                joinLeaveButton.setText("Leave Waiting List");
                                Toast.makeText(getContext(), "You were added to the waiting list!", Toast.LENGTH_SHORT).show();
                                NotificationsManager.sendJoinedWaitlist(getContext(), eventId, userRef.getId());
                                updateAvailableSpotsMessage(db);
                            });

                            // Update user's enrolled_events array
                            userRef.update("enrolled_events", FieldValue.arrayUnion(eventId));
                        }
                    } else {
                        // Leave the waitlist
                        waitlistRef.delete().addOnSuccessListener(aVoid -> joinLeaveButton.setText("Join Waiting List"));
                        userRef.update("enrolled_events", FieldValue.arrayRemove(eventId));
                        Toast.makeText(getContext(), "You were removed from the waiting list!", Toast.LENGTH_SHORT).show();
                        updateAvailableSpotsMessage(db);
                    }
                });
            });
        });
    }

    private void setupDeleteEventButton(FirebaseFirestore db) {
        joinLeaveButton.setText("Delete Event");
        joinLeaveButton.setOnClickListener(v -> {
            FireBaseDeleteFuncs.deleteEvent(db, eventId, requireContext());

        });
    }

    private void updateAvailableSpotsMessage(FirebaseFirestore db) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) return;

            Object limitObj = documentSnapshot.get("entrantLimit");
            long entrantLimit;

            if (limitObj == null) {
                entrantLimit = Long.MAX_VALUE; // no limit
            } else if (limitObj instanceof Number) {
                entrantLimit = ((Number) limitObj).longValue();
            } else {
                try {
                    entrantLimit = Long.parseLong(limitObj.toString());
                } catch (NumberFormatException e) {
                    entrantLimit = Long.MAX_VALUE; // fallback
                }
            }

            long finalEntrantLimit = entrantLimit;
            eventRef.collection("waitlist").get().addOnSuccessListener(waitlistSnapshot -> {
                int currentEnrollment = waitlistSnapshot.size();
                long availableSpots = finalEntrantLimit - currentEnrollment;

                if (availableSpots > 0 && finalEntrantLimit != Long.MAX_VALUE) {
                    message.setText("Available spots: " + availableSpots);
                    joinLeaveButton.setEnabled(true);
                    joinLeaveButton.setAlpha(1.0f);
                } else if (finalEntrantLimit == Long.MAX_VALUE) {
                    message.setText("Unlimited spots available");
                    joinLeaveButton.setEnabled(true);
                    joinLeaveButton.setAlpha(1.0f);
                } else {
                    message.setText("The event is full...");
                    joinLeaveButton.setEnabled(false);
                    joinLeaveButton.setAlpha(0.5f);
                }
            });
        });
    }
}




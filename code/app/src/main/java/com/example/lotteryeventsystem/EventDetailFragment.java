package com.example.lotteryeventsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.Event;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.bumptech.glide.Glide;

/**
 * Shows details for a single event after scanning a QR code.
 */
public class EventDetailFragment extends Fragment {
    public EventDetailFragment() {};

    private String eventId;
    private Button joinLeaveButton;
    private String deviceId;
    private TextView message;

    // Temporary variables to allow the locatinoPermissionLauncher to access values
    private DocumentReference pendingWaitlistRef;
    private DocumentReference pendingUserRef;
    private FirebaseFirestore pendingDb;

    public static final String ARG_EVENT_ID = "event_id";

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                handleLocationPermissionResult(result);
            });

    private boolean hasLocationPermission() {
        return requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
    }

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
            String raweventId = getArguments().getString(ARG_EVENT_ID); // Grab the event ID passed in

            if (raweventId != null && raweventId.startsWith("app://")) {
                Uri uri = Uri.parse(raweventId);
                eventId = uri.getQueryParameter("eventID");
            } else {
                eventId = raweventId;
            }


        }

        deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        joinLeaveButton = view.findViewById(R.id.join_leave_button);

        loadEventRequirementsAndSetup();

        FirebaseFirestore db = FirebaseFirestore.getInstance();


    }

    private void loadEventRequirementsAndSetup() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        eventId = getArguments().getString("event_id");

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    View view = getView();
                    if (view == null) return;

                    LocalDate parsed = parseDate(doc.getString("endDate"));
                    String endDate = parsed != null
                            ? parsed.format(DateTimeFormatter.ofPattern("MMM dd"))
                            : "";

                    parsed = parseDate(doc.getString("startDate"));
                    String startDate = parsed != null
                            ? parsed.format(DateTimeFormatter.ofPattern("MMM dd"))
                            : "";


                    ((TextView) view.findViewById(R.id.header_title)).setText(doc.getString("name"));
                    ((TextView) view.findViewById(R.id.event_description)).setText(doc.getString("description"));
                    ((TextView) view.findViewById(R.id.event_start_date)).setText(startDate);
                    ((TextView) view.findViewById(R.id.event_end_date)).setText(endDate);
                    ((TextView) view.findViewById(R.id.event_start_time)).setText(doc.getString("startTime"));
                    ((TextView) view.findViewById(R.id.event_end_time)).setText(doc.getString("endTime"));

                    // Criteria text
                    String criteria = "";
                    String tmp;
                    if ((tmp = doc.getString("dietaryRestrictions")) != null) {
                        criteria += "Dietary Restrictions: " + tmp;
                    }
                    if ((tmp = doc.getString("otherRestrictions")) != null) {
                        if (!criteria.isBlank()) criteria += " | ";
                        criteria += "Other Restrictions: " + tmp;
                    }
                    ((TextView) view.findViewById(R.id.event_criteria)).setText(criteria);

                    // Poster image
                    String posterUrl = doc.getString("posterURL");
                    ImageView posterImage = view.findViewById(R.id.event_poster);
                    if (!TextUtils.isEmpty(posterUrl)) {
                        Glide.with(requireContext())
                                .load(posterUrl)
                                .placeholder(R.drawable.qrcodeplaceholder)
                                .error(R.drawable.qrcodeplaceholder)
                                .into(posterImage);
                    }

                    if (((MainActivity) requireActivity()).getAdmin()) {
                        setupDeleteEventButton(db);
                    } else {
                        setupJoinLeaveButton(db);
                    }

                    updateAvailableSpotsMessage(db);
                });
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
                // Debug: surface which device id is being used for waitlist writes.
                Log.d("EventDetailFragment", "Join/Leave using deviceId=" + deviceId + " for eventId=" + eventId);
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

                            // Check if location is required to join the event
                            if (documentSnapshot.getBoolean("geolocationRequirement")) {
                                checkLocationPermission(waitlistRef, userRef, db);
                                return;
                            } else { // Join waitlist normally
                                addToWaitlist(new HashMap<>(), waitlistRef, userRef, db);
                            }
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

    /**
     * Refactored joining the waitlist - to allow the Geolocation Requirement to function
     * @param waitlistRef
     * @param userRef
     * @param db
     */
    private void addToWaitlist(Map<String, Object> eventData, DocumentReference waitlistRef, DocumentReference userRef, FirebaseFirestore db) {
        // Add to waitlist
        eventData.put("status", "WAITING");
        waitlistRef.set(eventData).addOnSuccessListener(aVoid -> {
            joinLeaveButton.setText("Leave Waiting List");
            NotificationsManager.sendJoinedWaitlist(getContext(), eventId, userRef.getId());
            updateAvailableSpotsMessage(db);
        });

        // Update user's enrolled_events array
        userRef.update("enrolled_events", FieldValue.arrayUnion(eventId));
    }

    /**
     *
     * @param waitlistRef
     * @param userRef
     * @param db
     */
    private void checkLocationPermission(DocumentReference waitlistRef, DocumentReference userRef, FirebaseFirestore db) {
        if (hasLocationPermission()) {
            jointWaitlistGeolocationApproved(waitlistRef, userRef, db);
            return;
        } else {
            pendingWaitlistRef = waitlistRef;
            pendingUserRef = userRef;
            pendingDb = db;

            locationPermissionLauncher.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void handleLocationPermissionResult(Map<String, Boolean> result) {
        boolean fineLocationGranted = Boolean.TRUE.equals(
                result.get(Manifest.permission.ACCESS_FINE_LOCATION));

        if (!fineLocationGranted) {
            message.setText("Location permission required to join this event.");
            return;
        }

        // Has permission, join the waitlist
        jointWaitlistGeolocationApproved(pendingWaitlistRef, pendingUserRef, pendingDb);
    }

    private void jointWaitlistGeolocationApproved(DocumentReference waitlistRef, DocumentReference userRef, FirebaseFirestore db) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        // This line is already correct because it uses the full class path.
        // It creates an instance of the NEW LocationRequest.
        CurrentLocationRequest locationRequest = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        // The fusedLocationClient.getCurrentLocation() method expects the new LocationRequest,
        // which it now correctly receives.
        fusedLocationClient.getCurrentLocation(locationRequest, null).addOnSuccessListener(location -> {
            // Check if location is null
            if (location == null) {
                message.setText("Unable to retrieve location. Please try again.");
                return;
            }

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("latitude", location.getLatitude());
            eventData.put("longitude", location.getLongitude());
            addToWaitlist(eventData, waitlistRef, userRef, db);
        });
    }

    private String formatRange(String raw) {
        String[] patterns = {"yyyy-MM-dd", "MMM dd, yyyy", "MMM dd yyyy"};
        for (String pattern : patterns) {
            try {
                LocalDate date = LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
                return date.format(DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH));
            } catch (DateTimeParseException ignored) {
            }
        }
        return raw;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String[] patterns = {"yyyy-MM-dd", "MM/dd/yyyy", "MMM dd, yyyy", "MMM dd yyyy"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }



}



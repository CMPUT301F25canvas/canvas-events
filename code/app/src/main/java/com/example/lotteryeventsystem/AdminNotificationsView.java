package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin view for displaying all notifications grouped by event.
 * Each event expands into all 6 possible notification types, and each type expands
 * into the list of users who received that notification, along with the timestamp.
 */
public class AdminNotificationsView extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout eventsContainer;

    /** Mapping Firestore notification document names → readable titles */
    private static final Map<String, String> NOTIF_NAMES = new HashMap<String, String>() {{
        put("invite_accepted_notification", "Invite Accepted");
        put("invite_cancelled_notification", "Invite Cancelled");
        put("invite_rejected_notification", "Invite Rejected");
        put("joined_waitlist_notification", "Joined Waitlist");
        put("not_selected_notification", "Not Selected");
        put("selected_notification", "Selected");
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_notifications, parent, false);
        if (!((MainActivity) requireActivity()).getAdmin()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_adminNotificationsFragment_to_notificationFragment);
        }
        return view;
    }

    /**
     * Ensures user is admin and initializes Firestore + UI.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        eventsContainer = v.findViewById(R.id.events_container);

        loadEvents();
    }

    /**
     * Loads all events from Firestore and creates an expandable card for each event.
     */
    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(events -> {
                    eventsContainer.removeAllViews();

                    for (QueryDocumentSnapshot doc : events) {
                        String eventId = doc.getId();
                        String eventName = doc.getString("name");
                        addEventCard(eventId, eventName);
                    }
                });
    }

    /**
     * Creates a single event card with expandable notification types.
     *
     * @param eventId The Firestore ID of the event
     * @param eventName The human-readable event name
     */
    private void addEventCard(String eventId, String eventName) {

        View card = getLayoutInflater().inflate(R.layout.item_admin_event, eventsContainer, false);

        TextView title = card.findViewById(R.id.event_name);
        View header = card.findViewById(R.id.event_header);
        View arrow = card.findViewById(R.id.expand_icon);
        LinearLayout notifContainer = card.findViewById(R.id.notifications_container);

        title.setText(eventName);
        notifContainer.setVisibility(View.GONE);

        final boolean[] loaded = {false};

        // Expand/collapse for event-level
        header.setOnClickListener(v -> {
            if (notifContainer.getVisibility() == View.GONE) {
                notifContainer.setVisibility(View.VISIBLE);
                arrow.setRotation(180);

                if (!loaded[0]) {
                    loadNotificationTypes(eventId, notifContainer);
                    loaded[0] = true;
                }
            } else {
                notifContainer.setVisibility(View.GONE);
                arrow.setRotation(0);
            }
        });

        eventsContainer.addView(card);
    }

    /**
     * Loads the 6 notification types for an event.
     *
     * @param eventId The ID of the event
     * @param container The layout where types will be added
     */
    private void loadNotificationTypes(String eventId, LinearLayout container) {
        container.removeAllViews();

        for (Map.Entry<String, String> entry : NOTIF_NAMES.entrySet()) {
            addNotifTypeRow(eventId, entry.getKey(), entry.getValue(), container);
        }
    }

    /**
     * Creates a row for a single notification type.
     * Each row itself is expandable to show the user list.
     */
    private void addNotifTypeRow(String eventId, String typeKey, String label, LinearLayout parent) {

        View row = getLayoutInflater().inflate(R.layout.item_notification_type, parent, false);

        TextView title = row.findViewById(R.id.notification_type_name);
        TextView count = row.findViewById(R.id.user_count);
        View header = row.findViewById(R.id.type_header);
        View arrow = row.findViewById(R.id.dropdown_icon);
        LinearLayout usersContainer = row.findViewById(R.id.users_container);

        title.setText(label);
        usersContainer.setVisibility(View.GONE);

        // Fetch user list for this notification type and event
        db.collection("notifications")
                .document(typeKey)
                .collection(eventId)
                .get()
                .addOnSuccessListener(snap -> {
                    count.setText(snap.size() + " users");

                    header.setOnClickListener(v -> {
                        if (usersContainer.getVisibility() == View.GONE) {
                            usersContainer.setVisibility(View.VISIBLE);
                            arrow.setRotation(180);
                            loadUsers(usersContainer, snap);
                        } else {
                            usersContainer.setVisibility(View.GONE);
                            arrow.setRotation(0);
                        }
                    });
                });

        parent.addView(row);
    }

    /**
     * Loads all user documents from a notification subcollection.
     */
    private void loadUsers(LinearLayout container, QuerySnapshot snap) {
        container.removeAllViews();
        for (QueryDocumentSnapshot doc : snap) {
            String userId = doc.getId();
            Timestamp ts = doc.getTimestamp("timestamp");
            addUserRow(container, userId, ts);
        }
    }

    /**
     * Creates a row displaying user name, email, and time the notification was received.
     *
     * @param ts Firestore timestamp when the notification was stored
     */
    private void addUserRow(LinearLayout parent, String userId, Timestamp ts) {

        View row = getLayoutInflater().inflate(R.layout.item_user, parent, false);

        TextView name = row.findViewById(R.id.user_name);
        TextView email = row.findViewById(R.id.user_email);
        TextView time = row.findViewById(R.id.time_received);

        // Load user profile info
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    String n = doc.getString("name");
                    String e = doc.getString("email");

                    name.setText(n != null ? n : userId);

                    if (e == null || e.isEmpty()) {
                        email.setVisibility(View.GONE);
                    } else {
                        email.setText(e);
                        email.setVisibility(View.VISIBLE);
                    }
                });

        // Show timestamp if available
        if (ts != null) {
            long diff = System.currentTimeMillis() - ts.toDate().getTime();
            time.setText(TimeAgo.formatTimeDiff(diff));
            time.setVisibility(View.VISIBLE);
        } else {
            time.setVisibility(View.GONE);
        }

        parent.addView(row);
    }
}

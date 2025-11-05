package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.Event;

import java.text.DateFormat;

/**
 * Shows details for a single event after scanning a QR code.
 */
public class EventDetailFragment extends Fragment {
    public static final String ARG_EVENT_ID = "eventId";

    private TextView titleView;
    private TextView descriptionView;
    private TextView locationView;
    private TextView registrationWindowView;
    private TextView capacityView;
    private View progressView;
    private View contentView;
    private TextView messageView;

    @Nullable
    private String eventId;

    public EventDetailFragment() {
    }
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class EventDetailFragment extends Fragment {
    public EventDetailFragment() {};

    private String eventId;
    private Button joinLeaveButton;
    private String deviceId;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleView = view.findViewById(R.id.event_title);
        descriptionView = view.findViewById(R.id.event_description);
        locationView = view.findViewById(R.id.event_location);
        registrationWindowView = view.findViewById(R.id.event_registration_window);
        capacityView = view.findViewById(R.id.event_capacity);
        progressView = view.findViewById(R.id.event_progress);
        contentView = view.findViewById(R.id.event_content);
        messageView = view.findViewById(R.id.event_message);

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
        loadEvent();
    }

    private void loadEvent() {
        if (eventId == null || eventId.isEmpty()) {
            showMessage(getString(R.string.event_detail_missing_id));
            return;
        }
        showLoading(true);
        ServiceLocator.provideEventRepository()
                .getEventById(eventId, (event, error) -> {
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        showLoading(false);
                        if (error != null) {
                            showMessage(getString(R.string.event_detail_error));
                            return;
                        }
                        if (event == null) {
                            showMessage(getString(R.string.event_detail_not_found));
                            return;
                        }
                        bindEvent(event);
                    });
                });
    }

    private void bindEvent(Event event) {
        contentView.setVisibility(View.VISIBLE);
        messageView.setVisibility(View.GONE);
        titleView.setText(orFallback(event.getTitle(), getString(R.string.event_detail_name_fallback)));
        descriptionView.setText(orFallback(event.getDescription(), getString(R.string.event_detail_description_fallback)));
        locationView.setText(orFallback(event.getLocation(), getString(R.string.event_detail_location_fallback)));
        registrationWindowView.setText(buildRegistrationWindow(event));
        String capacityText = event.friendlyCapacity();
        if (capacityText == null) {
            capacityView.setVisibility(View.GONE);
        } else {
            capacityView.setVisibility(View.VISIBLE);
            capacityView.setText(getString(R.string.event_detail_capacity_format, capacityText));
        }
    }

    private void showLoading(boolean loading) {
        progressView.setVisibility(loading ? View.VISIBLE : View.GONE);
        contentView.setVisibility(loading ? View.GONE : contentView.getVisibility());
    }

    private void showMessage(String message) {
        contentView.setVisibility(View.GONE);
        progressView.setVisibility(View.GONE);
        messageView.setVisibility(View.VISIBLE);
        messageView.setText(message);
    }

    private String buildRegistrationWindow(Event event) {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        String open = event.getRegistrationOpen() != null
                ? formatter.format(event.getRegistrationOpen().toDate())
                : getString(R.string.event_detail_unknown_time);
        String close = event.getRegistrationClose() != null
                ? formatter.format(event.getRegistrationClose().toDate())
                : getString(R.string.event_detail_unknown_time);
        return getString(R.string.event_detail_registration_window_format, open, close);
    }

    private String orFallback(@Nullable String value, String fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }
        return value;
    }
}

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp(); // Navigates back to the previous fragment
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        setupJoinLeaveButton(db);
    }

    private void setupJoinLeaveButton(FirebaseFirestore db) {
        DocumentReference attendeeRef = db.collection("events").document(eventId).collection("attendees").document(deviceId);

        attendeeRef.get().addOnSuccessListener( documentSnapshot -> {
            if (documentSnapshot.exists()) {
                joinLeaveButton.setText("Leave Waiting List");
            } else {
              joinLeaveButton.setText("Join Waiting List");
            }
        });

        joinLeaveButton.setOnClickListener(v -> {
            if (joinLeaveButton.getText().toString().equals("Join Waiting List")) {
                Map<String, Object> data = new HashMap<>();
                data.put("joined_at", FieldValue.serverTimestamp());

                attendeeRef.set(data).addOnSuccessListener(aVoid -> joinLeaveButton.setText("Leave Waiting List"));
                Toast.makeText(getContext(), "You where added to the waiting list!", Toast.LENGTH_SHORT).show();

            } else {
                attendeeRef.delete().addOnSuccessListener(aVoid -> joinLeaveButton.setText("Join Waiting List"));
                Toast.makeText(getContext(), "You where removed to the waiting list!", Toast.LENGTH_SHORT).show();

            }
        });

    }
}



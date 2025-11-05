package com.example.lotteryeventsystem;

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
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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



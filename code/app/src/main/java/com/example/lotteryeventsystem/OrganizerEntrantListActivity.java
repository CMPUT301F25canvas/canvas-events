package com.example.lotteryeventsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.example.lotteryeventsystem.Event;

public class OrganizerEntrantListActivity extends AppCompatActivity {
    private Button btnCanceled, btnEnrolled, btnNotify, btnCancelEntrant, btnReplace;
    private TextView eventName, eventDescription, eventStartTime, eventEndTime, eventDate;
    private Event currentEvent;
    private final String HARDCODED_EVENT_ID = "event_id3";
    private ImageButton btnBack;
    //private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_view_entrant_page);
        //eventId = getIntent().getStringExtra("EVENT_ID");

        eventName = findViewById(R.id.event_name);
        eventDescription = findViewById(R.id.event_description);
        eventStartTime = findViewById(R.id.event_start_time);
        eventEndTime = findViewById(R.id.event_end_time);
        eventDate = findViewById(R.id.event_date);

        // Get event data from intent
        currentEvent = (Event) getIntent().getSerializableExtra("EVENT_OBJECT");

        // If EVENT_OBJECT wasn't passed, try to get individual fields
        if (currentEvent == null) {
            currentEvent = new Event();
            //currentEvent.setId(getIntent().getStringExtra("EVENT_ID"));
            // You might need to load the event from Firestore here
        }

        loadEventFromFirestore(HARDCODED_EVENT_ID);

        btnCanceled = findViewById(R.id.btnCanceled);
        btnEnrolled = findViewById(R.id.btnEnrolled);
        btnCancelEntrant = findViewById(R.id.btnCancelEntrant);
        btnBack = findViewById(R.id.back_button);

        btnCanceled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEntrantListActivity.this, ListActivity.class);
                intent.putExtra("LIST_TYPE", "canceled");
                //intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            }
        });

        btnCancelEntrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEntrantListActivity.this, ListActivity.class);
                intent.putExtra("LIST_TYPE", "unenrolled");
                //intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            }
        });

        btnEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEntrantListActivity.this, ListActivity.class);
                intent.putExtra("LIST_TYPE", "enrolled");
                //intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this activity and return to MainActivity
                finish();
            }
        });
    }
    private void loadEventFromFirestore(String HARDCODED_EVENT_ID) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(HARDCODED_EVENT_ID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEvent = documentSnapshot.toObject(Event.class);
                        if (currentEvent != null) {
                            displayEventInfo();
                        } else {
                            showError("Failed to parse event data");
                        }
                    } else {
                        showError("Event not found: " + HARDCODED_EVENT_ID);
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

            if (currentEvent.getEndTime() != null) {
                eventEndTime.setText(currentEvent.getEndTime());
            } else {
                eventEndTime.setText("End: Not specified");
            }

            // Set end time
            if (currentEvent.getDate() != null) {
                eventDate.setText(currentEvent.getDate());
            } else {
                eventDate.setText("Date");
            }
        }
    }

    private void showError(String message) {
        // Display error in one of the TextViews
        eventName.setText("Error loading event");
        eventDescription.setText(message);
    }
}
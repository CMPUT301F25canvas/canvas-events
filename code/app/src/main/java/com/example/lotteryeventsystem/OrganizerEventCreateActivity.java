package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.w3c.dom.Text;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class OrganizerEventCreateActivity extends AppCompatActivity {
    String name;
    String description;
    String eventDate;
    String eventStart;
    String eventEnd;
    Integer entrantLimit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connecting all of the elements
        // Event Name
        TextInputEditText eventNameInput = findViewById(R.id.event_name_input);
        eventNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                name = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Event Description
        TextInputEditText eventDescriptionInput = findViewById(R.id.event_description_input);
        eventDescriptionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                description = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Event Date
        TextInputLayout eventDateLayout = findViewById(R.id.event_date_input_layout);
        TextInputEditText eventDateInput = findViewById(R.id.event_date_input_text);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        eventDateInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String inputText = s.toString().trim();

                // Error message to signify incorrect date format
                if (inputText.isEmpty()) {
                    eventDateLayout.setError(null);
                    return;
                }
                try {
                    eventDate = inputText;
                    // Clear any previous error if successful
                    eventDateLayout.setError(null);
                } catch (DateTimeParseException e) {
                    // Show error in the TextInputLayout
                    eventDateLayout.setError("Invalid Format - use YYYY-MM-DD");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ImageButton eventCalendarButton = findViewById(R.id.calendar_button);
        eventCalendarButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Event Date")
                    .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Convert the timestamp (UTC midnight) to LocalDate correctly
                ZoneId utcZone = ZoneId.of("UTC");
                Instant instant = Instant.ofEpochMilli(selection);
                LocalDate eventDate = instant.atZone(utcZone).toLocalDate();

                // Format as YYYY-MM-DD
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDate = eventDate.format(formatter);

                // Display and store
                eventDateInput.setText(formattedDate);
                this.eventDate = eventDate.toString(); // keep LocalDate for later use
            });
        });


        // Event Start Time
        TextInputLayout startTimeLayout = findViewById(R.id.start_time_input_layout);
        TextInputEditText startTimeInput = findViewById(R.id.start_time_input_text);

        ImageButton startTimeButton = findViewById(R.id.start_time_dropdown);
        startTimeButton.setOnClickListener(v -> {
            MaterialTimePicker timePicker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(12)
                            .setMinute(0)
                            .setTitleText("Select Event Start Time")
                            .build();

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                LocalTime eventTime = LocalTime.of(hour, minute);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
                String formattedTime = eventTime.format(timeFormatter);

                startTimeInput.setText(formattedTime);
                this.eventStart = eventTime.toString();
            });
        });


        // Event End Time
        TextInputLayout endTimeLayout = findViewById(R.id.end_time_input_layout);
        TextInputEditText endTimeInput = findViewById(R.id.end_time_input_text);

        ImageButton endTimeButton = findViewById(R.id.end_time_dropdown);

        endTimeButton.setOnClickListener(v -> {
            MaterialTimePicker timePicker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(12)
                            .setMinute(0)
                            .setTitleText("Select Event Start Time")
                            .build();

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                LocalTime eventTime = LocalTime.of(hour, minute);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
                String formattedTime = eventTime.format(timeFormatter);

                endTimeInput.setText(formattedTime);
                this.eventEnd = eventTime.toString();
            });
        });


        // Entrant Limit
        TextInputLayout entrantLimitLayout = findViewById(R.id.entrant_limit_input_layout);
        TextInputEditText entrantLimitInput = findViewById(R.id.entrant_limit_input_text);
        entrantLimitInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    entrantLimit = Integer.parseInt(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        CheckBox entrantLimitCheckBox = findViewById(R.id.entrant_limit_checkbox);
        entrantLimitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    entrantLimitLayout.setVisibility(VISIBLE);
                    entrantLimitInput.setVisibility(VISIBLE);

                } else {
                    entrantLimitLayout.setVisibility(INVISIBLE);
                    entrantLimitInput.setVisibility(INVISIBLE);
                }
            }
        });

        // Create Event Button
        Button createEventButton = findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if all required inputs are filled
                if (name != null && description != null && eventDate != null && eventStart != null && eventEnd != null) {
                    // Check if entrant limit is checked off
                    if (entrantLimit == null) {
                        Event newEvent = new Event(name, description, eventDate, eventStart, eventEnd);
                    } else {
                        Event newEvent = new Event(name, description, eventDate, eventStart, eventEnd, entrantLimit);
                    }

                    // TODO: store information into database
                    // TODO: generate new QR Code and store
                    // TODO: move to event details screen?
                } else {
                    // TODO: display error output message
                }
            }
        });
    }



}


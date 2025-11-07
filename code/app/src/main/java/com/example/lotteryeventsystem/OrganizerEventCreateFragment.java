package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class OrganizerEventCreateFragment extends Fragment {

    EventRepository eventRepository;

    String name;
    String description;
    String date;
    String eventStart;
    String eventEnd;
    String eventPosterURL;
    boolean geolocationRequirement;
    Integer entrantLimit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_create, container, false);
    }


    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventRepository = new EventRepository();

        // Connecting all of the elements
        // Back Button
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp(); // Navigates back to the previous fragment
        });

        // Event Name
        TextInputEditText eventNameInput = view.findViewById(R.id.event_name_input);
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
        TextInputEditText eventDescriptionInput = view.findViewById(R.id.event_description_input);
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
        TextInputLayout eventDateLayout = view.findViewById(R.id.event_date_input_layout);
        TextInputEditText eventDateInput = view.findViewById(R.id.event_date_input_text);
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
                    date = inputText;
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

        ImageButton eventCalendarButton = view.findViewById(R.id.calendar_button);
        eventCalendarButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Event Date")
                            .build();

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Convert the timestamp (UTC midnight) to LocalDate correctly
                ZoneId utcZone = ZoneId.of("UTC");
                Instant instant = Instant.ofEpochMilli(selection);
                LocalDate eventDate = instant.atZone(utcZone).toLocalDate();

                // Format as YYYY-MM-DD
                String formattedDate = eventDate.format(dateFormatter);

                // Display and store
                eventDateInput.setText(formattedDate);
                this.date = eventDate.toString(); // keep LocalDate for later use
            });
        });

        // Event Start Time
        TextInputLayout startTimeLayout = view.findViewById(R.id.start_time_input_layout);
        TextInputEditText startTimeInput = view.findViewById(R.id.start_time_input_text);

        ImageButton startTimeButton = view.findViewById(R.id.start_time_dropdown);
        startTimeButton.setOnClickListener(v -> {
            MaterialTimePicker timePicker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(12)
                            .setMinute(0)
                            .setTitleText("Select Event Start Time")
                            .build();

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");

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
        TextInputLayout endTimeLayout = view.findViewById(R.id.end_time_input_layout);
        TextInputEditText endTimeInput = view.findViewById(R.id.end_time_input_text);

        ImageButton endTimeButton = view.findViewById(R.id.end_time_dropdown);

        endTimeButton.setOnClickListener(v -> {
            MaterialTimePicker timePicker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(12)
                            .setMinute(0)
                            .setTitleText("Select Event Start Time")
                            .build();

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");

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

        // Event Poster
        // TODO: HOW TO UPLOAD EVENT POSTER
        // https://www.geeksforgeeks.org/android/android-how-to-upload-an-image-on-firebase-storage/

        // Geolocation Requirement
        CheckBox geolocationCheckBox = view.findViewById(R.id.geolocation_requirement_checkbox);
        geolocationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                geolocationRequirement = isChecked;
            }
        });

        // Entrant Limit
        TextInputLayout entrantLimitLayout = view.findViewById(R.id.entrant_limit_input_layout);
        TextInputEditText entrantLimitInput = view.findViewById(R.id.entrant_limit_input_text);
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

        CheckBox entrantLimitCheckBox = view.findViewById(R.id.entrant_limit_checkbox);
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
        Button createEventButton = view.findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if all required inputs are filled
                if (name != null && description != null && date != null && eventStart != null && eventEnd != null) {

                    String organizerID = Settings.Secure.getString(requireContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);

                    
                    eventRepository.generateEventID().addOnSuccessListener(snapshot -> {
                        long count = snapshot.getCount() + 1;
                        String eventID = "event_id" + count;

                        Event newEvent = new Event(eventID, organizerID, name, description, date, eventStart, eventEnd);

                        if (eventPosterURL != null) {
                            newEvent.setPosterURL(eventPosterURL);
                        }

                        newEvent.setGeolocationRequirement(geolocationRequirement);

                        if (entrantLimit != null) {
                            newEvent.setEntrantLimit(entrantLimit);
                        }

                        // Generate QR Code Bitmap
    //                    Bitmap qrBitmap = QRCodeGenerator.generateQRCode(eventID);

                        // Add event to firebase
                        eventRepository.addEvent(newEvent);

                        // Update the ListView

                        // Move to Event Detail Screen
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_organizerEventCreateFragment_to_organizerEventListFragment);
                    });


                } else {
                    Toast.makeText(getContext(), "Fill in the Required Fields", Toast.LENGTH_LONG).show();
                }
            }
        });





    }
}

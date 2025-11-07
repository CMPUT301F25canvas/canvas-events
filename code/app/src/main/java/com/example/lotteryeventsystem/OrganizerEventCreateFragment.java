package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;
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
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Fragment class for the OrganizerEventCreate screen.
 * Stores logic for the event creation form and creates a new Event if input is correct
 */
public class OrganizerEventCreateFragment extends Fragment {

    private OrganizerEventCreateViewModel viewModel;
    private EventRepository eventRepository;

    String posterURL; // temporary will change


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
        viewModel = new ViewModelProvider(this).get(OrganizerEventCreateViewModel.class);

        // Connecting all of the elements
        // Back Button
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp(); // Navigates back to the previous fragment
        });

        // Event Name
        TextInputLayout eventNameLayout = view.findViewById(R.id.event_name_input_layout);
        viewModel.getName().observe(getViewLifecycleOwner(), name -> {
            if (name != null && !name.isEmpty()) {
                eventNameLayout.setError(null);
            }
        });
        TextInputEditText eventNameInput = view.findViewById(R.id.event_name_input);
        setupTextWatcher(eventNameInput, text -> viewModel.setName(text));

        // Event Description
        TextInputLayout eventDescriptionLayout = view.findViewById(R.id.event_description_input_layout);
        viewModel.getDescription().observe(getViewLifecycleOwner(), description -> {
            if (description != null && !description.isEmpty()) {
                eventDescriptionLayout.setError(null);
            }
        });
        TextInputEditText eventDescriptionInput = view.findViewById(R.id.event_description_input);
        setupTextWatcher(eventDescriptionInput, text -> viewModel.setDescription(text));

        // Event Date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TextInputLayout eventDateLayout = view.findViewById(R.id.event_date_input_layout);
        viewModel.getDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null && !date.isEmpty()) {
                try {
                    LocalDate.parse(date, dateFormatter);
                    eventDateLayout.setError(null);
                } catch (DateTimeParseException e) {
                    eventDateLayout.setError("Invalid Format - use YYYY-MM-DD");
                }
            }
        });
        TextInputEditText eventDateInput = view.findViewById(R.id.event_date_input_text);
        setupTextWatcher(eventDateInput, text -> viewModel.setDate(text));

        ImageButton eventCalendarButton = view.findViewById(R.id.calendar_button);
        eventCalendarButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Event Date")
                            .build();

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                ZoneId utcZone = ZoneId.of("UTC");
                Instant instant = Instant.ofEpochMilli(selection);
                LocalDate eventDate = instant.atZone(utcZone).toLocalDate();
                String formattedDate = eventDate.format(dateFormatter);
                eventDateInput.setText(formattedDate);
            });
        });

        // Event Start Time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        TextInputLayout startTimeLayout = view.findViewById(R.id.start_time_input_layout);
        viewModel.getEventStart().observe(getViewLifecycleOwner(), time -> {
            if (time != null && !time.isEmpty()) {
                try {
                    LocalTime.parse(time, timeFormatter);
                    startTimeLayout.setError(null);
                } catch (DateTimeParseException e) {
                    startTimeLayout.setError("Invalid Format - use YYYY-MM-DD");
                }
            }
        });
        TextInputEditText startTimeInput = view.findViewById(R.id.start_time_input_text);
        setupTextWatcher(startTimeInput, text -> viewModel.setEventStart(text));

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
                String formattedTime = eventTime.format(timeFormatter);
                startTimeInput.setText(formattedTime);
            });
        });

        // Event End Time
        TextInputLayout endTimeLayout = view.findViewById(R.id.end_time_input_layout);
        viewModel.getEventEnd().observe(getViewLifecycleOwner(), time -> {
            if (time != null && !time.isEmpty()) {
                try {
                    LocalTime.parse(time, timeFormatter);
                    endTimeLayout.setError(null);
                } catch (DateTimeParseException e) {
                    endTimeLayout.setError("Invalid Format - use YYYY-MM-DD");
                }
            }
        });
        TextInputEditText endTimeInput = view.findViewById(R.id.end_time_input_text);
        setupTextWatcher(endTimeInput, text -> viewModel.setEventEnd(text));

        ImageButton endTimeButton = view.findViewById(R.id.end_time_dropdown);
        endTimeButton.setOnClickListener(v -> {
            MaterialTimePicker timePicker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(12)
                            .setMinute(0)
                            .setTitleText("Select Event End Time")
                            .build();

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                LocalTime eventTime = LocalTime.of(hour, minute);
                String formattedTime = eventTime.format(timeFormatter);
                endTimeInput.setText(formattedTime);
            });
        });

        // Registration Window
        // Registration Start
        TextInputLayout registrationStartLayout = view.findViewById(R.id.registration_start_input_layout);
        viewModel.getRegistrationStart().observe(getViewLifecycleOwner(), registrationStart -> {
            if (registrationStart != null && !registrationStart.isEmpty()) {
                try {
                    LocalDate.parse(registrationStart, dateFormatter);
                    registrationStartLayout.setError(null);
                } catch (DateTimeParseException e) {
                    registrationStartLayout.setError("Invalid Format - use YYYY-MM-DD");
                }
            }
        });
        TextInputEditText registrationStartInput = view.findViewById(R.id.registration_start_input_text);
        setupTextWatcher(registrationStartInput, text -> viewModel.setRegistrationStart(text));

        ImageButton registrationStartDropdown = view.findViewById(R.id.registration_start_dropdown);
        registrationStartDropdown.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Registration Start Date")
                            .build();

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                ZoneId utcZone = ZoneId.of("UTC");
                Instant instant = Instant.ofEpochMilli(selection);
                LocalDate registrationStartDate = instant.atZone(utcZone).toLocalDate();
                String formattedDate = registrationStartDate.format(dateFormatter);
                registrationStartInput.setText(formattedDate);
            });
        });

        // Registration End
        TextInputLayout registrationEndLayout = view.findViewById(R.id.registration_end_input_layout);
        viewModel.getRegistrationEnd().observe(getViewLifecycleOwner(), registrationEnd -> {
            if (registrationEnd != null && !registrationEnd.isEmpty()) {
                try {
                    LocalDate.parse(registrationEnd, dateFormatter);
                    registrationEndLayout.setError(null);
                } catch (DateTimeParseException e) {
                    registrationEndLayout.setError("Invalid Format - use YYYY-MM-DD");
                }
            }
        });
        TextInputEditText registrationEndInput = view.findViewById(R.id.registration_end_input_text);
        setupTextWatcher(registrationEndInput, text -> viewModel.setRegistrationEnd(text));

        ImageButton registrationEndDropdown = view.findViewById(R.id.registration_end_dropdown);
        registrationEndDropdown.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Registration End Date")
                            .build();

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                ZoneId utcZone = ZoneId.of("UTC");
                Instant instant = Instant.ofEpochMilli(selection);
                LocalDate registrationEndDate = instant.atZone(utcZone).toLocalDate();
                String formattedDate = registrationEndDate.format(dateFormatter);
                registrationEndInput.setText(formattedDate);
            });
        });

        // Sample Size
        TextInputLayout sampleSizeLayout = view.findViewById(R.id.sample_size_input_layout);
        viewModel.getSampleSize().observe(getViewLifecycleOwner(), sampleSize -> {
            if (sampleSize != null) {
                sampleSizeLayout.setError(null);
            }
        });
        TextInputEditText sampleSizeInput = view.findViewById(R.id.sample_size_input_text);
        setupTextWatcher(sampleSizeInput, text -> {
            if (!text.isEmpty()) {
                viewModel.setSampleSize(Integer.parseInt(text));
            } else {
                viewModel.setSampleSize(null);
            }
        });

        // Event Categories
        // TODO: Add event categories

        // Event Poster
        // TODO: HOW TO UPLOAD EVENT POSTER
        // https://www.geeksforgeeks.org/android/android-how-to-upload-an-image-on-firebase-storage/

        // Geolocation Requirement
        CheckBox geolocationCheckBox = view.findViewById(R.id.geolocation_requirement_checkbox);
        geolocationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                viewModel.setGeolocationRequirement(isChecked);
            }
        });

        // Entrant Limit
        TextInputLayout entrantLimitLayout = view.findViewById(R.id.entrant_limit_input_layout);
        viewModel.getEntrantLimit().observe(getViewLifecycleOwner(), entrantLimit -> {
            if (entrantLimit != null) {
                entrantLimitLayout.setError(null);
            }
        });
        TextInputEditText entrantLimitInput = view.findViewById(R.id.entrant_limit_input_text);
        setupTextWatcher(entrantLimitInput, text -> {
            if (!text.isEmpty()) {
                viewModel.setEntrantLimit(Integer.parseInt(text));
            } else {
                viewModel.setEntrantLimit(null);
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

                // Checks if the event is filled in correctly before creating it
                if (viewModel.isEventValid()) {

                    if (!viewModel.eventDateValid(viewModel.getDateValue(), dateFormatter)) {
                        Toast.makeText(getContext(), "Event date has already passed", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!viewModel.eventTimeValid(viewModel.getEventStartValue(), viewModel.getEventEndValue(), timeFormatter)) {
                        Toast.makeText(getContext(), "Check start and end time", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!viewModel.registrationPeriodValid(viewModel.getDateValue(), viewModel.getRegistrationStartValue(), viewModel.getRegistrationEndValue(), dateFormatter)) {
                        Toast.makeText(getContext(), "Registration Period is not valid", Toast.LENGTH_LONG).show();
                        return;
                    }

                    createEvent();
                } else {
                    Toast.makeText(getContext(), "Fill in the Required Fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * A text watcher to deal with user input
     * @param input TextInputEditText object to read values from
     * @param callback Function called to deal with the user input
     */
    private void setupTextWatcher(TextInputEditText input, Consumer<String> callback) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                callback.accept(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }


    // TODO: refactor to put datepicker logic method here
    // TODO: refactor to put timepicker logic method here

    /**
     * Method for creating the Event, filling in the parameters and adding it to the Firestore database
     */
    private void createEvent() {
        String organizerID = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        eventRepository.generateEventID().addOnSuccessListener(snapshot -> {
            long count = snapshot.getCount() + 1;
            String eventID = "event_id" + count;

            Event newEvent = new Event(eventID, organizerID, viewModel.getNameValue(), viewModel.getDescriptionValue(),
                    viewModel.getDateValue(), viewModel.getEventStartValue(), viewModel.getEventEndValue());

            // TODO: change to actually save posterURL
            if (posterURL != null) {
                newEvent.setPosterURL(posterURL);
            }

            newEvent.setGeolocationRequirement(viewModel.getGeolocationRequirementValue());

            if (viewModel.getEntrantLimitValue() != null) {
                newEvent.setEntrantLimit(viewModel.getEntrantLimitValue());
            }

            // TODO: generate QR code and save into database
            // Generate QR Code Bitmap
            // Bitmap qrBitmap = QRCodeGenerator.generateQRCode(eventID);

            // Add event to firebase
            eventRepository.addEvent(newEvent);

            // Update the ListView
            // TODO: figure out why ListView is not updating

            // Clear the Event Creation Form after success
            viewModel.clearForm();

            // Move to Event Detail Screen
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_organizerEventCreateFragment_to_organizerEventListFragment);
        });
    }
}

package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import static com.example.lotteryeventsystem.BuildConfig.MAPS_API_KEY;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Fragment class for the OrganizerEventCreate screen.
 * Stores logic for the event creation form and creates a new Event if input is correct
 * Also used to edit event details if event is already created
 */
public class OrganizerEventCreateFragment extends Fragment {
    private EventCreationForm eventCreationForm;
    private EventRepository eventRepository;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ImageButton eventPoster; // Stores the image being saved?
    private boolean isSport, isArt, isConcert, isFamily;
    private String mode = "create";
    private String existingEventId;
    private Event existingEvent;

    /**
     * Interface for uploading an event poster image to Firebase. Calls the onUploaded method when the upload is complete.
     */
    public interface ImageUploadCallback {
        void onUploaded(String imageUrl);
    }

    /**
     * Interface for uploading a QR code to Firebase. Calls the onQRCodeUploaded method when the upload is complete.
     */
    public interface QRCodeUploadCallback {
        void onQRCodeUploaded(String qrCodeURL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), MAPS_API_KEY);
        }

        // Opens gallery for uploading an image
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
//                        Bitmap scaledBitmap = decodeSampledBitmapFromUri(uri, 1024, 1024);
                        eventPoster.setImageURI(uri); // preview
                        eventCreationForm.setLocalImageUri(uri); // saves the selected image locally first
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_create, container, false);
    }

    /**
     * Called when the fragment's view has been created.
     * Sets up all of the input layouts, input text fields, buttons, and other UI elements.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getString("MODE", "create");
            existingEventId = getArguments().getString("EVENT_ID");
        }
        eventRepository = new EventRepository();
        eventCreationForm = new EventCreationForm();
        Event event = new Event();

        // CONNECTING ALL OF THE UI ELEMENTS //
        // Back Button
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp(); // Navigates back to the previous fragment
        });

        // Event Name
        TextInputLayout eventNameLayout = view.findViewById(R.id.event_name_input_layout);
        TextInputEditText eventNameInput = view.findViewById(R.id.event_name_input);
        setupTextWatcher(eventNameInput, event::setName);

        // Event Description
        TextInputLayout eventDescriptionLayout = view.findViewById(R.id.event_description_input_layout);
        TextInputEditText eventDescriptionInput = view.findViewById(R.id.event_description_input);
        setupTextWatcher(eventDescriptionInput, event::setDescription);

        // Event Location
            // Location Input - uses the Google Places API
        TextInputLayout eventLocationLayout = view.findViewById(R.id.event_location_input_layout);
        TextInputEditText eventLocationInput = view.findViewById(R.id.event_location_input);
        eventLocationInput.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
            );
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .build(requireContext());

            startActivityForResult(intent, 1001);
        });
        setupTextWatcher(eventLocationInput, event::setLocation);

        // Event Start Date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TextInputLayout startDateLayout = view.findViewById(R.id.start_date_input_layout);
        TextInputEditText startDateInput = view.findViewById(R.id.start_date_input_text);
        startDateInput.addTextChangedListener(new TextWatcher() {
            /**
             * Called when the user edits the text in the input field.
             * Sets the event's start date based on the input.
             * Ensures that the input is in the correct format.
             *
             * @param s The updated text in the input field.
             */
            @Override
            public void afterTextChanged(Editable s) {
                String date = s.toString();
                event.setStartDate(date);
                if (!date.isEmpty()) {
                    try {
                        LocalDate.parse(date, dateFormatter);
                        startDateLayout.setError(null);
                    } catch (DateTimeParseException e) {
                        startDateLayout.setError("Invalid Format - use YYYY-MM-DD");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ImageButton startDateCalendarButton = view.findViewById(R.id.start_calendar_button);
        startDateCalendarButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Start Date")
                            .build();

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                ZoneId utcZone = ZoneId.of("UTC");
                Instant instant = Instant.ofEpochMilli(selection);
                LocalDate startDate = instant.atZone(utcZone).toLocalDate();
                String formattedDate = startDate.format(dateFormatter);
                startDateInput.setText(formattedDate);
            });
        });

        // Event End Date
        TextInputLayout endDateLayout = view.findViewById(R.id.end_date_input_layout);
        TextInputEditText endDateInput = view.findViewById(R.id.end_date_input_text);
        endDateInput.addTextChangedListener(new TextWatcher() {
            /**
             * Called when the user edits the text in the input field.
             * Sets the event's end date based on the input.
             * Ensures that the input is in the correct format
             *
             * @param s The updated text in the input field.
             */
            @Override
            public void afterTextChanged(Editable s) {
                String date = s.toString();
                event.setEndDate(date);
                if (!date.isEmpty()) {
                    try {
                        LocalDate.parse(date, dateFormatter);
                        endDateLayout.setError(null);
                    } catch (DateTimeParseException e) {
                        endDateLayout.setError("Invalid Format - use YYYY-MM-DD");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ImageButton endDateCalendarButton = view.findViewById(R.id.end_calendar_button);
        endDateCalendarButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select End Date")
                            .build();

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                ZoneId utcZone = ZoneId.of("UTC");
                Instant instant = Instant.ofEpochMilli(selection);
                LocalDate endDate = instant.atZone(utcZone).toLocalDate();
                String formattedDate = endDate.format(dateFormatter);
                endDateInput.setText(formattedDate);
            });
        });

        // Event Start Time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        TextInputLayout startTimeLayout = view.findViewById(R.id.start_time_input_layout);
        TextInputEditText startTimeInput = view.findViewById(R.id.start_time_input_text);
        startTimeInput.addTextChangedListener(new TextWatcher() {
            /**
             * Called when the user edits the text in the input field.
             * Sets the event's start time based on the input.
             * Ensures that the input is in the correct format
             *
             * @param s The updated text in the input field.
             */
            @Override
            public void afterTextChanged(Editable s) {
                String time = s.toString();
                event.setStartTime(time);
                if (!time.isEmpty()) {
                    try {
                        LocalTime.parse(time, timeFormatter);
                        startTimeLayout.setError(null);
                    } catch (DateTimeParseException e) {
                        startTimeLayout.setError("Invalid Format - use HH:MM AM or HH:MM PM");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

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
        TextInputEditText endTimeInput = view.findViewById(R.id.end_time_input_text);
        endTimeInput.addTextChangedListener(new TextWatcher() {
            /**
             * Called when the user edits the text in the input field.
             * Sets the event's end time based on the input.
             * Ensures that the input is in the correct format
             *
             * @param s The updated text in the input field.
             */
            @Override
            public void afterTextChanged(Editable s) {
                String time = s.toString();
                event.setEndTime(time);
                if (!time.isEmpty()) {
                    try {
                        LocalTime.parse(time, timeFormatter);
                        endTimeLayout.setError(null);
                    } catch (DateTimeParseException e) {
                        endTimeLayout.setError("Invalid Format - use HH:MM AM or HH:MM PM");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

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
        TextInputEditText registrationStartInput = view.findViewById(R.id.registration_start_input_text);
        registrationStartInput.addTextChangedListener(new TextWatcher() {
            /**
             * Called when the user edits the text in the input field.
             * Sets the event's registration start date based on the input.
             * Ensures that the input is in the correct format
             *
             * @param s The updated text in the input field.
             */
            @Override
            public void afterTextChanged(Editable s) {
                String date = s.toString();
                event.setRegistrationStart(date);
                if (!date.isEmpty()) {
                    try {
                        LocalDate.parse(date, dateFormatter);
                        registrationStartLayout.setError(null);
                    } catch (DateTimeParseException e) {
                        registrationStartLayout.setError("Invalid Format - use YYYY-MM-DD");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

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
        TextInputEditText registrationEndInput = view.findViewById(R.id.registration_end_input_text);
        registrationEndInput.addTextChangedListener(new TextWatcher() {
            /**
             * Called when the user edits the text in the input field.
             * Sets the event's registration end date based on the input.
             * Ensures that the input is in the correct format
             *
             * @param s The updated text in the input field.
             */
            @Override
            public void afterTextChanged(Editable s) {
                String date = s.toString();
                event.setRegistrationEnd(date);
                if (!date.isEmpty()) {
                    try {
                        LocalDate.parse(date, dateFormatter);
                        registrationEndLayout.setError(null);
                    } catch (DateTimeParseException e) {
                        registrationEndLayout.setError("Invalid Format - use YYYY-MM-DD");
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

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
        TextInputEditText sampleSizeInput = view.findViewById(R.id.sample_size_input_text);
        setupTextWatcher(sampleSizeInput, text -> {
            if (!text.isEmpty()) {
                event.setSampleSize(Integer.parseInt(text));
            } else {
                event.setSampleSize(null);
            }
        });

        // Event Categories
        CheckBox isConcertCheckbox = view.findViewById(R.id.concert_category_checkbox);
        isConcertCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                isConcert = isChecked;
            }
        });

        CheckBox isSportCheckbox = view.findViewById(R.id.sports_category_checkbox);
        isSportCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                isSport = isChecked;
            }
        });

        CheckBox isArtCheckbox = view.findViewById(R.id.arts_category_checkbox);
        isArtCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                isArt = isChecked;
            }
        });

        CheckBox isFamilyCheckbox = view.findViewById(R.id.family_category_checkbox);
        isFamilyCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                isFamily = isChecked;
            }
        });

        // Event Poster
        eventPoster = view.findViewById(R.id.event_poster_upload_button);
        eventPoster.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        // Geolocation Requirement
        CheckBox geolocationCheckBox = view.findViewById(R.id.geolocation_requirement_checkbox);
        geolocationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                event.setGeolocationRequirement(isChecked);
            }
        });

        // Entrant Limit
        TextInputLayout entrantLimitLayout = view.findViewById(R.id.entrant_limit_input_layout);
        TextInputEditText entrantLimitInput = view.findViewById(R.id.entrant_limit_input_text);
        setupTextWatcher(entrantLimitInput, text -> {
            if (!text.isEmpty()) {
                event.setEntrantLimit(Integer.parseInt(text));
            } else {
                event.setEntrantLimit(null);
            }
        });

        CheckBox entrantLimitCheckBox = view.findViewById(R.id.entrant_limit_checkbox);
        entrantLimitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Logic for if the EntrantLimit checkbox is checked or not.
             * Unhides the EntrantLimit text input if selected.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
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
        if ("edit".equals(mode)) {
            createEventButton.setText("Update Event");
            // Load existing event data
            loadExistingEventData();
        } else {
            createEventButton.setText("Create Event");
        }
        createEventButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Logic for when the Create Event/Edit event button is clicked
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {

                // Checks if the event is filled in correctly before creating it
                if (eventCreationForm.isEventValid(event)) {

                    if (!eventCreationForm.eventDateValid(event.getStartDate(), dateFormatter)) {
                        Toast.makeText(getContext(), "Event date has already passed", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!eventCreationForm.startEndDateValid(event.getStartDate(), event.getEndDate(), dateFormatter)) {
                        Toast.makeText(getContext(), "Check start and end date", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!eventCreationForm.eventTimeValid(event.getStartTime(), event.getEndTime(), timeFormatter)) {
                        Toast.makeText(getContext(), "Check start and end time", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!eventCreationForm.registrationPeriodValid(event.getEndDate(), event.getRegistrationStart(), event.getRegistrationEnd(), dateFormatter)) {
                        Toast.makeText(getContext(), "Registration Period is not valid", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if ("edit".equals(mode)) {
                        setEventCategories(event);
                        updateEvent(event);
                    } else {
                        setEventCategories(event);
                        createEvent(event);

                    }
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

    /**
     * Loads existing event data from Firestore for prefilling the form in edit mode.
     * Retrieves the event document by ID and populates the form fields with current values.
     * If the event doesn't exist or loading fails, displays an error message.
     */
    private void loadExistingEventData() {
        if (existingEventId == null || existingEventId.isEmpty()) {
            return;
        }
        eventRepository.getEventById(existingEventId).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                existingEvent = documentSnapshot.toObject(Event.class);
                if (existingEvent != null) {
                    existingEvent.setEventID(existingEventId);
                    preFillFormFields(existingEvent);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error loading event data", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Pre-fills all form fields with existing event data for editing.
     * Populates text inputs, checkboxes, and image views with current event values.
     * Handles null values by leaving fields empty or using default states.
     *
     * @param event the Event object containing the existing event data to pre-fill the form
     */
    private void preFillFormFields(Event event) {
        TextInputEditText eventNameInput = getView().findViewById(R.id.event_name_input);
        if (event.getName() != null) {
            eventNameInput.setText(event.getName());
        }
        // Event Description
        TextInputEditText eventDescriptionInput = getView().findViewById(R.id.event_description_input);
        if (event.getDescription() != null) {
            eventDescriptionInput.setText(event.getDescription());
        }
        // Start Date
        TextInputEditText startDateInput = getView().findViewById(R.id.start_date_input_text);
        if (event.getStartDate() != null) {
            startDateInput.setText(event.getStartDate());
        }
        // End Date
        TextInputEditText endDateInput = getView().findViewById(R.id.end_date_input_text);
        if (event.getEndDate() != null) {
            endDateInput.setText(event.getEndDate());
        }
        // Start Time
        TextInputEditText startTimeInput = getView().findViewById(R.id.start_time_input_text);
        if (event.getStartTime() != null) {
            startTimeInput.setText(event.getStartTime());
        }
        // End Time
        TextInputEditText endTimeInput = getView().findViewById(R.id.end_time_input_text);
        if (event.getEndTime() != null) {
            endTimeInput.setText(event.getEndTime());
        }
        // Registration Start
        TextInputEditText registrationStartInput = getView().findViewById(R.id.registration_start_input_text);
        if (event.getRegistrationStart() != null) {
            registrationStartInput.setText(event.getRegistrationStart());
        }
        // Registration End
        TextInputEditText registrationEndInput = getView().findViewById(R.id.registration_end_input_text);
        if (event.getRegistrationEnd() != null) {
            registrationEndInput.setText(event.getRegistrationEnd());
        }
        // Sample Size
        TextInputEditText sampleSizeInput = getView().findViewById(R.id.sample_size_input_text);
        if (event.getSampleSize() != null) {
            sampleSizeInput.setText(String.valueOf(event.getSampleSize()));
        }
        // Load Event Poster Image from URL
        if (event.getPosterURL() != null && !event.getPosterURL().isEmpty()) {
            loadImageFromUrl(event.getPosterURL(), eventPoster);
        }
        // Entrant Limit
        TextInputEditText entrantLimitInput = getView().findViewById(R.id.entrant_limit_input_text);
        CheckBox entrantLimitCheckBox = getView().findViewById(R.id.entrant_limit_checkbox);
        if (event.getEntrantLimit() != null) {
            entrantLimitInput.setText(String.valueOf(event.getEntrantLimit()));
            entrantLimitCheckBox.setChecked(true);
            entrantLimitInput.setVisibility(View.VISIBLE);
        } else {
            entrantLimitCheckBox.setChecked(false);
            entrantLimitInput.setVisibility(View.INVISIBLE);
        }
        // Geolocation Requirement
        CheckBox geolocationCheckBox = getView().findViewById(R.id.geolocation_requirement_checkbox);
        geolocationCheckBox.setChecked(event.getGeolocationRequirement());
        // Location
        TextInputEditText eventLocationInput = getView().findViewById(R.id.event_location_input);
        if (event.getLocation() != null) {
            eventLocationInput.setText(event.getLocation());
        }
        // Event categories
        preFillCategoryCheckboxes(event);
    }

    /**
     * Pre-fills the category checkboxes based on the event's existing categories.
     * Resets all checkboxes first, then checks the appropriate ones based on the event's category list.
     * Updates both the UI checkboxes and the corresponding boolean flags.
     *
     * @param event the Event object containing the categories to pre-fill
     */
    private void preFillCategoryCheckboxes(Event event) {
        CheckBox isConcertCheckbox = getView().findViewById(R.id.concert_category_checkbox);
        CheckBox isSportCheckbox = getView().findViewById(R.id.sports_category_checkbox);
        CheckBox isArtCheckbox = getView().findViewById(R.id.arts_category_checkbox);
        CheckBox isFamilyCheckbox = getView().findViewById(R.id.family_category_checkbox);
        // Reset all checkboxes first
        isConcertCheckbox.setChecked(false);
        isSportCheckbox.setChecked(false);
        isArtCheckbox.setChecked(false);
        isFamilyCheckbox.setChecked(false);
        isConcert = false;
        isSport = false;
        isArt = false;
        isFamily = false;
        // Check if event has categories
        if (event.getCategories() != null) {
            for (String category : event.getCategories()) {
                switch (category) {
                    case "Concert":
                        isConcertCheckbox.setChecked(true);
                        isConcert = true;
                        break;
                    case "Sports":
                        isSportCheckbox.setChecked(true);
                        isSport = true;
                        break;
                    case "Art":
                        isArtCheckbox.setChecked(true);
                        isArt = true;
                        break;
                    case "Family":
                        isFamilyCheckbox.setChecked(true);
                        isFamily = true;
                        break;
                }
            }
        }
    }

    /**
     * Updates an existing event in Firestore with the modified data.
     * Preserves original organizer ID, QR code URL, and handles poster image updates.
     * If a new poster image is selected, uploads it first before updating the event.
     * If no new image is selected, preserves the existing poster URL.
     *
     * @param updatedEvent the Event object containing the updated event data
     */
    private void updateEvent(Event updatedEvent) {
        updatedEvent.setEventID(existingEventId);
        if (existingEvent != null) {
            updatedEvent.setOrganizerID(existingEvent.getOrganizerID());
            updatedEvent.setQRCodeURL(existingEvent.getQRCodeURL());
        }
        if (eventCreationForm.getLocalImageUri() != null) {
            eventRepository.uploadPosterToFirebase(requireContext(), eventCreationForm.getLocalImageUri(), existingEventId, new OrganizerEventCreateFragment.ImageUploadCallback() {
                @Override
                public void onUploaded(String imageUrl) {
                    updatedEvent.setPosterURL(imageUrl);
                    eventRepository.addEvent(updatedEvent);
                    navigateAfterUpdate();
                }
            });
        } else {
            if (existingEvent != null) {
                updatedEvent.setPosterURL(existingEvent.getPosterURL());
            }
            eventRepository.addEvent(updatedEvent);
            navigateAfterUpdate();
        }
    }

    /**
     * Navigates back to the previous fragment after successfully updating an event.
     * Displays a success toast message and pops the current fragment from the back stack.
     */
    private void navigateAfterUpdate() {
        Toast.makeText(getContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
        // Navigate back to the event details page
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }

    /**
     * Loads an image from a URL using the Picasso library.
     * Used to display event posters from their stored URLs.
     *
     * @param imageUrl the URL of the image to load
     * @param imageButton the ImageButton to display the loaded image in
     */
    private void loadImageFromUrl(String imageUrl, ImageButton imageButton) {
        Picasso.get()
                .load(imageUrl)
                .into(imageButton);
    }

    /**
     * Method for creating the Event, filling in the parameters and adding it to the Firestore database
     *
     * @param event The Event Class to be created
     */
    private void createEvent(Event event) {
        // Load the organizerID to store into database
        String organizerID = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        eventRepository.generateEventID().addOnSuccessListener(snapshot -> {
            long count = snapshot.getCount() + 1;
            String eventID = "event_id" + Instant.now().toString(); // Generate a unique event ID

            event.setEventID(eventID);
            event.setOrganizerID(organizerID);
            event.setSampled(false); // Initialize sampled as false

            // Check if an event poster was uploaded
            if (eventCreationForm.getLocalImageUri() != null) {
                eventRepository.uploadPosterToFirebase(requireContext(), eventCreationForm.getLocalImageUri(), eventID, imageUrl ->  {
                    event.setPosterURL(imageUrl); // Sets the image URL to store in firebase

                    // Generate QR Code Bitmap
                    Bitmap qrBitmap = QRCodeGenerator.generateQRCode(eventID);
                    // Upload QR Code to Firebase
                    eventRepository.uploadQRCodeToFirebase(qrBitmap, eventID, qrCodeURL -> {
                        event.setQRCodeURL(qrCodeURL);

                        // Add event to firebase
                        eventRepository.addEvent(event);

                        // Move to Event List Screen
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_organizerEventCreateFragment_to_homeFragment);
                    });
                });
            // Else create an event without uploading the event poster
            } else {
                // Generate QR Code Bitmap
                Bitmap qrBitmap = QRCodeGenerator.generateQRCode(eventID);
                // Upload QR Code to Firebase
                eventRepository.uploadQRCodeToFirebase(qrBitmap, eventID, qrCodeURL -> {
                    event.setQRCodeURL(qrCodeURL);

                    // Add event to firebase
                    eventRepository.addEvent(event);

                    // Move to Event List Screen
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_organizerEventCreateFragment_to_homeFragment);
                });
            }
        });
    }

    /**
     * Sets the event's categories based on the selected checkboxes.
     *
     * @param event The event to set the categories for.
     */
    private void setEventCategories(Event event) {
        ArrayList<String> categories = new ArrayList<>();
        if (isSport) {
            categories.add("Sports");
        }
        if (isConcert) {
            categories.add("Concert");
        }
        if (isArt) {
            categories.add("Art");
        }
        if (isFamily) {
            categories.add("Family");
        }
        event.setCategories(categories);
    }

    /**
     * Called when a result from an activity is received.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            if (resultCode == getActivity().RESULT_OK && data != null) {

                Place place = Autocomplete.getPlaceFromIntent(data);

                // Update the text field
                TextInputEditText eventLocationInput =
                        getView().findViewById(R.id.event_location_input);
                eventLocationInput.setText(place.getAddress());

                Log.d("LOCATION", "Selected: " + place.getAddress());

            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Log.d("LOCATION", "User canceled autocomplete");
            }
        }
    }
}

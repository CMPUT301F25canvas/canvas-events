package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.graphics.Bitmap;
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

//    private OrganizerEventCreateViewModel viewModel;
    private EventCreationForm eventCreationForm;
    private EventRepository eventRepository;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ImageView imageView; // Stores the image being saved?
    String posterURL; // temporary will change


    // Callback Interface for when an image is uploaded
    public interface ImageUploadCallback {
        void onUploaded(String imageUrl);
    }

    public interface QRCodeUploadCallback {
        void onQRCodeUploaded(String qrCodeURL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Opens gallery for uploading an image
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageView.setImageURI(uri); // preview
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


    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventRepository = new EventRepository();
        eventCreationForm = new EventCreationForm();
        Event event = new Event();
//        viewModel = new ViewModelProvider(this).get(OrganizerEventCreateViewModel.class);

        // Connecting all of the elements
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

        // Event Date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TextInputLayout eventDateLayout = view.findViewById(R.id.event_date_input_layout);
        TextInputEditText eventDateInput = view.findViewById(R.id.event_date_input_text);
        eventDateInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String date = s.toString();
                event.setDate(date);
                if (!date.isEmpty()) {
                    try {
                        LocalDate.parse(date, dateFormatter);
                        eventDateLayout.setError(null);
                    } catch (DateTimeParseException e) {
                        eventDateLayout.setError("Invalid Format - use YYYY-MM-DD");
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
        TextInputEditText startTimeInput = view.findViewById(R.id.start_time_input_text);
        startTimeInput.addTextChangedListener(new TextWatcher() {
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
        // TODO: ADD EVENT CATEGORIES

        // Event Poster
        ImageButton eventPosterButton = view.findViewById(R.id.event_poster_upload_button);
        imageView = view.findViewById(R.id.event_poster_image);
        eventPosterButton.setOnClickListener(v -> {
            ImageView posterImage = view.findViewById(R.id.event_poster_image);
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
                if (eventCreationForm.isEventValid(event)) {

                    if (!eventCreationForm.eventDateValid(event.getDate(), dateFormatter)) {
                        Toast.makeText(getContext(), "Event date has already passed", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!eventCreationForm.eventTimeValid(event.getStartTime(), event.getEndTime(), timeFormatter)) {
                        Toast.makeText(getContext(), "Check start and end time", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!eventCreationForm.registrationPeriodValid(event.getDate(), event.getRegistrationStart(), event.getRegistrationEnd(), dateFormatter)) {
                        Toast.makeText(getContext(), "Registration Period is not valid", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Create Event if everything is valid
                    createEvent(event);
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
    private void createEvent(Event event) {
        // Load the organizerID to store into database
        String organizerID = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        eventRepository.generateEventID().addOnSuccessListener(snapshot -> {
            long count = snapshot.getCount() + 1;
            String eventID = "event_id" + count;

            event.setEventID(eventID);
            event.setOrganizerID(organizerID);

            // Function to actually save the event to Firebase
            Runnable saveEvent = () -> {
                eventRepository.addEvent(event);
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.homeFragment); // Or event list screen
            };

            // Upload poster if available, otherwise just save the event
            Uri posterUri = eventCreationForm.getLocalImageUri();
            eventRepository.uploadPosterToFirebase(posterUri, eventID, posterUrl -> {
                event.setPosterURL(posterUrl); // will be null if no poster
                saveEvent.run(); // save event after poster handling
            });

            // Generate QR Code Bitmap
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(eventID);
            // Upload QR Code to Firebase
            eventRepository.uploadQRCodeToFirebase(qrBitmap, eventID, qrCodeURL -> {
                event.setQRCodeURL(qrCodeURL);
            });


            // Update the ListView
            // TODO: figure out why ListView is not updating

            // TODO: navigate to the event_list screen instead maybe
        });
    }

}

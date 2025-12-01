package com.example.lotteryeventsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryeventsystem.util.EventLinkParser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Home screen for entrants.
 * Lets them scan a QR-code or click and event to jump into an event page.
 */
public class HomeFragment extends Fragment {
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<ScanOptions> scanLauncher;

    private HomeEventAdapter adapter;
    private ArrayList<EventItem> itemsList;
    private ArrayList<EventItem> filteredItems;
    private final FilterState filterState = new FilterState();

    private static ArrayList<EventItem> cachedItems;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
                if (((MainActivity) requireActivity()).getAdmin()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_profileFragment_to_adminHomeFragment);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadEvents();
    }

    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.home_header);
        title.setText("Events");
        ImageButton scanButton = view.findViewById(R.id.button_scan_qr);
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        ImageButton createButton = view.findViewById(R.id.create_event_button);
        TextInputEditText searchInput = view.findViewById(R.id.search_input);
        RecyclerView recyclerView = view.findViewById(R.id.events_recycler);

        itemsList = new ArrayList<>();
        filteredItems = new ArrayList<>();
        adapter = new HomeEventAdapter(item -> {
            Bundle args = new Bundle();
            args.putString(EventDetailFragment.ARG_EVENT_ID, item.id);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_homeFragment_to_eventDetailFragment, args);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        reloadEvents();

        filterButton.setOnClickListener(v -> showFilterDialog());

        registerPermissionLauncher();
        registerScanLauncher();
        scanButton.setOnClickListener(v -> showQrDialog());

        searchInput.addTextChangedListener(new SimpleTextWatcher(text ->
                applyFilter(text != null ? text : "")));

        createButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_homeFragment_to_organizerEventCreateFragment);
        });
    }

    private void reloadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemsList.clear();
                    for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                        EventItem item = mapToEventItem(doc);
                        itemsList.add(item);
                    }
                    cachedItems = new ArrayList<>(itemsList);
                    applyFilter("");
                });
    }

    /**
     * Launches the QR-code scanner.
     * If it has permission start scanning
     * Otherwise, get permission.
     */
    private void launchScanner() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Starts the QR code scanning process with configured options.
     */
    private void startScanning() {
        ScanOptions options = new ScanOptions();
        options.setPrompt(getString(R.string.scan_prompt));
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        scanLauncher.launch(options);
    }

    /**
     * Registers a permission launcher to handle the result of the camera permission request.
     */
    private void registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startScanning();
                    } else if (getContext() != null) {
                        Toast.makeText(getContext(),
                                R.string.scan_camera_permission_denied,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Registers a launcher to handle the result of a QR code scan.
     */
    private void registerScanLauncher() {
        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result == null || result.getContents() == null) {
                showToast(R.string.scan_cancelled_message);
                return;
            }
            handleScanResult(result.getContents());
        });
    }

    /**
     * Handles the result of a QR code scan.
     * Parses the scanned content to extract an event ID and navigates to the EventDetailFragment.
     * @param contents the scanned QR code contents
     */
    private void handleScanResult(String contents) {
        String eventId = EventLinkParser.parseEventId(contents);
        if (eventId == null || eventId.isEmpty()) {
            showToast(R.string.scan_unknown_event_message);
            return;
        }
        Bundle args = new Bundle();
        args.putString(EventDetailFragment.ARG_EVENT_ID, eventId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_homeFragment_to_eventDetailFragment, args);
    }

    /**
     * Displays a short toast message using the provided string.
     * @param messageRes
     */
    private void showToast(int messageRes) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), messageRes, Toast.LENGTH_SHORT).show();
    }

    private void applyFilter(String query) {
        filteredItems.clear();
        String lower = query.toLowerCase();

        for (EventItem item : itemsList) {

            String name = item.name != null ? item.name.toLowerCase() : "";
            String description = item.description != null ? item.description.toLowerCase() : "";
            String location = item.location != null ? item.location.toLowerCase() : "";

            // 🔍 Query filter: name OR description OR location OR category text
            boolean passesQuery =
                    lower.isEmpty() ||
                            name.contains(lower) ||
                            description.contains(lower) ||
                            location.contains(lower) ||
                            item.category != null &&
                                    item.category.stream()
                                            .anyMatch(c -> c.toLowerCase().contains(lower));  // <-- search categories too

            boolean passesCategory = matchesCategory(item);
            boolean passesDate = matchesDate(item);
            boolean passesAvailability = matchesAvailability(item);
            boolean passesDistance = matchesDistance(item);

            if (passesQuery && passesCategory && passesDate && passesAvailability && passesDistance) {
                filteredItems.add(item);
            }
        }

        adapter.submitList(new ArrayList<>(filteredItems));
    }

    private boolean matchesCategory(EventItem item) {
        // If no category selected → always pass
        if (filterState.category == null || filterState.category.equals("any")) {
            return true;
        }

        // If item has no categories → fail
        if (item.category == null || item.category.isEmpty()) {
            return false;
        }

        String wanted = filterState.category.toLowerCase();

        // Event belongs to multiple categories → check if ANY matches
        return item.category.stream()
                .anyMatch(cat -> cat != null && cat.toLowerCase().equals(wanted));
    }

    private boolean matchesDate(EventItem item) {
        if (filterState.fromDate == null && filterState.toDate == null) {
            return true;
        }
        LocalDate eventDate = parseDate(item.dateHighlight);
        if (eventDate == null) {
            return true;
        }
        if (filterState.fromDate != null && eventDate.isBefore(filterState.fromDate)) {
            return false;
        }
        if (filterState.toDate != null && eventDate.isAfter(filterState.toDate)) {
            return false;
        }
        return true;
    }

    private boolean matchesAvailability(EventItem item) {
        if (!filterState.onlyAvailable) {
            return true;
        }
        // Placeholder: assume available if no capacity data.
        return true;
    }

    private boolean matchesDistance(EventItem item) {
        if (filterState.maxDistanceKm == null) {
            return true;
        }
        if (item.latitude == null || item.longitude == null) {
            return true;
        }
        double[] userLocation = tryGetUserLocation();
        if (userLocation == null) {
            return true;
        }
        double distance = haversine(userLocation[0], userLocation[1], item.latitude, item.longitude);
        return distance <= filterState.maxDistanceKm;
    }

    private EventItem mapToEventItem(QueryDocumentSnapshot doc) {
        String name = doc.getString("name");
        String description = doc.getString("description");
        String date = doc.getString("date");
        String registrationStart = doc.getString("registrationStart");
        String registrationEnd = doc.getString("registrationEnd");
        @SuppressWarnings("unchecked")
        ArrayList<String> category = (ArrayList<String>) doc.get("categories");
        Double latitude = doc.getDouble("latitude");
        Double longitude = doc.getDouble("longitude");
        String posterUrl = doc.getString("posterURL");
        String location = doc.getString("location");



        String highlight = registrationStart != null && !registrationStart.isEmpty()
                ? registrationStart
                : date;
        String range = buildRange(registrationStart, registrationEnd, date);

        return new EventItem(doc.getId(),
                name != null ? name : getString(R.string.event_detail_name_fallback),
                description != null ? description : "",
                highlight,
                range,
                category,
                latitude,
                longitude,
                posterUrl,
                location != null ? location : "No Location Provided");
    }

    private String buildRange(String start, String end, String fallbackDate) {
        if (start != null && end != null && !start.isEmpty() && !end.isEmpty()) {
            return formatRange(start) + " - " + formatRange(end);
        }
        if (fallbackDate != null && !fallbackDate.isEmpty()) {
            return formatRange(fallbackDate);
        }
        return getString(R.string.event_detail_unknown_time);
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

    private void showQrDialog() {
        if (getContext() == null) {
            return;
        }
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_qr_entry, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        TextInputEditText codeInput = dialogView.findViewById(R.id.qr_code_input);
        View scanWithCamera = dialogView.findViewById(R.id.qr_scan_with_camera);
        View cancel = dialogView.findViewById(R.id.qr_cancel_button);
        View openEvent = dialogView.findViewById(R.id.qr_open_button);

        scanWithCamera.setOnClickListener(v -> {
            dialog.dismiss();
            launchScanner();
        });
        cancel.setOnClickListener(v -> dialog.dismiss());
        openEvent.setOnClickListener(v -> {
            String code = codeInput.getText() != null ? codeInput.getText().toString().trim() : "";
            if (code.isEmpty()) {
                showToast(R.string.scan_unknown_event_message);
            } else {
                handleScanResult(code);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showFilterDialog() {
        if (getContext() == null) {
            return;
        }
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_events, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        android.widget.RadioGroup categoryGroup = dialogView.findViewById(R.id.category_group);
        TextInputEditText dateFrom = dialogView.findViewById(R.id.date_from_input);
        TextInputEditText dateTo = dialogView.findViewById(R.id.date_to_input);
        android.widget.CheckBox availability = dialogView.findViewById(R.id.availability_check);

        dialogView.findViewById(R.id.filter_cancel_button).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.filter_clear_button).setOnClickListener(v -> {
            categoryGroup.check(R.id.category_any);
            dateFrom.setText("");
            dateTo.setText("");
            availability.setChecked(false);
            filterState.category = "any";
            filterState.fromDate = null;
            filterState.toDate = null;
            filterState.onlyAvailable = false;
            filterState.maxDistanceKm = null;
            applyFilter(searchInputValueSafe());
        });
        View.OnClickListener datePickerListener = pickerView -> {
            boolean isFrom = pickerView.getId() == R.id.date_from_input;
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(isFrom ? "Choose start date" : "Choose end date")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                LocalDate chosen = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                String formatted = chosen.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH));
                if (isFrom) {
                    dateFrom.setText(formatted);
                } else {
                    dateTo.setText(formatted);
                }
            });
            picker.show(getParentFragmentManager(), "DATE_PICKER");
        };
        dateFrom.setOnClickListener(datePickerListener);
        dateTo.setOnClickListener(datePickerListener);
        dialogView.findViewById(R.id.filter_apply_button).setOnClickListener(v -> {
            int checked = categoryGroup.getCheckedRadioButtonId();
            if (checked == R.id.category_concert) {
                filterState.category = "concert";
            } else if (checked == R.id.category_sports) {
                filterState.category = "sports";
            } else if (checked == R.id.category_arts) {
                filterState.category = "art";
            }  else if (checked == R.id.category_family) {
                filterState.category = "family";
            } else {
                filterState.category = "any";
            }
            filterState.fromDate = parseDate(dateFrom.getText() != null ? dateFrom.getText().toString() : null);
            filterState.toDate = parseDate(dateTo.getText() != null ? dateTo.getText().toString() : null);
            filterState.onlyAvailable = availability.isChecked();
            applyFilter(searchInputValueSafe());
            dialog.dismiss();
        });

        dialog.show();
    }

    private String searchInputValueSafe() {
        TextInputEditText searchInput = getView() != null ? getView().findViewById(R.id.search_input) : null;
        if (searchInput == null || searchInput.getText() == null) {
            return "";
        }
        return searchInput.getText().toString();
    }

    private double[] tryGetUserLocation() {
        android.location.LocationManager locationManager = (android.location.LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        android.location.Location location = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
        }
        if (location == null) {
            return null;
        }
        return new double[]{location.getLatitude(), location.getLongitude()};
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static class FilterState {
        String category = "any";
        LocalDate fromDate;
        LocalDate toDate;
        boolean onlyAvailable = false;
        Double maxDistanceKm;
    }

    /**
     * Lightweight watcher to avoid anonymous classes everywhere.
     */
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Consumer<String> onChange;

        SimpleTextWatcher(Consumer<String> onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            onChange.accept(s != null ? s.toString() : "");
        }
    }
}

package com.example.lotteryeventsystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotteryeventsystem.data.FirebaseWaitlistRepository;
import com.example.lotteryeventsystem.data.NotificationRepository;
import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.model.NotificationStatus;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment that displays a filtered list of event entrants based on their status.
 * This fragment shows different categories of entrants (Canceled, Enrolled, Unenrolled)
 * and provides functionality to manage them, including canceling unenrolled entrants.
 *
 * @author Emily Lan
 * @version 1.1
 * @see OrganizerEntrantListFragment
 * @see WaitlistEntry
 * @see WaitlistStatus
 * @see FirebaseWaitlistRepository
 */
public class EntrantListFragment extends Fragment {
    private ListView listView;
    private TextView tvTitle;
    private ImageButton btnBack, btnFilter;
    private Button btnDeleteSelectedEntrant, btnExportCSV, btnNotifyAll;
    private int entrantPosition = -1;
    private WaitlistEntryAdapter adapter;
    private final FirebaseWaitlistRepository repository = new FirebaseWaitlistRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private ArrayList<WaitlistEntry> entrantsList = new ArrayList<>();
    private String eventId;
    private String listType;

    /**
     * Method to create a new instance of EntrantListFragment with required parameters.
     *
     * @param eventId  The Firestore document ID of the event
     * @param listType The type of list to display: "canceled", "enrolled", or "unenrolled"
     * @return A new instance of EntrantListFragment with the provided arguments
     */
    public static EntrantListFragment newInstance(String eventId, String listType) {
        EntrantListFragment fragment = new EntrantListFragment();
        Bundle args = new Bundle();
        args.putString("EVENT_ID", eventId);
        args.putString("LIST_TYPE", listType);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This method inflates the fragment's layout, retrieves arguments, and initializes
     * the UI components and data loading.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views
     * @param container          The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_view_entrant_list, container, false);
        if (getArguments() != null) {
            eventId = getArguments().getString("EVENT_ID");
            listType = getArguments().getString("LIST_TYPE");
        }
        initializeViews(view);
        setupClickListeners();
        loadDataFromFirebase();
        return view;
    }

    /**
     * Initializes all the view components and sets up the adapter for the ListView.
     * Configures the title and visibility of the delete button based on the list type.
     *
     * @param view The root view of the fragment containing all the UI components
     */
    private void initializeViews(View view) {
        listView = view.findViewById(R.id.listView);
        tvTitle = view.findViewById(R.id.tvTitle);
        btnBack = view.findViewById(R.id.back_button);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnDeleteSelectedEntrant = view.findViewById(R.id.btnDeleteSelectedEntrant);
        btnExportCSV = view.findViewById(R.id.btnExportCSV);
        btnNotifyAll = view.findViewById(R.id.btnNotifyAll);
        adapter = new WaitlistEntryAdapter(requireContext(), entrantsList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        updateTitle();
    }

    private void updateTitle() {
        if ("canceled".equals(listType)) {
            tvTitle.setText("Canceled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
            btnExportCSV.setVisibility(View.GONE);
        } else if ("unenrolled".equals(listType)) {
            tvTitle.setText("Unenrolled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.VISIBLE);
            btnExportCSV.setVisibility(View.GONE);
        } else if ("enrolled".equals(listType)) {
            tvTitle.setText("Enrolled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
            btnExportCSV.setVisibility(View.VISIBLE);
        } else if ("waiting".equals(listType)) {
            tvTitle.setText("Waitlist");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
            btnExportCSV.setVisibility(View.GONE);
        } else {
            tvTitle.setText("All Chosen Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
            btnExportCSV.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up all click listeners for the fragment's interactive elements.
     * Includes back navigation, item selection, and entrant deletion functionality.
     *
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterMenu(v);
            }
        });

        btnExportCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportEnrolledEntrantsToCSV();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Handles item clicks in the ListView to select an entrant.
             * Stores the position of the selected entrant for later operations.
             *
             * @param parent The AdapterView where the click happened
             * @param view The view within the AdapterView that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that was clicked
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                entrantPosition = position;
            }
        });

        btnDeleteSelectedEntrant.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles delete button click to cancel the selected unenrolled entrant.
             * Only functional when an entrant is selected and only in the unenrolled list.
             *
             * @param v The clicked view (delete button)
             */
            @Override
            public void onClick(View v) {
                if (entrantPosition != -1) {
                    WaitlistEntry entrantToDelete = entrantsList.get(entrantPosition);
                    deleteEntrantFromFirebase(entrantToDelete);
                }
            }
        });
    }

    /**
     * Exports enrolled entrants to CSV and shows it in a dialog
     */
    private void exportEnrolledEntrantsToCSV() {
        if (entrantsList.isEmpty()) {
            Toast.makeText(getContext(), "No enrolled entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create CSV content - names separated by commas on one line
            StringBuilder csvContent = new StringBuilder();

            for (int i = 0; i < entrantsList.size(); i++) {
                WaitlistEntry entrant = entrantsList.get(i);

                // Use the same logic as WaitlistEntryAdapter to get display name
                String displayName = adapter.getUserDisplayName(entrant);
                if (displayName == null || displayName.isEmpty()) {
                    // If no name, use anonymous numbering like the adapter does
                    int anonymousNumber = adapter.getConsistentAnonymousNumber(entrant);
                    displayName = "Anonymous" + anonymousNumber;
                }

                // Escape commas and quotes in the name for proper CSV format
                String escapedName = displayName.replace("\"", "\"\"");
                if (escapedName.contains(",") || escapedName.contains("\"")) {
                    escapedName = "\"" + escapedName + "\"";
                }

                // Add the name to CSV
                csvContent.append(escapedName);

                // Add comma separator if it's not the last entry
                if (i < entrantsList.size() - 1) {
                    csvContent.append(",");
                }
            }

            // Show the CSV content in a dialog
            showCSVContentInDialog(csvContent.toString());

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves CSV content to a file (optional - for users who still want files)
     */
    private void saveCSVFile(String fileName, String csvContent) {
        try {
            // Use the public Download folder
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);

            // Create parent directories if they don't exist
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            // Write CSV content to file
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvContent);
            writer.flush();
            writer.close();

            // Show success message
            Toast.makeText(getContext(), "CSV saved to Downloads folder", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(getContext(), "Error saving CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows CSV content in a dialog within the app
     */
    private void showCSVContentInDialog(String csvContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enrolled Entrants CSV (" + entrantsList.size() + " entrants)");

        // Create a scrollable text view
        ScrollView scrollView = new ScrollView(requireContext());
        TextView textView = new TextView(requireContext());

        // Format the text for better readability
        textView.setText(csvContent);
        textView.setPadding(50, 30, 50, 30);
        textView.setTextIsSelectable(true); // Allow text selection
        textView.setTextSize(14); // Slightly smaller to fit more text
        textView.setTypeface(Typeface.MONOSPACE); // Use monospace font for CSV

        scrollView.addView(textView);
        builder.setView(scrollView);

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("Save as CSV", (dialog, which) -> {
            // Save the file for users who want it
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = "enrolled_entrants_" + timestamp + ".csv";
            saveCSVFile(fileName, csvContent);
        });

        builder.show();
    }

    private void showFilterMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenu().add("All Chosen Entrants");
        popupMenu.getMenu().add("Enrolled Entrants");
        popupMenu.getMenu().add("Canceled Entrants");
        popupMenu.getMenu().add("Unenrolled Entrants");
        popupMenu.getMenu().add("Waitlist");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "All Chosen Entrants":
                    listType = "all";
                    break;
                case "Enrolled Entrants":
                    listType = "enrolled";
                    break;
                case "Canceled Entrants":
                    listType = "canceled";
                    break;
                case "Unenrolled Entrants":
                    listType = "unenrolled";
                    break;
                case "Waitlist":
                    listType = "waiting";
                    break;
            }
            updateTitle();
            loadDataFromFirebase();
            return true;
        });

        popupMenu.show();
    }

    /**
     * Loads entrants from Firestore based on the current list type and event ID.
     * Filters entrants by status and updates the ListView adapter with the results.
     * This method is automatically called when the fragment is created.
     */
    private void loadDataFromFirebase() {
        if (listType == null || eventId == null) {
            return;
        }
        List<WaitlistStatus> statusesToLoad = new ArrayList<>();
        switch (listType) {
            case "all":
                statusesToLoad.add(WaitlistStatus.CONFIRMED);
                statusesToLoad.add(WaitlistStatus.INVITED);
                statusesToLoad.add(WaitlistStatus.CANCELLED);
                statusesToLoad.add(WaitlistStatus.DECLINED);
                break;
            case "waiting":
                statusesToLoad.add(WaitlistStatus.WAITING);
                break;
            case "canceled":
                statusesToLoad.add(WaitlistStatus.CANCELLED);
                statusesToLoad.add(WaitlistStatus.DECLINED);
                break;
            case "enrolled":
                statusesToLoad.add(WaitlistStatus.CONFIRMED);
                break;
            case "unenrolled":
                statusesToLoad.add(WaitlistStatus.INVITED);
                break;
            default:
                return;
        }

        repository.getEntrantsByStatusWithUserDetails(eventId, statusesToLoad, new RepositoryCallback<List<WaitlistEntry>>() {
            /**
             * Callback method that handles the result of the Firestore query.
             * Updates the UI with the loaded entrants or handles any errors.
             *
             * @param result The list of WaitlistEntry objects retrieved from Firestore, or null if error
             * @param error The exception that occurred during the query, or null if successful
             */
            @Override
            public void onComplete(List<WaitlistEntry> result, Exception error) {
                if (error == null && result != null) {
                    entrantsList.clear();
                    entrantsList.addAll(result);
                    adapter.notifyDataSetChanged();
                    if (btnNotifyAll != null) {
                        btnNotifyAll.setEnabled(!entrantsList.isEmpty());
                    }
                }
            }
        });
    }

    /**
     * Updates the status of an entrant to CANCELLED in Firestore and removes them
     * from the current list. This method is only available for unenrolled entrants.
     *
     * @param entrant The WaitlistEntry object representing the entrant to be canceled
     */
    private void deleteEntrantFromFirebase(WaitlistEntry entrant) {
        repository.updateEntrantStatus(eventId, entrant.getId(), WaitlistStatus.CANCELLED,
                new RepositoryCallback<WaitlistEntry>() {
                    /**
                     * Callback method that handles the result of the status update operation.
                     * Removes the entrant from the local list and updates the UI on success.
                     *
                     * @param updatedEntry The updated WaitlistEntry object, or null if error
                     * @param error The exception that occurred during the update, or null if successful
                     */
                    @Override
                    public void onComplete(WaitlistEntry updatedEntry, Exception error) {
                        if (error == null) {
                            String userId = entrant.getId(); //
                            if (userId != null && !userId.isEmpty()) {
                                NotificationsManager.sendInviteCancelled(requireContext(), eventId, userId);
                            }
                            // Success - remove from current list and refresh
                            entrantsList.remove(entrantPosition);
                            entrantPosition = -1;
                            adapter.notifyDataSetChanged();
                            // Sample one person, print toast message
                            sampleSingleEntrantAfterDeletion();
                        }
                    }
                });
    }

    private void sampleSingleEntrantAfterDeletion() {
        // Get all waiting entrants
        repository.getWaitingEntrants(eventId, new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onComplete(List<WaitlistEntry> result, Exception error) {
                if (error != null) {
                    Toast.makeText(getContext(), "Error loading entrants: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (result == null || result.isEmpty()) {
                    Toast.makeText(getContext(), "No waiting entrants found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sample exactly 1 entrant
                List<WaitlistEntry> selectedEntrant = getRandomSample(result, 1);

                if (!selectedEntrant.isEmpty()) {
                    // Update status to INVITED
                    updateEntrantsStatus(selectedEntrant, WaitlistStatus.INVITED);

                    // Send notification to the selected entrant
                    sendSelectedNotifications(selectedEntrant);

                    Toast.makeText(getContext(),
                            "Sampled new entrant",
                            Toast.LENGTH_LONG).show();

                    // Refresh the list
                    loadDataFromFirebase();
                }
            }
        });
    }

    private List<WaitlistEntry> getRandomSample(List<WaitlistEntry> allEntrants, int sampleSize) {
        List<WaitlistEntry> shuffled = new ArrayList<>(allEntrants);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, sampleSize);
    }

    private void updateEntrantsStatus(List<WaitlistEntry> entrants, WaitlistStatus status) {
        for (WaitlistEntry entrant : entrants) {
            repository.updateEntrantStatus(eventId, entrant.getId(), status,
                    new RepositoryCallback<WaitlistEntry>() {
                        @Override
                        public void onComplete(WaitlistEntry result, Exception error) {
                            // Handle errors if needed
                        }
                    });
        }
    }

    private void sendSelectedNotifications(List<WaitlistEntry> selectedEntrants) {
        for (WaitlistEntry entrant : selectedEntrants) {
            String userId = entrant.getId();
            if (userId != null && !userId.isEmpty()) {
                NotificationsManager.sendSelected(requireContext(), eventId, userId);
            }
        }
    }
}

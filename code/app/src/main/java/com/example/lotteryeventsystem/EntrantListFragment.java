package com.example.lotteryeventsystem;

import android.app.AlertDialog;
import android.graphics.Typeface;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.lotteryeventsystem.data.FirebaseWaitlistRepository;
import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a filtered list of event entrants based on their status.
 * This fragment shows different categories of entrants (Canceled, Enrolled, Unenrolled, Waiting, All Chosen, and Declined)
 * and provides functionality to manage them, including canceling unenrolled entrants, exporting enrolled entrants to CSV,
 * and filtering between different entrant status views.
 *
 * @author Emily Lan
 * @version 1.2
 * @see OrganizerEntrantListFragment
 * @see WaitlistEntry
 * @see WaitlistStatus
 * @see FirebaseWaitlistRepository
 */
public class EntrantListFragment extends Fragment {
    private ListView listView;
    private TextView tvTitle;
    private ImageButton btnBack, btnFilter;
    private Button btnDeleteSelectedEntrant, btnExportCSV;
    private int entrantPosition = -1;
    private WaitlistEntryAdapter adapter;
    private final FirebaseWaitlistRepository repository = new FirebaseWaitlistRepository();
    private ArrayList<WaitlistEntry> entrantsList = new ArrayList<>();
    private String eventId;
    private String listType;
    private SampleEntrantsManager sampleManager;

    /**
     * Method to create a new instance of EntrantListFragment with required parameters.
     *
     * @param eventId  The Firestore document ID of the event
     * @param listType The type of list to display: "canceled", "enrolled", "unenrolled", "waiting", "all", or "declined"
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
        sampleManager = new SampleEntrantsManager(requireContext(), repository, eventId);
        initializeViews(view);
        setupClickListeners();
        loadDataFromFirebase();
        return view;
    }

    /**
     * Initializes all the view components and sets up the adapter for the ListView.
     * Configures the title and visibility of buttons based on the list type.
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
        adapter = new WaitlistEntryAdapter(requireContext(), entrantsList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        updateTitle();
    }

    /**
     * Updates the fragment title and button visibility based on the current list type.
     */
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
     * Includes back navigation, filter menu, CSV export, item selection, entrant deletion, and notification functionality.
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
        btnFilter.setOnClickListener(new View.OnClickListener() {
            /**
             * Shows the filter menu when the filter button is clicked.
             *
             * @param v The clicked view (filter button)
             */
            @Override
            public void onClick(View v) {
                showFilterMenu(v);
            }
        });
        btnExportCSV.setOnClickListener(new View.OnClickListener() {
            /**
             * Exports enrolled entrants to CSV format when the export button is clicked.
             *
             * @param v The clicked view (export button)
             */
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
     * Exports enrolled entrants to CSV format and displays the content in a dialog.
     * The CSV contains entrant names separated by commas, with proper escaping for CSV format.
     * Users can view the content in-app or save it as a file.
     */
    private void exportEnrolledEntrantsToCSV() {
        if (entrantsList.isEmpty()) {
            Toast.makeText(getContext(), "No enrolled entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            StringBuilder csvContent = new StringBuilder();
            for (int i = 0; i < entrantsList.size(); i++) {
                WaitlistEntry entrant = entrantsList.get(i);
                String displayName = adapter.getUserDisplayName(entrant);
                if (displayName == null || displayName.isEmpty()) {
                    int anonymousNumber = adapter.getConsistentAnonymousNumber(entrant);
                    displayName = "Anonymous" + anonymousNumber;
                }
                String escapedName = displayName.replace("\"", "\"\"");
                if (escapedName.contains(",") || escapedName.contains("\"")) {
                    escapedName = "\"" + escapedName + "\"";
                }
                csvContent.append(escapedName);
                if (i < entrantsList.size() - 1) {
                    csvContent.append(",");
                }
            }
            showCSVContentInDialog(csvContent.toString());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves CSV content to a file in the device's Downloads folder.
     *
     * @param fileName The name of the file to create
     * @param csvContent The CSV content to write to the file
     */
    private void saveCSVFile(String fileName, String csvContent) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvContent);
            writer.flush();
            writer.close();
            Toast.makeText(getContext(), "CSV saved to Downloads folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error saving CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows CSV content in a scrollable dialog within the app.
     * The dialog allows users to view the CSV content and optionally save it as a file.
     *
     * @param csvContent The CSV content to display in the dialog
     */
    private void showCSVContentInDialog(String csvContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enrolled Entrants CSV (" + entrantsList.size() + " entrants)");
        ScrollView scrollView = new ScrollView(requireContext());
        TextView textView = new TextView(requireContext());
        textView.setText(csvContent);
        textView.setPadding(50, 30, 50, 30);
        textView.setTextIsSelectable(true);
        textView.setTextSize(14);
        textView.setTypeface(Typeface.MONOSPACE);
        scrollView.addView(textView);
        builder.setView(scrollView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Save as CSV", (dialog, which) -> {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = "enrolled_entrants_" + timestamp + ".csv";
            saveCSVFile(fileName, csvContent);
        });
        builder.show();
    }

    /**
     * Displays a filter menu allowing users to switch between different entrant status views.
     *
     * @param anchor The view to anchor the popup menu to
     */
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
     * This method is automatically called when the fragment is created and when filters change.
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
                }
            }
        });
    }

    /**
     * Updates the status of an entrant to CANCELLED in Firestore and removes them
     * from the current list. This method is only available for unenrolled entrants.
     * Sends a cancellation notification and triggers sampling of a new entrant after deletion.
     *
     * @param entrant The WaitlistEntry object representing the entrant to be canceled
     */
    private void deleteEntrantFromFirebase(WaitlistEntry entrant) {
        repository.updateEntrantStatus(eventId, entrant.getId(), WaitlistStatus.CANCELLED,
                new RepositoryCallback<WaitlistEntry>() {
                    /**
                     * Callback method that handles the result of the status update operation.
                     * Removes the entrant from the local list, sends notification, and triggers
                     * new entrant sampling on success.
                     *
                     * @param updatedEntry The updated WaitlistEntry object, or null if error
                     * @param error The exception that occurred during the update, or null if successful
                     */
                    @Override
                    public void onComplete(WaitlistEntry updatedEntry, Exception error) {
                        if (error == null) {
                            String userId = entrant.getId();
                            if (userId != null && !userId.isEmpty()) {
                                NotificationsManager.sendInviteCancelled(requireContext(), eventId, userId);
                            }
                            entrantsList.remove(entrantPosition);
                            entrantPosition = -1;
                            adapter.notifyDataSetChanged();
                            sampleManager.sampleSingleEntrantAfterDeletion(new SampleEntrantsManager.SamplingCallback() {
                                /**
                                 * Handles completion of entrant sampling operation.
                                 * Refreshes Firebase data on success.
                                 *
                                 * @param error Exception from sampling operation, or null if successful
                                 */
                                @Override
                                public void onComplete(Exception error) {
                                    if (error == null) {
                                        loadDataFromFirebase();
                                    }
                                }
                            });
                        }
                    }
                });
    }
}

package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.lotteryeventsystem.data.FirebaseWaitlistRepository;
import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a filtered list of event entrants based on their status.
 * This fragment shows different categories of entrants (Canceled, Enrolled, Unenrolled)
 * and provides functionality to manage them, including canceling unenrolled entrants.
 *
 * @author Emily Lan
 * @version 1.0
 * @see OrganizerEntrantListFragment
 * @see WaitlistEntry
 * @see WaitlistStatus
 * @see FirebaseWaitlistRepository
 */
public class EntrantListFragment extends Fragment {
    private ListView listView;
    private TextView tvTitle;
    private ImageButton btnBack;
    private Button btnDeleteSelectedEntrant;
    private int entrantPosition = -1;
    private WaitlistEntryAdapter adapter;
    private final FirebaseWaitlistRepository repository = new FirebaseWaitlistRepository();
    private ArrayList<WaitlistEntry> entrantsList = new ArrayList<>();
    private String eventId;
    private String listType;

    /**
     * Method to create a new instance of EntrantListFragment with required parameters.
     *
     * @param eventId The Firestore document ID of the event
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
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
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
        btnDeleteSelectedEntrant = view.findViewById(R.id.btnDeleteSelectedEntrant);
        adapter = new WaitlistEntryAdapter(requireContext(), entrantsList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if ("canceled".equals(listType)) {
            tvTitle.setText("Canceled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
        } else if ("unenrolled".equals(listType)) {
            tvTitle.setText("Unenrolled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText("Enrolled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up all click listeners for the fragment's interactive elements.
     * Includes back navigation, item selection, and entrant deletion functionality.
     *
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles back button click to return to the previous fragment.
             * Uses the fragment manager's back stack for navigation.
             *
             * @param v The clicked view (back button)
             */
            @Override
            public void onClick(View v) {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
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

        repository.getEntrantsByStatus(eventId, statusesToLoad, new RepositoryCallback<List<WaitlistEntry>>() {
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
                            // Success - remove from current list and refresh
                            entrantsList.remove(entrantPosition);
                            entrantPosition = -1;
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
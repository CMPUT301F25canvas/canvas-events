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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotteryeventsystem.data.FirebaseWaitlistRepository;
import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;

import java.util.ArrayList;
import java.util.List;

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

    // Factory method to create fragment with event ID and list type
    public static EntrantListFragment newInstance(String eventId, String listType) {
        EntrantListFragment fragment = new EntrantListFragment();
        Bundle args = new Bundle();
        args.putString("EVENT_ID", eventId);
        args.putString("LIST_TYPE", listType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.organizer_view_entrant_list, container, false);

        // Get event ID and list type from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("EVENT_ID");
            listType = getArguments().getString("LIST_TYPE");
        }

        initializeViews(view);
        setupClickListeners();
        loadDataFromFirebase();

        return view;
    }

    private void initializeViews(View view) {
        listView = view.findViewById(R.id.listView);
        tvTitle = view.findViewById(R.id.tvTitle);
        btnBack = view.findViewById(R.id.back_button);
        btnDeleteSelectedEntrant = view.findViewById(R.id.btnDeleteSelectedEntrant);

        adapter = new WaitlistEntryAdapter(requireContext(), entrantsList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Set title based on list type
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

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to previous fragment
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                entrantPosition = position;
            }
        });

        btnDeleteSelectedEntrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entrantPosition != -1) {
                    WaitlistEntry entrantToDelete = entrantsList.get(entrantPosition);
                    deleteEntrantFromFirebase(entrantToDelete);
                }
            }
        });
    }

    private void loadDataFromFirebase() {
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
        }

        repository.getEntrantsByStatus(eventId, statusesToLoad, new RepositoryCallback<List<WaitlistEntry>>() {
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

    private void deleteEntrantFromFirebase(WaitlistEntry entrant) {
        repository.updateEntrantStatus(eventId, entrant.getId(), WaitlistStatus.CANCELLED,
                new RepositoryCallback<WaitlistEntry>() {
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

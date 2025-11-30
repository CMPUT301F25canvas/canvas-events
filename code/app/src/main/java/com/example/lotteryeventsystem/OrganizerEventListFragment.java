package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Displays a list of events created by a user.
 * User is identified by their device number
 */
public class OrganizerEventListFragment extends Fragment {
    private HomeEventAdapter eventAdapter;
    private ArrayList<EventItem> eventsList;

    /**
     * Called to have the OrganizerEventListFragment instantiate its user interface view.
     * This method inflates the fragment's layout, retrieves arguments, and initializes
     * the UI components and data loading.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI, or null
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @androidx.annotation.Nullable ViewGroup container,
                             @androidx.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_event_list, container, false);
    }

    /**
     * Logic for when the OrganizerEventList View was created
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton createEventButton = view.findViewById(R.id.create_event_button);
        ImageButton backButton = view.findViewById(R.id.back_button);

        eventsList = new ArrayList<>();

        eventAdapter = new HomeEventAdapter(item -> {
            Bundle args = new Bundle();
            args.putString("EVENT_ID", item.id);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_organizerEventListFragment_to_organizerEntrantListFragment, args);
        });
        RecyclerView recyclerView = view.findViewById(R.id.events_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(eventAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(users -> {
                    List<String> organized_events = (List<String>) users.get("organized_events");
                    if (organized_events == null || organized_events.isEmpty()) {
                        return;
                    }
                    db.collection("events")
                            .whereIn(FieldPath.documentId(), organized_events).get()
                            .addOnSuccessListener(events_user_organized -> {
                                if (events_user_organized.isEmpty()) {
                                    return;
                                }
                                for (QueryDocumentSnapshot event : events_user_organized) {
                                    EventItem item = EventItem.queryDocumentSnapshotToEventItem(event);
                                    eventsList.add(item);
                                }
                                eventAdapter.submitList(eventsList);
                                eventAdapter.notifyDataSetChanged();
                            });
                });

        // Navigates to the event creation form
        createEventButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_organizerEventListFragment_to_organizerEventCreateFragment);
        });

        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp(); // Navigates back to the previous fragment
        });
    }
}

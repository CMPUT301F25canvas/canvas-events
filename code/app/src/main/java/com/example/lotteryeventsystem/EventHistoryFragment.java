package com.example.lotteryeventsystem;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EventHistoryFragment extends Fragment {

    public EventHistoryFragment() {}

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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_history, container, false);
    }

    /**
     * Once the view is created, we set up listeners for the profile options.
     *
     * @param view The Profile view (fragment_profile.xml)
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        // View stuff
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener( v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.eventHistoryFragment_to_profileFragment);
        });

        ArrayList<EventItem> eventsList = new ArrayList<>();
        HomeEventAdapter adapter = new HomeEventAdapter(item -> {
            Bundle args = new Bundle();
            args.putString(EventDetailFragment.ARG_EVENT_ID, item.id);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_homeFragment_to_eventDetailFragment, args);
        });
        RecyclerView recyclerView = view.findViewById(R.id.events_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Model stuff

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(users -> {
                    List<String> enrolledEvents = (List<String>) users.get("enrolled_events");
                    if (enrolledEvents == null) {
                        return;
                    }
                    db.collection("events")
                            .whereIn(FieldPath.documentId(), enrolledEvents).get()
                            .addOnSuccessListener(events_user_enrolled_in -> {
                                if (events_user_enrolled_in.isEmpty()) {
                                    return;
                                }
                                for (QueryDocumentSnapshot event : events_user_enrolled_in) {
                                    EventItem item = EventItem.queryDocumentSnapshotToEventItem(event);
                                    eventsList.add(item);
                                }
                                adapter.submitList(eventsList);
                                adapter.notifyDataSetChanged();
                            });
                });
    }
}
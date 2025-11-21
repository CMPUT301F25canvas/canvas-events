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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.Nullable;

/**
 * Displays a list of events created by a user.
 * User is identified by their device number
 */
public class OrganizerEventListFragment extends Fragment {
    private ArrayAdapter<Event> eventAdapter;
    private ArrayList<Event> eventsList;
    private EventRepository eventRepository;

    private static ArrayList<Event> cachedItems;
    private String androidDeviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @androidx.annotation.Nullable ViewGroup container,
                             @androidx.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_event_list, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadEvents();
    }

    /**
     * Logic for when the OrganizerEventList View was created
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventRepository = new EventRepository();

        ListView eventListView = view.findViewById(R.id.organizer_event_list);
        Button createEventButton = view.findViewById(R.id.create_event_button);
        ImageButton backButton = view.findViewById(R.id.back_button);

        eventsList = new ArrayList<Event>();
        eventAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventsList);
        eventListView.setAdapter(eventAdapter);

        androidDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        reloadEvents();

        // Navigates to the event creation form
        createEventButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_organizerEventListFragment_to_organizerEventCreateFragment);
        });

        // List View On Click Listener - Passes eventId to the organizerEventListFragment
        eventListView.setOnItemClickListener((parent, itemView, position, id) -> {
            Event event = eventsList.get(position);
            Bundle args = new Bundle();
            args.putString("EVENT_ID", event.getEventID());

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_organizerEventListFragment_to_organizerEntrantListFragment, args);
        });

        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp(); // Navigates back to the previous fragment
        });
    }

    private void reloadEvents() {
        eventsList.clear();

        eventRepository.getEventsByOrganizer(androidDeviceId)
                .addOnSuccessListener(snapshot -> {
                    eventsList.clear();
                    for (var doc : snapshot) {
                        eventsList.add(doc.toObject(Event.class));
                    }
                    cachedItems = new ArrayList<>(eventsList);
                    eventAdapter.notifyDataSetChanged();
                });


    }
}

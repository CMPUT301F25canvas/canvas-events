package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

public class OrganizerEventListFragment extends Fragment {
    private ArrayAdapter<Event> eventAdapter;
    private ArrayList<Event> eventsList;

    private static ArrayList<Event> cachedItems;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @androidx.annotation.Nullable ViewGroup container,
                             @androidx.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_event_list, container, false);
    }


    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView eventListView = view.findViewById(R.id.organizer_event_list);
        Button createEventButton = view.findViewById(R.id.create_event_button);

        eventsList = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventsList);
        eventListView.setAdapter(eventAdapter);

        // Getting the device ID
        String androidDeviceId = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        if (cachedItems != null) {
            eventsList.addAll(cachedItems);
            eventAdapter.notifyDataSetChanged();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String creator_id = doc.getString("creator_id");

                            // Only add events created by this user
                            if (Objects.equals(creator_id, androidDeviceId))
                                eventsList.add(new Event(
                                    doc.getId(), doc.getString("name"), doc.getString("creator_id"),
                                    doc.getString("description"), doc.getString("date"), doc.getString("start_time"),
                                    doc.getString("end_time"), doc.getDouble("entrant_limit")
                            ));
                        }
                        cachedItems = new ArrayList<>(eventsList);
                        eventAdapter.notifyDataSetChanged();
                    });
        }

        // Navigates to the event creation form
        createEventButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_organizerEventListFragment_to_organizerEventCreateFragment);
        });

        // List View On Click Listener
        eventListView.setOnItemClickListener((parent, itemView, position, id) -> {
            Event event = eventsList.get(position);
            Bundle args = new Bundle();

            // TODO: NEED EMILY'S FRAGMENT DETAILS FILE HERE
            args.putString(OrganizerEventDetailFragment.ARG_EVENT_ID, event.getId());

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_organizerEventListFragment_to_organizerEventDetailFragment, args);
        });
    }
}

package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class HomeFragment extends Fragment {
    private ArrayAdapter<EventItem> adapter;
    private ArrayList<EventItem> itemsList;

    private static ArrayList<EventItem> cachedItems;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchView = view.findViewById(R.id.search_view);
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        ListView listView = view.findViewById(R.id.list_view);

        itemsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, itemsList);
        listView.setAdapter(adapter);

        if (cachedItems != null) {
            itemsList.addAll(cachedItems);
            adapter.notifyDataSetChanged();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String name = doc.getString("name");
                            if (name != null) itemsList.add(new EventItem(doc.getId(), name));
                        }
                        cachedItems = new ArrayList<>(itemsList);
                        adapter.notifyDataSetChanged();
                    });
        }

        searchView.setOnClickListener( v -> {
            Toast.makeText(getContext(), "Events search returned", Toast.LENGTH_SHORT).show();
            // TODO: all the search logic
        });

        filterButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Filter Clicked!", Toast.LENGTH_SHORT).show();
            // TODO: all the dialog and filtering
        });

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            EventItem event = itemsList.get(position);
            Bundle args = new Bundle();
            args.putString("event_name", event.name);
            args.putString("event_id", event.id);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_homeFragment_to_eventDetailFragment, args);
        });


        //MISSING STUFF
    }

}

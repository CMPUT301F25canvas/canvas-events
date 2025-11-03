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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class HomeFragment extends Fragment {
    private ArrayAdapter<String> adapter;
    private ArrayList<String> itemsList;


    public HomeFragment() {}

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

        // ✅ Initialize your list BEFORE creating the adapter
        itemsList = new ArrayList<>(Arrays.asList(
                "Event A", "Event B", "Event C", "Event D", "Event E"
        ));

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, itemsList);
        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        filterButton.setOnClickListener( v -> {
            Toast.makeText(getContext(), "Filter Clicked!", Toast.LENGTH_SHORT).show();
            // TODO: all the dialog and filtering
        });

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            String eventName = itemsList.get(position);

            // Get the NavController from this fragment
            NavController navController = Navigation.findNavController(requireView());

            // Prepare arguments to pass to EventDetailFragment
            Bundle args = new Bundle();
            args.putString("event_name", eventName);

            // Navigate using the action defined in nav_graph.xml
            navController.navigate(R.id.action_homeFragment_to_eventDetailFragment, args);
        });


        //MISSING STUFF
    }

}

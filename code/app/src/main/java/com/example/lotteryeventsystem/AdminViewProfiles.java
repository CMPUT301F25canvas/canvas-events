package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdminViewProfiles extends Fragment {
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<ScanOptions> scanLauncher;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> itemsList;
    private static ArrayList<String> cachedItems;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.home_label);
        title.setText("Profiles");
        ImageButton backArrow = view.findViewById(R.id.back_image_button);
        ImageButton scanButton = view.findViewById(R.id.button_scan_qr);
        scanButton.setVisibility(INVISIBLE);
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
            db.collection("users")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String docId = doc.getId();
                            itemsList.add(docId);
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
            String profileId = itemsList.get(position);
            title.setText(profileId);
//            Bundle args = new Bundle();
//            args.putString(EventDetailFragment.ARG_EVENT_ID, event.id);
//
//            NavController navController = Navigation.findNavController(requireView());
//            navController.navigate(R.id.action_homeFragment_to_eventDetailFragment, args);
        });

        backArrow.setOnClickListener(v-> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
    }
}

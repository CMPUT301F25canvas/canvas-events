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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdminViewEvents extends Fragment {
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<ScanOptions> scanLauncher;

    private ArrayAdapter<EventItem> adapter;
    private ArrayList<EventItem> itemsList;

    private static ArrayList<EventItem> cachedItems;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_list_layout, container, false);
    }

    /**
     * When a list item is clicked on, it opens the fragment displays the event details
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.home_label);
        title.setText("Events");
        ImageButton backArrow = view.findViewById(R.id.back_image_button);
        ImageButton scanButton = view.findViewById(R.id.button_scan_qr);
        scanButton.setVisibility(INVISIBLE);        // Removes the scan option for admin
        SearchView searchView = view.findViewById(R.id.search_view);
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        ListView listView = view.findViewById(R.id.list_view);

        itemsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, itemsList);
        listView.setAdapter(adapter);

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
            title.setText(event.id);
            Bundle args = new Bundle();
            args.putString(EventDetailFragment.ARG_EVENT_ID, event.id);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_adminEventFragment_to_eventDetailFragment, args);
        });

        backArrow.setOnClickListener(v-> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
    }
}

package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;

import android.app.AlertDialog;
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
import java.util.Map;

public class AdminViewProfiles extends Fragment {
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<ScanOptions> scanLauncher;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> itemsList;
    private static ArrayList<String> cachedItems;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_list_layout, container, false);
    }

    /**
     * When a user ID is clicked, it shows a popup that displays user information and gives the admin an option
     * to delete the user profile.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
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
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(profileId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data == null || data.isEmpty()) {
                            Toast.makeText(requireContext(), "Error: No data available for this user", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        StringBuilder info = new StringBuilder();
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            info.append(entry.getKey())
                                    .append(": ").append(entry.getValue()).append("\n");
                        }
                        new AlertDialog.Builder(requireContext()).setTitle("User Profile")
                                .setMessage(info.toString())
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    new AlertDialog.Builder(requireContext())
                                            .setTitle("Confirm Deletion")
                                            .setMessage("Are you sure you want to delete this user?")
                                            .setPositiveButton("Yes", (confirmDialog, cWhich) -> {
                                                FireBaseDeleteFuncs.deleteUserProfile(db, profileId, requireContext());
                                                itemsList.remove(position);
                                                adapter.notifyDataSetChanged();
                                            })
                                            .show();
                                })
                                .show();
                    } else {
                        Toast.makeText(requireContext(), "Error: Profile doesn't exist", Toast.LENGTH_SHORT).show();
                    }
                });
        });

        backArrow.setOnClickListener(v-> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
    }
}

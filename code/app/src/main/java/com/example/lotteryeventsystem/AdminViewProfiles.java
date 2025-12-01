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

    private ArrayList<String> idList;
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

        idList = new ArrayList<>();
        itemsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, itemsList);
        listView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String docId = doc.getId();
                        String userName = doc.getString("name");
                        if (userName != null && !userName.trim().isEmpty()) {
                            itemsList.add(userName);
                            idList.add(docId);
                        }
                        else {
                            itemsList.add(docId);
                            idList.add(docId);
                        }

                    }
                    cachedItems = new ArrayList<>(itemsList);
                    adapter.notifyDataSetChanged();
                });

        searchView.setOnClickListener( v -> {
            Toast.makeText(getContext(), "Events search returned", Toast.LENGTH_SHORT).show();
            // TODO: all the search logic
        });

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            String profileId = idList.get(position);
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View dialogView = inflater.inflate(R.layout.dialog_user_profile, null);

            TextView textName = dialogView.findViewById(R.id.textUserName);
            TextView textEnrolled = dialogView.findViewById(R.id.textEnrolled);
            TextView textOrganized = dialogView.findViewById(R.id.textOrganized);
            db.collection("users").document(profileId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data == null || data.isEmpty()) {
                            Toast.makeText(requireContext(), "Error: No data available for this user", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String userName = data.get("name") != null ? data.get("name").toString() : "N/A";
                        Object enrolledObj = data.get("enrolled_events");
                        Object organizedObj = data.get("organized_events");
                        String enrolledStr = (enrolledObj instanceof java.util.List)
                                ? String.join("\n• ", (java.util.List<String>) enrolledObj)
                                : "None";
                        String organizedStr = (organizedObj instanceof java.util.List)
                                ? String.join("\n• ", (java.util.List<String>) organizedObj)
                                : "None";
                        textName.setText("Name: " + userName);
                        textEnrolled.setText("Enrolled Events:\n• " + enrolledStr);
                        textOrganized.setText("Organized Events:\n• " + organizedStr);
                        new AlertDialog.Builder(requireContext()).setTitle("User Profile")
                                .setView(dialogView)
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    new AlertDialog.Builder(requireContext())
                                            .setTitle("Confirm Deletion")
                                            .setMessage("Are you sure you want to delete this user?")
                                            .setPositiveButton("Yes", (confirmDialog, cWhich) -> {
                                                FireBaseDeleteFuncs.deleteUserProfile(db, profileId, requireContext());
                                                itemsList.remove(position);
                                                adapter.notifyDataSetChanged();
                                            })
                                            .setNegativeButton("Cancel", null)
                                            .show();
                                })
                                .setNegativeButton("Close", null)
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

package com.example.lotteryeventsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotteryeventsystem.util.EventLinkParser;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Home screen for entrants.
 * Lets them scan a QR-code or click and event to jump into an event page.
 */
public class HomeFragment extends Fragment {
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<ScanOptions> scanLauncher;

    private ArrayAdapter<EventItem> adapter;
    private ArrayList<EventItem> itemsList;

    private static ArrayList<EventItem> cachedItems;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        if (((MainActivity) requireActivity()).getAdmin()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_profileFragment_to_adminHomeFragment);
        }
        return view;
    }


    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton scanButton = view.findViewById(R.id.button_scan_qr);
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

        registerPermissionLauncher();
        registerScanLauncher();
        scanButton.setOnClickListener(v -> launchScanner());

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            EventItem event = itemsList.get(position);
            Bundle args = new Bundle();
            args.putString(EventDetailFragment.ARG_EVENT_ID, event.id);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_homeFragment_to_eventDetailFragment, args);
        });


    }

    /**
     * Launches the QR-code scanner.
     * If it has permission start scanning
     * Otherwise, get permission.
     */
    private void launchScanner() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Starts the QR code scanning process with configured options.
     */
    private void startScanning() {
        ScanOptions options = new ScanOptions();
        options.setPrompt(getString(R.string.scan_prompt));
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        scanLauncher.launch(options);
    }

    /**
     * Registers a permission launcher to handle the result of the camera permission request.
     */
    private void registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startScanning();
                    } else if (getContext() != null) {
                        Toast.makeText(getContext(),
                                R.string.scan_camera_permission_denied,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Registers a launcher to handle the result of a QR code scan.
     */
    private void registerScanLauncher() {
        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result == null || result.getContents() == null) {
                showToast(R.string.scan_cancelled_message);
                return;
            }
            handleScanResult(result.getContents());
        });
    }

    /**
     * Handles the result of a QR code scan.
     * Parses the scanned content to extract an event ID and navigates to the EventDetailFragment.
     * @param contents the scanned QR code contents
     */
    private void handleScanResult(String contents) {
        String eventId = EventLinkParser.parseEventId(contents);
        if (eventId == null || eventId.isEmpty()) {
            showToast(R.string.scan_unknown_event_message);
            return;
        }
        Bundle args = new Bundle();
        args.putString(EventDetailFragment.ARG_EVENT_ID, eventId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_homeFragment_to_eventDetailFragment, args);
    }

    /**
     * Displays a short toast message using the provided string.
     * @param messageRes
     */
    private void showToast(int messageRes) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), messageRes, Toast.LENGTH_SHORT).show();
    }
}

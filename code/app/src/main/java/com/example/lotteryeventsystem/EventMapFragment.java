package com.example.lotteryeventsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

public class EventMapFragment extends Fragment implements OnMapReadyCallback{
    MapView mapView;
    GoogleMap map;
    String eventID;

    ImageButton backButton;
    EventRepository eventRepository;

    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_event_map, container, false);

        eventRepository = new EventRepository();

        // Requests for location permissions
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocationPermissions();


        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);

        // Get location of Organizer
        defaultMapLocation();

        // Add markers to the map
        addEntrantMarkers(eventID);
    }

    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.getMapAsync(this);

        // Load the event - use current location of organizer?
        Bundle args = getArguments();
        if (args != null) {
            eventID = args.getString("EVENT_ID");
        }

        mapView = view.findViewById(R.id.mapView);


        // Back Button
        backButton = view.findViewById(R.id.back_button);
        setBackButton(view);
    }

    /**
     * Requests for location permissions
     */
    private void requestLocationPermissions() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {

                            boolean fineLocationGranted = Boolean.TRUE.equals(
                                    result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                            boolean coarseLocationGranted = Boolean.TRUE.equals(
                                    result.get(Manifest.permission.ACCESS_COARSE_LOCATION));


                            if (fineLocationGranted) {
                                defaultMapLocation();
                            }
                        }
                );

        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void defaultMapLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Default location if no location
                            if (location == null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 0));
                            } else {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 5));
                            }
                        }
                    });
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 0));
        }
    }

    private void addEntrantMarkers(String eventID) {
        // Get the waiting list for the event
        Task<QuerySnapshot> waitingList = eventRepository.getEntrants(eventID);
        waitingList.addOnSuccessListener(queryDocumentSnapshots -> {
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                String userID = queryDocumentSnapshots.getDocuments().get(i).getId();
                Double latitude = queryDocumentSnapshots.getDocuments().get(i).getDouble("latitude");
                Double longitude = queryDocumentSnapshots.getDocuments().get(i).getDouble("longitude");

                if (longitude == null || latitude == null) {
                    continue;
                }

                // Add marker to map
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(userID));
            }
        });


    }


    // Map lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    // Setting up buttons
    public void setBackButton(View v) {
        backButton.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });
    }



}

package com.example.lotteryeventsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotteryeventsystem.util.EventLinkParser;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Home screen for entrants. Lets them scan a QR code to jump into an event.
 */
public class HomeFragment extends Fragment {
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<ScanOptions> scanLauncher;
    private Button scanButton;

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPermissionLauncher();
        registerScanLauncher();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanButton = view.findViewById(R.id.button_scan_qr);
        scanButton.setOnClickListener(v -> launchScanner());
    }

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

    private void registerScanLauncher() {
        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result == null || result.getContents() == null) {
                showToast(R.string.scan_cancelled_message);
                return;
            }
            handleScanResult(result.getContents());
        });
    }

    private void launchScanner() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startScanning() {
        ScanOptions options = new ScanOptions();
        options.setPrompt(getString(R.string.scan_prompt));
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        scanLauncher.launch(options);
    }

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

    private void showToast(int messageRes) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), messageRes, Toast.LENGTH_SHORT).show();
    }
}

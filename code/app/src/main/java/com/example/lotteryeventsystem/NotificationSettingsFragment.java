package com.example.lotteryeventsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight in-app notification preferences.
 */
public class NotificationSettingsFragment extends Fragment {
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_ALLOW_PUSH = "allow_push";
    private static final String KEY_ORGANIZER = "organizer";
    private static final String KEY_ADMIN = "admin";
    private static final String KEY_MARKETING = "marketing";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton backButton = view.findViewById(R.id.settings_back);
        SwitchMaterial allowPush = view.findViewById(R.id.switch_allow_push);
        SwitchMaterial organizerMessages = view.findViewById(R.id.switch_organizer);
        SwitchMaterial adminMessages = view.findViewById(R.id.switch_admin);
        SwitchMaterial marketing = view.findViewById(R.id.switch_marketing);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        allowPush.setChecked(prefs.getBoolean(KEY_ALLOW_PUSH, true));
        organizerMessages.setChecked(prefs.getBoolean(KEY_ORGANIZER, true));
        adminMessages.setChecked(prefs.getBoolean(KEY_ADMIN, true));
        marketing.setChecked(prefs.getBoolean(KEY_MARKETING, false));

        backButton.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        allowPush.setOnCheckedChangeListener((buttonView, isChecked) -> saveAndSync(prefs, KEY_ALLOW_PUSH, isChecked));
        organizerMessages.setOnCheckedChangeListener((buttonView, isChecked) -> saveAndSync(prefs, KEY_ORGANIZER, isChecked));
        adminMessages.setOnCheckedChangeListener((buttonView, isChecked) -> saveAndSync(prefs, KEY_ADMIN, isChecked));
        marketing.setOnCheckedChangeListener((buttonView, isChecked) -> saveAndSync(prefs, KEY_MARKETING, isChecked));
    }

    private void saveAndSync(SharedPreferences prefs, String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        syncToFirestore(prefs);
    }

    private void syncToFirestore(SharedPreferences prefs) {
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null || deviceId.isEmpty()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put(KEY_ALLOW_PUSH, prefs.getBoolean(KEY_ALLOW_PUSH, true));
        payload.put(KEY_ORGANIZER, prefs.getBoolean(KEY_ORGANIZER, true));
        payload.put(KEY_ADMIN, prefs.getBoolean(KEY_ADMIN, true));
        payload.put(KEY_MARKETING, prefs.getBoolean(KEY_MARKETING, false));
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(deviceId)
                .collection("preferences")
                .document("notifications")
                .set(payload, SetOptions.merge());
    }
}

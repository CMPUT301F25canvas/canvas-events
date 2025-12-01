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

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        allowPush.setChecked(prefs.getBoolean(KEY_ALLOW_PUSH, true));
        backButton.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        allowPush.setOnCheckedChangeListener((buttonView, isChecked) -> saveAndSync(prefs, KEY_ALLOW_PUSH, isChecked));
    }

    /**
     * Saves the updated preference locally and triggers sync to Firestore.
     *
     * @param prefs The SharedPreferences instance storing notification settings.
     * @param key The preference key being updated.
     * @param value The new boolean value chosen by the user.
     */
    private void saveAndSync(SharedPreferences prefs, String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        syncToFirestore(prefs);
    }

    /**
     * Writes the current notification preference to Firestore under the
     * logged-in device's user document.
     */
    private void syncToFirestore(SharedPreferences prefs) {
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null || deviceId.isEmpty()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put(KEY_ALLOW_PUSH, prefs.getBoolean(KEY_ALLOW_PUSH, true));
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(deviceId)
                .collection("preferences")
                .document("notifications")
                .set(payload, SetOptions.merge());
    }
}

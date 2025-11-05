package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminHomeFragment extends Fragment {

    public AdminHomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        View eventsBtn = view.findViewById(R.id.admin_events_button);
        View profilesBtn = view.findViewById(R.id.admin_profiles_button);
        View imagesBtn = view.findViewById(R.id.admin_images_button);
        View notifBtn = view.findViewById(R.id.admin_notif_logs_button);
        eventsBtn.setOnClickListener(v-> NavHostFragment.findNavController(AdminHomeFragment.this)
                .navigate(R.id.to_adminProfileViewFragment));
        return view;
    }
}

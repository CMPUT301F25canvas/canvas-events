package com.example.lotteryeventsystem;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class AdminHomeFragment extends Fragment {

    public AdminHomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.admin_home_fragment, container, false);
        if (!((MainActivity) requireActivity()).getAdmin()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_adminHomeFragment_to_homeFragment);
        }
        return view;
    }

    /**
     * Sets up the admin home page, and activates the admin options.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get references to your views
        TextView adminTitle = view.findViewById(R.id.adminTitle);
        Button eventsBtn = view.findViewById(R.id.admin_events_button);
        Button profilesBtn = view.findViewById(R.id.admin_profiles_button);
        Button imagesBtn = view.findViewById(R.id.admin_images_button);

        eventsBtn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_adminHomeFragment_to_adminEventsFragment);
        });

        profilesBtn.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_adminHomeFragment_to_adminProfilesFragment);
        });

        imagesBtn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_adminHomeFragment_to_adminPosterFragment);
        });

    }
}

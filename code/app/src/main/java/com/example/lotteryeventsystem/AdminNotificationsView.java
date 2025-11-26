package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminNotificationsView extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        if (!((MainActivity) requireActivity()).getAdmin()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_adminHomeFragment_to_homeFragment);
            return;
        }
        RecyclerView notificationList = view.findViewById(R.id.notification_list);
        TextView emptyText = view.findViewById(R.id.notification_empty);
        ProgressBar progressBar = view.findViewById(R.id.notification_progress);
        ImageButton settingsButton = view.findViewById(R.id.button_notification_settings);
        notificationList.setLayoutManager(new LinearLayoutManager(requireContext()));
        progressBar.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
    }
}

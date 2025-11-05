package com.example.lotteryeventsystem;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class AdminProfileViewFragment extends Fragment {

    public AdminProfileViewFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_search_view, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titleText = view.findViewById(R.id.list_title);
        // Back button listener
        ImageButton btnBack = view.findViewById(R.id.back_from_admin_events);
        titleText.setText("Profiles");
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp()
        );
    }
}

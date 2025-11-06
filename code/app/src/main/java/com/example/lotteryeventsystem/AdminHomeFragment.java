package com.example.lotteryeventsystem;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

        // Get references to your views
        TextView adminTitle = view.findViewById(R.id.adminTitle);
        Button eventsBtn = view.findViewById(R.id.admin_events_button);
        Button profilesBtn = view.findViewById(R.id.admin_profiles_button);
        Button imagesBtn = view.findViewById(R.id.admin_images_button);

        eventsBtn.setOnClickListener(v ->
                adminTitle.setText("Events button clicked"));

        profilesBtn.setOnClickListener(v ->
                adminTitle.setText("Profiles button clicked"));

        imagesBtn.setOnClickListener(v ->
                adminTitle.setText("Images button clicked"));

        return view;
    }
}

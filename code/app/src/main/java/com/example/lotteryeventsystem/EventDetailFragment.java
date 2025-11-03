package com.example.lotteryeventsystem;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.w3c.dom.Text;

public class EventDetailFragment extends Fragment {
    public EventDetailFragment() {};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp(); // Navigates back to the previous fragment
        });


        TextView nameText = view.findViewById(R.id.header_title);
        TextView descText = view.findViewById(R.id.event_description);
        TextView dateText = view.findViewById(R.id.event_date);
        TextView startText = view.findViewById(R.id.event_start_time);
        TextView endText = view.findViewById(R.id.event_end_time);
        ImageView poster;

        if (getArguments() != null) {
            nameText.setText(getArguments().getString("event_name", "Event Name"));
            descText.setText(getArguments().getString("event_description", "No Description"));
            dateText.setText("Date: " + getArguments().getString("event_date", "MM/DD/YYYY"));
            startText.setText("Start: " + getArguments().getString("event_start_time", "HH:MM"));
            endText.setText("   End: " + getArguments().getString("event_end_time", "HH:MM"));
        }

    }
}

package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

public class ProfileFragment extends Fragment {
    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        TextView PersonalInformation = view.findViewById(R.id.personal_info);
        TextView MyCreatedEvents = view.findViewById(R.id.my_created_events);
        TextView DeleteProfile = view.findViewById(R.id.delete_profile);

        PersonalInformation.setOnClickListener( v -> {
            // TODO: Everything
        });

        MyCreatedEvents.setOnClickListener( v -> {
            // TODO: Everything
        });

        DeleteProfile.setOnClickListener( v -> {
            // TODO: Everything
        });
    }


}

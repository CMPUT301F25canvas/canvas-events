package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class HomeFragment extends Fragment {
    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        View admin_button = view.findViewById(R.id.admin_home);
        admin_button.setOnClickListener(v-> NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_adminHomeFragment));
        return view;
    }


}

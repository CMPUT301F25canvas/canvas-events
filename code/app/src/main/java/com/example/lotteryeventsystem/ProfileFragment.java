package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
        TextView adminLogin = view.findViewById(R.id.admin_login);
        if (((MainActivity) requireActivity()).getAdmin()){
            adminLogin.setText("Logout as Admin");
        }
        else {
            adminLogin.setText("Login as Admin");
        }
        adminLogin.setOnClickListener(v-> {
            if (adminLogin.getText().equals("Login as Admin")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Admin Login");

                EditText input = new EditText(getContext());
                input.setHint("Enter Admin code");
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Confirm", (dialog, which) -> {
                    String adminId = input.getText().toString().trim();
                    if (adminId.isEmpty()) {
                        Toast.makeText(getContext(), "Admin ID cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        if (adminId.equals("canvas")) {
                            ((MainActivity) requireActivity()).setAdmin(true);
                            adminLogin.setText("Logout as Admin");
                            Toast.makeText(getContext(), "Logged in as Admin", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            } else {
                ((MainActivity) requireActivity()).setAdmin(false);
                adminLogin.setText("Login as Admin");
                Toast.makeText(getContext(), "Logged out as Admin", Toast.LENGTH_SHORT).show();
            }
        });

        PersonalInformation.setOnClickListener( v -> {
            // TODO: Everything
        });

        MyCreatedEvents.setOnClickListener( v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_profileFragment_to_organizerEventListFragment);
        });

        DeleteProfile.setOnClickListener( v -> {
            // TODO: Everything
        });
    }


}

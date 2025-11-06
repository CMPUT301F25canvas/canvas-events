package com.example.lotteryeventsystem;

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

public class ProfileFragment extends Fragment {
    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView adminLogin = view.findViewById(R.id.admin_login);
        adminLogin.setOnClickListener(v->showAdminLoginDialog());
        return view;
    }

    public void showAdminLoginDialog(){
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
                }
                Toast.makeText(getContext(), "Logged in as Admin", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the popup
        builder.show();
    }
}

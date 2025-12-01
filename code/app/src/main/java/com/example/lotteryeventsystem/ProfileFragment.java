/**
 * The Profile page, one of three main fragments in the MainActivity.
 * Model and Controller code are mixed in OnViewCreated and helper functions,
 * but View is specified in onCreateView
 */

package com.example.lotteryeventsystem;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Combined MVC code for the Profile page
 *
 * @author Ethan Kinch
 * @author Other Author
 */
public class ProfileFragment extends Fragment {
    /**
     * Empty required constructor
     */
    public ProfileFragment() {}

    /**
     * Called to have the fragment instantiate its user interface view.
     * This method inflates the fragment's layout, retrieves arguments, and initializes
     * the UI components and data loading.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI, or null
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Once the view is created, we set up listeners for the profile options.
     *
     * @param view The Profile view (fragment_profile.xml)
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        TextView PersonalInformation = view.findViewById(R.id.personal_info);
        TextView EventHistory = view.findViewById(R.id.event_history);
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
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.profileFragment_to_personalInfoFragment);
        });

        EventHistory.setOnClickListener( v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_profileFragment_to_eventHistoryFragment);
        });

        MyCreatedEvents.setOnClickListener( v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_profileFragment_to_organizerEventListFragment);
        });

        DeleteProfile.setOnClickListener( v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setMessage("Are you sure you want to PERMANENTLY DELETE your account?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteProfile();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        });
    }

    /**
     * Deletes the user's profile from firebase. Does not delete their events.
     */
    private void deleteProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

        // https://firebase.google.com/docs/firestore/manage-data/delete-data#java
        db.collection("users").document(deviceId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Account Deleted", Toast.LENGTH_SHORT).show();
                        Log.d(this.toString(), "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Account Deletion Failed", Toast.LENGTH_SHORT).show();
                        Log.w(this.toString(), "Error deleting document", e);
                    }
                });
    }

}

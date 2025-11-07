package com.example.lotteryeventsystem;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

/**
 * Combined MVC code for the Personal Info page
 *
 * @author Ethan Kinch
 */
public class PersonalInfoFragment extends Fragment {
    /**
     * Empty required constructor
     */
    public PersonalInfoFragment() {
    }

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
    public View onCreateView(@Nullable LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_info, container, false);
    }

    /**
     * Once the view is created, we instantiate the three user fields from the database.
     * We also set up listeners such that when a field is edited, it is sent to the database
     *
     * @param view The PersonalInfo view (fragment_personal_info.xml)
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        // View stuff
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener( v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.personalInfoFragment_to_profileFragment);
        });

        // Model stuff
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        EditText name = view.findViewById(R.id.name);
        EditText email = view.findViewById(R.id.email);
        EditText phoneNumber = view.findViewById(R.id.phone_number);

        db.collection("users").document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String tmp;
                            if ((tmp = document.getString("name")) != null) {
                                name.setText(tmp);
                            }
                            if ((tmp = document.getString("email")) != null) {
                                email.setText(tmp);
                            }
                            if ((tmp = document.getString("phone_number")) != null) {
                                phoneNumber.setText(tmp);
                            }
                            Log.d(this.toString(), "Found user");
                        } else {
                            Log.d(this.toString(), "User doesn't exist");
                        }
                    } else {
                        Log.d(this.toString(), "Some database error");
                    }
                });

        name.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = ((EditText)v).getText().toString();
                db.collection("users").document(deviceId).update("name", text);
            }
        });

        email.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = ((EditText)v).getText().toString();
                db.collection("users").document(deviceId).update("email", text);
            }
        });

        phoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = ((EditText)v).getText().toString();
                db.collection("users").document(deviceId).update("phone_number", text);
            }
        });


    }


}
package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.lotteryeventsystem.data.WaitlistRepository;
import com.example.lotteryeventsystem.di.ServiceLocator;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationDetailFragment extends Fragment {

    private String type;
    private String body;
    private String title;
    private String eventId;
    private String response;     // <-- NEW (None / Accepted / Rejected)

    private MaterialButton acceptBtn, declineBtn;
    private ProgressBar progress;
    private FirebaseFirestore db;
    private SampleEntrantsManager sampleManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_detail, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Get arguments passed from NotificationFragment
        if (getArguments() != null) {
            type = getArguments().getString("type");
            body = getArguments().getString("body");
            title = getArguments().getString("title");
            eventId = getArguments().getString("eventId");
            response = getArguments().getString("response", "None");
        }

        if (eventId != null && !eventId.isEmpty()) {
            WaitlistRepository waitlistRepository = ServiceLocator.provideWaitlistRepository();
            sampleManager = new SampleEntrantsManager(requireContext(), waitlistRepository, eventId);
        }

        // Views
        ImageButton back = v.findViewById(R.id.detail_back);
        TextView titleView = v.findViewById(R.id.detail_title);
        TextView bodyView = v.findViewById(R.id.detail_body);
        TextView statusView = v.findViewById(R.id.detail_status);
        TextView eventNameView = v.findViewById(R.id.detail_event_name);

        acceptBtn = v.findViewById(R.id.detail_accept);
        declineBtn = v.findViewById(R.id.detail_decline);
        progress = v.findViewById(R.id.detail_progress);

        // Populate UI
        titleView.setText("Notification Details");
        bodyView.setText(body);
        eventNameView.setText(title);

        // STATUS TEXT (Pending, Accepted, Rejected, etc)
        if (!"selected_notification".equals(type)) {
            // For all other notifications use Seen/New logic
            statusView.setText("Info");
        } else {
            switch (response) {
                case "Accepted":
                    statusView.setText("Accepted");
                    break;
                case "Rejected":
                    statusView.setText("Rejected");
                    break;
                case "None":
                default:
                    statusView.setText("Pending");
                    break;
            }
        }

        // Back button
        back.setOnClickListener(v2 ->
                Navigation.findNavController(requireView()).navigateUp()
        );

        // Show Accept/Reject ONLY for selected_notification + response = None
        boolean showActions =
                type.equals("selected_notification") &&
                        (response == null || response.equals("None"));

        acceptBtn.setVisibility(showActions ? View.VISIBLE : View.GONE);
        declineBtn.setVisibility(showActions ? View.VISIBLE : View.GONE);

        // Click actions
        acceptBtn.setOnClickListener(v2 -> updateResponse("Accepted"));
        declineBtn.setOnClickListener(v2 -> {
            updateResponse("Rejected");
            // Call sampleSingleEntrantAfterDeletion when decline button is clicked
            if (sampleManager != null) {
                sampleManager.sampleSingleEntrantAfterDeletion(new SampleEntrantsManager.SamplingCallback() {
                    @Override
                    public void onComplete(Exception error) {
                        // Handle completion Silently
                    }
                });
            }
        });
    }

    /**
     * Writes Accept/Reject back to Firestore correctly.
     * @param newResponse The user's choice ("Accepted" or "Rejected").
     */
    private void updateResponse(String newResponse) {

        setLoading(true);

        String userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (userId == null) {
            Toast.makeText(getContext(), "User ID missing", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        db.collection("notifications")
                .document(type)
                .collection(eventId)
                .document(userId)
                .update("response", newResponse)    // <-- MUST BE "response"
                .addOnSuccessListener(a -> {
                    if (newResponse.equals("Accepted")) {
                        NotificationsManager.sendInviteAccepted(getContext(), eventId, userId);
                    } else {
                        NotificationsManager.sendInviteRejected(getContext(), eventId, userId);
                    }
                    String eventPath = "events/" + eventId + "/waitlist/" + userId;

                    db.document(eventPath)
                            .update("status", newResponse.equals("Accepted") ? "CONFIRMED" : "DECLINED");
                    setLoading(false);
                    acceptBtn.setVisibility(View.GONE);
                    declineBtn.setVisibility(View.GONE);

                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                });
    }

    /**
     * Controls progress bar visibility and button disabling during Firestore operations.
     *
     * @param loading true: show progress bar and lock buttons
     *                false: hide progress bar and unlock UI
     */
    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        acceptBtn.setEnabled(!loading);
        declineBtn.setEnabled(!loading);
    }
}

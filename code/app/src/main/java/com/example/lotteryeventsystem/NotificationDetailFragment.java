package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.lotteryeventsystem.data.NotificationRepository;
import com.example.lotteryeventsystem.data.WaitlistRepository;
import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.NotificationStatus;
import com.example.lotteryeventsystem.model.WaitlistStatus;
import com.google.android.material.button.MaterialButton;

/**
 * Detailed invite/notification view with Accept/Decline actions.
 */
public class NotificationDetailFragment extends Fragment {
    private final WaitlistRepository waitlistRepository = ServiceLocator.provideWaitlistRepository();
    private final NotificationRepository notificationRepository = ServiceLocator.provideNotificationRepository();

    private TextView statusView;
    private TextView bodyView;
    private TextView eventNameView;
    private TextView eventTimeView;
    private TextView respondByView;
    private ProgressBar progressBar;
    private MaterialButton acceptButton;
    private MaterialButton declineButton;
    private MaterialButton viewEventButton;
    private MaterialButton myEventsButton;

    private String notificationId;
    private String eventId;
    private String waitlistEntryId;
    private String eventName;
    private String body;
    private String messageType;
    private NotificationStatus currentStatus = NotificationStatus.UNREAD;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statusView = view.findViewById(R.id.detail_status);
        bodyView = view.findViewById(R.id.detail_body);
        eventNameView = view.findViewById(R.id.detail_event_name);
        eventTimeView = view.findViewById(R.id.detail_event_time);
        respondByView = view.findViewById(R.id.detail_respond_by);
        progressBar = view.findViewById(R.id.detail_progress);
        acceptButton = view.findViewById(R.id.detail_accept);
        declineButton = view.findViewById(R.id.detail_decline);
        viewEventButton = view.findViewById(R.id.detail_view_event);
        myEventsButton = view.findViewById(R.id.detail_my_events);
        ImageButton backButton = view.findViewById(R.id.detail_back);

        backButton.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        if (getArguments() != null) {
            notificationId = getArguments().getString("notificationId");
            eventId = getArguments().getString("eventId");
            waitlistEntryId = getArguments().getString("waitlistEntryId");
            eventName = getArguments().getString("eventName");
            body = getArguments().getString("body");
            messageType = getArguments().getString("type");
            String statusValue = getArguments().getString("status");
            if (!TextUtils.isEmpty(statusValue)) {
                try {
                    currentStatus = NotificationStatus.valueOf(statusValue);
                } catch (IllegalArgumentException ignored) {
                    currentStatus = NotificationStatus.UNREAD;
                }
            }
        }

        bindContent();
        updateActionVisibility();
        acceptButton.setOnClickListener(v -> handleAction(NotificationStatus.REGISTERED, WaitlistStatus.CONFIRMED));
        declineButton.setOnClickListener(v -> handleAction(NotificationStatus.DECLINED, WaitlistStatus.DECLINED));
        viewEventButton.setOnClickListener(v -> openEventDetails());
        myEventsButton.setOnClickListener(v -> navigateToMyEvents());
    }

    private void bindContent() {
        statusView.setText(getStatusText(currentStatus));
        if (body != null) {
            bodyView.setText(body);
        }
        if (eventName != null) {
            eventNameView.setText(eventName);
        }
        if (eventTimeView != null) {
            eventTimeView.setText(getString(R.string.notification_event_time_placeholder));
        }
        respondByView.setText(getString(R.string.notification_respond_placeholder));
    }

    private void handleAction(NotificationStatus notificationStatus, @Nullable WaitlistStatus waitlistStatus) {
        if (!isActionable()) {
            return;
        }
        setLoading(true);
        if (waitlistStatus != null && eventId != null && waitlistEntryId != null) {
            waitlistRepository.updateEntrantStatus(eventId, waitlistEntryId, waitlistStatus,
                    (entry, error) -> {
                        if (!isAdded()) {
                            return;
                        }
                        requireActivity().runOnUiThread(() -> completeNotificationUpdate(notificationStatus, error));
                    });
        } else {
            completeNotificationUpdate(notificationStatus, null);
        }
    }

    private void completeNotificationUpdate(NotificationStatus notificationStatus, @Nullable Exception error) {
        if (error != null) {
            setLoading(false);
            showToast(getString(R.string.notification_action_error));
            return;
        }
        if (notificationId != null) {
            notificationRepository.updateNotificationStatus(notificationId, notificationStatus,
                    (ignored, updateError) -> {
                        if (!isAdded()) {
                            return;
                        }
                        requireActivity().runOnUiThread(() -> {
                            setLoading(false);
                            if (updateError != null) {
                                showToast(getString(R.string.notification_action_error));
                                return;
                            }
                            currentStatus = notificationStatus;
                            statusView.setText(getStatusText(notificationStatus));
                            updateActionVisibility();
                            showToast(getString(R.string.notification_action_success));
                        });
                    });
        } else {
            setLoading(false);
        }
    }

    private String getStatusText(NotificationStatus status) {
        switch (status) {
            case ACCEPTED:
                return getString(R.string.notification_status_accepted);
            case DECLINED:
                return getString(R.string.notification_status_declined);
            case REGISTERED:
                return getString(R.string.notification_status_registered);
            case PENDING:
            case UNREAD:
                return getString(R.string.notification_status_pending);
            case INFO:
            default:
                return getString(R.string.notification_status_info);
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        acceptButton.setEnabled(!loading);
        declineButton.setEnabled(!loading);
        viewEventButton.setEnabled(!loading);
        myEventsButton.setEnabled(!loading);
    }

    private void updateActionVisibility() {
        boolean actionable = isActionable();
        acceptButton.setVisibility(actionable ? View.VISIBLE : View.GONE);
        declineButton.setVisibility(actionable ? View.VISIBLE : View.GONE);
        boolean followUp = currentStatus == NotificationStatus.REGISTERED || currentStatus == NotificationStatus.ACCEPTED;
        viewEventButton.setVisibility(followUp ? View.VISIBLE : View.GONE);
        myEventsButton.setVisibility(followUp ? View.VISIBLE : View.GONE);
    }

    private boolean isActionable() {
        return currentStatus == NotificationStatus.PENDING;
    }

    private void openEventDetails() {
        if (eventId == null || eventId.isEmpty() || !isAdded()) {
            showToast(getString(R.string.notification_action_error));
            return;
        }
        Bundle args = new Bundle();
        args.putString(EventDetailFragment.ARG_EVENT_ID, eventId);
        Navigation.findNavController(requireView()).navigate(R.id.eventDetailFragment, args);
    }

    private void navigateToMyEvents() {
        if (!isAdded()) {
            return;
        }
        Navigation.findNavController(requireView()).navigate(R.id.eventHistoryFragment);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}

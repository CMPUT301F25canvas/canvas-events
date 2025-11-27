package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryeventsystem.data.NotificationRepository;
import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.NotificationMessage;
import com.example.lotteryeventsystem.notifications.NotificationAdapter;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Entrant-facing notifications feed.
 */
public class NotificationFragment extends Fragment implements NotificationAdapter.NotificationClickListener {
    private RecyclerView notificationList;
    private View emptyView;
    private View progressView;
    private final NotificationRepository notificationRepository = ServiceLocator.provideNotificationRepository();
    private ListenerRegistration registration;
    private NotificationAdapter adapter;
    private String recipientId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (((MainActivity) requireActivity()).getAdmin()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_notificationFragment_to_adminNotificationFragment);
            return;
        }
        super.onViewCreated(view, savedInstanceState);
        notificationList = view.findViewById(R.id.notification_list);
        emptyView = view.findViewById(R.id.notification_empty);
        progressView = view.findViewById(R.id.notification_progress);
        ImageButton settingsButton = view.findViewById(R.id.button_notification_settings);

        adapter = new NotificationAdapter(this);
        notificationList.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList.setAdapter(adapter);

        recipientId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (recipientId == null) {
            recipientId = "";
        }
        settingsButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_notificationFragment_to_notificationSettingsFragment);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeToNotifications();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    private void subscribeToNotifications() {
        setLoading(true);
        if (registration != null) {
            registration.remove();
        }
        registration = notificationRepository.listenToUserNotifications(recipientId, this::handleNotificationUpdate);
    }

    private void handleNotificationUpdate(@Nullable List<NotificationMessage> messages, @Nullable Exception error) {
        if (!isAdded()) {
            return;
        }
        requireActivity().runOnUiThread(() -> {
            setLoading(false);
            if (error != null || messages == null) {
                emptyView.setVisibility(View.VISIBLE);
                adapter.submitList(null);
                return;
            }
            adapter.submitList(messages);
            emptyView.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setLoading(boolean loading) {
        if (progressView != null) {
            progressView.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onOpen(NotificationMessage message) {
        if (!isAdded()) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("notificationId", message.getId());
        args.putString("eventId", message.getEventId());
        args.putString("eventName", message.getEventName());
        args.putString("body", message.getBody());
        args.putString("title", message.getTitle());
        args.putString("status", message.getStatus() != null ? message.getStatus().name() : null);
        args.putString("waitlistEntryId", message.getWaitlistEntryId());
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_notificationFragment_to_notificationDetailFragment, args);
    }
}

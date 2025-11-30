package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationFragment extends Fragment implements NotificationAdapter.Listener {

    private RecyclerView recyclerView;
    private View emptyView;
    private View progressView;
    private NotificationAdapter adapter;
    private NotificationRepository repo = new NotificationRepository();

    private String userId = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        if (((MainActivity) requireActivity()).getAdmin()) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_notificationFragment_to_adminNotificationsFragment);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.notification_list);
        emptyView = view.findViewById(R.id.notification_empty);
        progressView = view.findViewById(R.id.notification_progress);

        adapter = new NotificationAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Get device user ID
        userId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (userId == null) {
            userId = "";
        }

        listenToNotifications();
    }

    private void listenToNotifications() {
        progressView.setVisibility(View.VISIBLE);

        repo.listenToUserNotifications(userId, (messages, error) -> {
            if (!isAdded()) return;

            progressView.setVisibility(View.GONE);

            if (error != null) {
                Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                emptyView.setVisibility(View.VISIBLE);
                adapter.submitList(new ArrayList<>());
                return;
            }

            if (messages == null || messages.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                adapter.submitList(new ArrayList<>());
                return;
            }

            emptyView.setVisibility(View.GONE);
            adapter.submitList(messages);
        });
    }

    @Override
    public void onOpen(NotificationMessage msg) {
        if (!isAdded()) return;

        NotificationRepository repo = new NotificationRepository();
        userId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        repo.markAsSeen(msg.getType(), msg.getEventId(), userId);

        Bundle args = new Bundle();
        args.putString("type", msg.getType());
        args.putString("body", msg.getBody());
        args.putString("title", msg.getTitle());
        args.putString("eventId", msg.getEventId());
        args.putString("response", msg.getResponse());

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_notificationFragment_to_notificationDetailFragment, args);
    }
}

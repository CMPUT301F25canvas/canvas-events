package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryeventsystem.data.NotificationRepository;
import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.NotificationMessage;
import com.example.lotteryeventsystem.notifications.NotificationAdapter;

import java.util.List;

/**
 * Admin-facing view of all notification logs.
 */
public class AdminNotificationLogFragment extends Fragment implements NotificationAdapter.NotificationClickListener {
    private RecyclerView recyclerView;
    private View emptyView;
    private View progressView;
    private final NotificationRepository notificationRepository = ServiceLocator.provideNotificationRepository();
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.admin_notification_list);
        emptyView = view.findViewById(R.id.admin_notification_empty);
        progressView = view.findViewById(R.id.admin_notification_progress);
        adapter = new NotificationAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        loadLogs();
    }

    private void loadLogs() {
        setLoading(true);
        notificationRepository.listenToAllNotifications((messages, error) -> {
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                setLoading(false);
                if (error != null || messages == null) {
                    Toast.makeText(getContext(), "Could not load notification logs", Toast.LENGTH_SHORT).show();
                    emptyView.setVisibility(View.VISIBLE);
                    return;
                }
                adapter.submitList(messages);
                emptyView.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void setLoading(boolean loading) {
        if (progressView != null) {
            progressView.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onOpen(NotificationMessage message) {
        // Read-only; do nothing on click for admin log.
    }
}

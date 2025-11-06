package com.example.lotteryeventsystem.organizer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryeventsystem.R;
import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.Event;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.service.WaitlistService;

import java.util.List;

/**
 * Organizer tools so we can manage invited entrants and draw replacements.
 */
public class OrganizerFragment extends Fragment implements OrganizerInvitedAdapter.OnEntrantActionListener {
    private EditText eventIdInput;
    private Button loadButton;
    private Button drawButton;
    private TextView eventTitleView;
    private TextView waitingCountView;
    private TextView statusMessageView;
    private View progressView;
    private RecyclerView invitedRecycler;
    private TextView invitedHeaderView;
    private OrganizerInvitedAdapter adapter;

    private String currentEventId;

    private final WaitlistService waitlistService = ServiceLocator.provideWaitlistService();

    public OrganizerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRecycler();
        setupListeners();
    }

    private void bindViews(View root) {
        eventIdInput = root.findViewById(R.id.input_event_id);
        loadButton = root.findViewById(R.id.button_load_event);
        drawButton = root.findViewById(R.id.button_draw_replacement);
        eventTitleView = root.findViewById(R.id.text_event_title);
        waitingCountView = root.findViewById(R.id.text_waiting_count);
        statusMessageView = root.findViewById(R.id.text_status_message);
        progressView = root.findViewById(R.id.organizer_progress);
        invitedRecycler = root.findViewById(R.id.list_invited);
        invitedHeaderView = root.findViewById(R.id.text_invited_header);
    }

    private void setupRecycler() {
        adapter = new OrganizerInvitedAdapter(this);
        invitedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        invitedRecycler.setAdapter(adapter);
    }

    private void setupListeners() {
        loadButton.setOnClickListener(v -> tryLoadEvent());
        drawButton.setOnClickListener(v -> triggerDraw());
    }

    private void tryLoadEvent() {
        hideMessage();
        String eventId = eventIdInput.getText().toString().trim();
        if (TextUtils.isEmpty(eventId)) {
            showToast(R.string.organizer_missing_event_id);
            return;
        }
        showProgress(true);
        ServiceLocator.provideEventRepository()
                .getEventById(eventId, (event, error) -> {
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        showProgress(false);
                        if (error != null) {
                            showToast(R.string.organizer_action_error);
                            return;
                        }
                        if (event == null) {
                            showToast(R.string.organizer_event_not_found);
                            return;
                        }
                        currentEventId = eventId;
                        showEvent(event);
                        showToast(R.string.organizer_event_loaded);
                        refreshWaitlist();
                    });
                });
    }

    private void showEvent(Event event) {
        eventTitleView.setVisibility(View.VISIBLE);
        eventTitleView.setText(TextUtils.isEmpty(event.getTitle())
                ? getString(R.string.event_detail_name_fallback)
                : event.getTitle());
        drawButton.setVisibility(View.VISIBLE);
        drawButton.setEnabled(true);
        waitingCountView.setVisibility(View.VISIBLE);
    }

    private void refreshWaitlist() {
        if (currentEventId == null) {
            return;
        }
        showProgress(true);
        ServiceLocator.provideWaitlistRepository()
                .getInvitedEntrants(currentEventId, (entries, error) -> {
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> handleInvitedResponse(entries, error));
                });
        ServiceLocator.provideWaitlistRepository()
                .getWaitingEntrants(currentEventId, (entries, error) -> {
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> handleWaitingResponse(entries, error));
                });
    }

    private void handleInvitedResponse(List<WaitlistEntry> entries, Exception error) {
        showProgress(false);
        if (error != null) {
            showToast(R.string.organizer_action_error);
            return;
        }
        if (entries == null || entries.isEmpty()) {
            invitedHeaderView.setVisibility(View.GONE);
            adapter.submitList(null);
            statusMessageView.setVisibility(View.VISIBLE);
            statusMessageView.setText(R.string.organizer_draw_empty);
        } else {
            invitedHeaderView.setVisibility(View.VISIBLE);
            adapter.submitList(entries);
            statusMessageView.setVisibility(View.GONE);
        }
    }

    private void handleWaitingResponse(List<WaitlistEntry> entries, Exception error) {
        if (error != null) {
            waitingCountView.setText(R.string.organizer_action_error);
            return;
        }
        int count = entries != null ? entries.size() : 0;
        waitingCountView.setText(getString(R.string.organizer_waiting_count_format, count));
    }

    private void triggerDraw() {
        if (currentEventId == null) {
            showToast(R.string.organizer_missing_event_id);
            return;
        }
        drawButton.setEnabled(false);
        waitlistService.drawReplacement(currentEventId, new WaitlistService.WaitlistActionCallback() {
            @Override
            public void onSuccess(@Nullable WaitlistEntry replacement) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    drawButton.setEnabled(true);
                    if (replacement == null) {
                        showToast(R.string.organizer_draw_empty);
                    } else {
                        String name = replacement.getEntrantName() != null
                                ? replacement.getEntrantName()
                                : getString(R.string.event_detail_name_fallback);
                        showToastMessage(getString(R.string.organizer_draw_success, name));
                    }
                    refreshWaitlist();
                });
            }

            @Override
            public void onFailure(Exception error) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    drawButton.setEnabled(true);
                    showToast(R.string.organizer_action_error);
                });
            }
        });
    }

    @Override
    public void onDeclineClicked(WaitlistEntry entry) {
        if (currentEventId == null) {
            return;
        }
        waitlistService.markEntrantDeclined(currentEventId, entry.getId(),
                new WaitlistService.WaitlistActionCallback() {
                    @Override
                    public void onSuccess(@Nullable WaitlistEntry replacement) {
                        if (!isAdded()) {
                            return;
                        }
                        requireActivity().runOnUiThread(() -> {
                            String name = entry.getEntrantName() != null
                                    ? entry.getEntrantName()
                                    : getString(R.string.event_detail_name_fallback);
                            showToastMessage(getString(R.string.organizer_decline_success, name));
                            refreshWaitlist();
                        });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        if (!isAdded()) {
                            return;
                        }
                        requireActivity().runOnUiThread(() -> showToast(R.string.organizer_action_error));
                    }
                });
    }

    private void showProgress(boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showToast(int messageRes) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), messageRes, Toast.LENGTH_SHORT).show();
    }

    private void showToastMessage(CharSequence message) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void hideMessage() {
        statusMessageView.setVisibility(View.GONE);
    }
}

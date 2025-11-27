package com.example.lotteryeventsystem.notifications;

import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryeventsystem.R;
import com.example.lotteryeventsystem.model.NotificationMessage;
import com.example.lotteryeventsystem.model.NotificationStatus;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple adapter for the notifications feed.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface NotificationClickListener {
        void onOpen(NotificationMessage message);
    }

    private final List<NotificationMessage> items = new ArrayList<>();
    private final NotificationClickListener listener;

    public NotificationAdapter(NotificationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NotificationMessage> messages) {
        items.clear();
        if (messages != null) {
            items.addAll(messages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationMessage message = items.get(position);
        holder.bind(message, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView bodyView;
        private final TextView statusView;
        private final TextView timestampView;
        private final MaterialButton openButton;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.text_title);
            bodyView = itemView.findViewById(R.id.text_body);
            statusView = itemView.findViewById(R.id.text_status);
            timestampView = itemView.findViewById(R.id.text_timestamp);
            openButton = itemView.findViewById(R.id.button_open);
        }

        void bind(NotificationMessage message, NotificationClickListener listener) {
            String title = message.getEventName() != null ? message.getEventName() : message.getTitle();
            if (title == null || title.isEmpty()) {
                title = itemView.getContext().getString(R.string.notification_title_fallback);
            }
            titleView.setText(title);
            bodyView.setText(message.getBody());
            statusView.setText(getStatusLabel(message.getStatus()));
            setStatusAppearance(message.getStatus());

            if (message.getCreatedAt() != null) {
                long now = System.currentTimeMillis();
                CharSequence relative = DateUtils.getRelativeTimeSpanString(
                        message.getCreatedAt().toDate().getTime(),
                        now,
                        DateUtils.MINUTE_IN_MILLIS);
                timestampView.setText(relative);
            } else {
                timestampView.setText("");
            }

            boolean pendingInvite = message.getStatus() == NotificationStatus.PENDING;
            String buttonText = pendingInvite
                    ? itemView.getContext().getString(R.string.notification_open)
                    : itemView.getContext().getString(R.string.notification_view_details);
            openButton.setText(buttonText);

            itemView.setOnClickListener(v -> listener.onOpen(message));
            openButton.setOnClickListener(v -> listener.onOpen(message));
        }

        private String getStatusLabel(NotificationStatus status) {
            Resources res = itemView.getResources();
            if (status == null) {
                return res.getString(R.string.notification_status_unread);
            }
            switch (status) {
                case PENDING:
                    return res.getString(R.string.notification_status_pending);
                case ACCEPTED:
                    return res.getString(R.string.notification_status_accepted);
                case DECLINED:
                    return res.getString(R.string.notification_status_declined);
                case REGISTERED:
                    return res.getString(R.string.notification_status_registered);
                case WAITING:
                    return res.getString(R.string.notification_status_waiting);
                case NOT_SELECTED:
                    return res.getString(R.string.notification_status_not_selected);
                case INFO:
                    return res.getString(R.string.notification_status_info);
                case UNREAD:
                default:
                    return res.getString(R.string.notification_status_unread);
            }
        }

        private void setStatusAppearance(NotificationStatus status) {
            if (status == null) {
                status = NotificationStatus.UNREAD;
            }
            int bgRes;
            int textColor;
            switch (status) {
                case PENDING:
                    bgRes = R.drawable.bg_status_pending;
                    textColor = 0xFF4B2BB1;
                    break;
                case ACCEPTED:
                    bgRes = R.drawable.bg_status_accepted;
                    textColor = 0xFF176C4A;
                    break;
                case REGISTERED:
                    bgRes = R.drawable.bg_status_registered;
                    textColor = 0xFF0B65B5;
                    break;
                case DECLINED:
                    bgRes = R.drawable.bg_status_declined;
                    textColor = 0xFF8A1B2F;
                    break;
                case WAITING:
                    bgRes = R.drawable.bg_status_waiting;
                    textColor = 0xFF176C4A;
                    break;
                case NOT_SELECTED:
                    bgRes = R.drawable.bg_status_not_selected;
                    textColor = 0xFF9A4A0A;
                    break;
                case INFO:
                case UNREAD:
                default:
                    bgRes = R.drawable.bg_status_info;
                    textColor = 0xFF4B2BB1;
                    break;
            }
            statusView.setBackgroundResource(bgRes);
            statusView.setTextColor(textColor);
        }
    }
}

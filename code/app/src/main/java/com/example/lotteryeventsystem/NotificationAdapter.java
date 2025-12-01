package com.example.lotteryeventsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface Listener {
        void onOpen(NotificationMessage msg);
    }

    private List<NotificationMessage> items = new ArrayList<>();
    private final Listener listener;

    public NotificationAdapter(Listener l) {
        this.listener = l;
    }

    public void submitList(List<NotificationMessage> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView title, body, status, timestamp;
        MaterialButton open;

        VH(View v) {
            super(v);
            title = v.findViewById(R.id.text_title);
            body = v.findViewById(R.id.text_body);
            status = v.findViewById(R.id.text_status);
            timestamp = v.findViewById(R.id.text_timestamp);
            open = v.findViewById(R.id.button_open);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_card, parent, false));
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {
        NotificationMessage m = items.get(pos);
        Context ctx = h.itemView.getContext();

        // Basic notification data
        h.title.setText(m.getTitle());
        h.body.setText(m.getBody());

        long now = System.currentTimeMillis();
        long diff = now - m.getTimestamp();
        h.timestamp.setText(TimeAgo.formatTimeDiff(diff));

        if (m.getType().equals("selected_notification")) {

            String r = m.getResponse();   // response from Firestore

            if (r == null || r.equals("None")) {
                // PENDING
                h.status.setText("Pending");

            } else if (r.equals("Accepted")) {
                h.status.setText("Accepted");
                h.status.setBackgroundResource(R.drawable.bg_status_accepted);

            } else if (r.equals("Rejected")) {
                // DECLINED
                h.status.setText("Declined");
                h.status.setBackgroundResource(R.drawable.bg_status_declined);

            }

        } else {
            // NORMAL NOTIFICATIONS: Seen/New
            h.status.setText(m.isSeen() ? "Seen" : "New");
            h.status.setBackgroundResource(R.drawable.bg_status_info);
        }

        // Button
        h.open.setOnClickListener(v -> listener.onOpen(m));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

package com.example.lotteryeventsystem.organizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryeventsystem.R;
import com.example.lotteryeventsystem.model.WaitlistEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows invited entrants with a quick action to mark them as declined.
 */
public class OrganizerInvitedAdapter extends RecyclerView.Adapter<OrganizerInvitedAdapter.InvitedViewHolder> {
    interface OnEntrantActionListener {
        void onDeclineClicked(WaitlistEntry entry);
    }

    private final List<WaitlistEntry> items = new ArrayList<>();
    private final OnEntrantActionListener listener;

    public OrganizerInvitedAdapter(OnEntrantActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public InvitedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invited_entrant, parent, false);
        return new InvitedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitedViewHolder holder, int position) {
        WaitlistEntry entry = items.get(position);
        holder.bind(entry, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<WaitlistEntry> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    static class InvitedViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView contactView;
        private final Button declineButton;

        InvitedViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_entrant_name);
            contactView = itemView.findViewById(R.id.text_entrant_contact);
            declineButton = itemView.findViewById(R.id.button_mark_declined);
        }

        void bind(WaitlistEntry entry, OnEntrantActionListener listener) {
            nameView.setText(entry.getEntrantName());
            String contact = entry.primaryContact();
            if (contact != null && !contact.isEmpty()) {
                contactView.setVisibility(View.VISIBLE);
                contactView.setText(contact);
            } else {
                contactView.setVisibility(View.GONE);
            }
            declineButton.setOnClickListener(v -> listener.onDeclineClicked(entry));
        }
    }
}

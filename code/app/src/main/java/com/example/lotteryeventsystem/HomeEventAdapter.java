package com.example.lotteryeventsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Recycler adapter for the home event feed.
 */
public class HomeEventAdapter extends RecyclerView.Adapter<HomeEventAdapter.EventViewHolder> {
    interface OnEventClickListener {
        void onEventClick(EventItem item);
    }

    private final List<EventItem> items = new ArrayList<>();
    private final OnEventClickListener listener;

    public HomeEventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<EventItem> events) {
        items.clear();
        if (events != null) {
            items.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView descriptionView;
        private final TextView locationView;
        private final TextView dateHighlightView;
        private final ImageView posterView;


        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.event_title);
            descriptionView = itemView.findViewById(R.id.event_description);
            locationView = itemView.findViewById(R.id.event_location);
            dateHighlightView = itemView.findViewById(R.id.event_date_highlight);
            posterView = itemView.findViewById(R.id.event_poster);
        }

        void bind(EventItem item, OnEventClickListener listener) {
            titleView.setText(item.name != null ? item.name : "");
            descriptionView.setText(item.description != null ? item.description : "");
            locationView.setText(item.location != null ? item.location : "");
            dateHighlightView.setText(formatHighlight(item.dateHighlight));
            itemView.setOnClickListener(v -> listener.onEventClick(item));

            Glide.with(itemView.getContext())
                    .load(item.posterUrl != null ? item.posterUrl : R.drawable.img_placeholder)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(posterView);
        }

        private String formatHighlight(String raw) {
            if (raw == null || raw.isEmpty()) {
                return "";
            }
            // Try to convert into "MMM dd"
            String[] patterns = {"yyyy-MM-dd", "MMM dd, yyyy", "MMM dd yyyy"};
            for (String pattern : patterns) {
                try {
                    LocalDate date = LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
                    return date.format(DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH));
                } catch (DateTimeParseException ignored) {
                }
            }
            return raw;
        }
    }
}

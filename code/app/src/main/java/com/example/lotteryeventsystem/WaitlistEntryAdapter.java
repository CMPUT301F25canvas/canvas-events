package com.example.lotteryeventsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import java.util.ArrayList;

/**
 * A custom ArrayAdapter for displaying WaitlistEntry objects in a ListView.
 * This adapter is responsible for creating and binding views for waitlist entries,
 * showing entrant names in a list format for organizer viewing.
 *
 * @author Emily Lan
 * @version 1.0
 * @see WaitlistEntry
 */
public class WaitlistEntryAdapter extends ArrayAdapter<WaitlistEntry> {
    /**
     * Constructs a new WaitlistEntryAdapter with the given context and list of waitlist entries.
     *
     * @param context The current context used to inflate layout files
     * @param entrants The list of WaitlistEntry objects to represent in the ListView
     */
    public WaitlistEntryAdapter(Context context, ArrayList<WaitlistEntry> entrants) {
        super(context, 0, entrants);
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * This method handles view recycling and data binding for individual list items.
     *
     * @param position The position of the item within the adapter's data set
     * @param convertView The old view to reuse, if possible. Note: You should check that this
     *                   view is non-null and of an appropriate type before using.
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position
     *
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WaitlistEntry entrant = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.organizer_entrant_list_item, parent, false);
        }
        TextView tvItem = convertView.findViewById(R.id.tvItem);
        if (entrant != null && entrant.getEntrantName() != null && !entrant.getEntrantName().isEmpty()) {
            tvItem.setText(entrant.getEntrantName());
        } else {
            // Calculate anonymous number based on position in current list
            int anonymousNumber = calculateAnonymousNumber(position);
            tvItem.setText("Anonymous" + anonymousNumber);
        }
        return convertView;
    }

    /**
     * Calculates the anonymous number for an entrant at the specified position by counting
     * how many anonymous entrants (those with null or empty names) appear before and including
     * the current position in the list. This method provides dynamic anonymous numbering that recalculates each time getView
     * is called, ensuring the number reflects the entrant's position in the current list order.
     *
     * @param currentPosition The position in the adapter for which to calculate the anonymous number
     * @return The sequential anonymous number for the entrant at the specified position, starting from 1 for the first anonymous entrant
     */
    private int calculateAnonymousNumber(int currentPosition) {
        int anonymousCount = 0;
        for (int i = 0; i <= currentPosition; i++) {
            WaitlistEntry entrant = getItem(i);
            if (entrant != null && (entrant.getEntrantName() == null || entrant.getEntrantName().isEmpty())) {
                anonymousCount++;
            }
        }
        return anonymousCount;
    }
}
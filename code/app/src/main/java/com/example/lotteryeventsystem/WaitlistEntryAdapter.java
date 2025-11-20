package com.example.lotteryeventsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import java.util.ArrayList;
import java.util.HashMap;

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
    private HashMap<String, Integer> anonymousIdMap; // Maps entrant ID to anonymous number

    /**
     * Constructs a new WaitlistEntryAdapter with the given context and list of waitlist entries.
     *
     * @param context  The current context used to inflate layout files
     * @param entrants The list of WaitlistEntry objects to represent in the ListView
     */
    public WaitlistEntryAdapter(Context context, ArrayList<WaitlistEntry> entrants) {
        super(context, 0, entrants);
        calculateConsistentAnonymousIds(entrants);
    }

    /**
     * Pre-calculates consistent anonymous IDs across all lists based on unique entrant IDs
     * This ensures the same anonymous entrant gets the same number across different filtered views
     *
     * @param entrants The list of WaitlistEntry objects to process
     */
    private void calculateConsistentAnonymousIds(ArrayList<WaitlistEntry> entrants) {
        anonymousIdMap = new HashMap<>();
        int anonymousCounter = 1;

        // First pass: assign IDs to anonymous entrants
        for (WaitlistEntry entrant : entrants) {
            if (entrant != null && isAnonymous(entrant)) {
                String entrantId = entrant.getId();
                if (entrantId != null && !entrantId.isEmpty() && !anonymousIdMap.containsKey(entrantId)) {
                    anonymousIdMap.put(entrantId, anonymousCounter++);
                }
            }
        }
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * This method handles view recycling and data binding for individual list items.
     *
     * @param position    The position of the item within the adapter's data set
     * @param convertView The old view to reuse, if possible. Note: You should check that this
     *                    view is non-null and of an appropriate type before using.
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WaitlistEntry entrant = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.organizer_entrant_list_item, parent, false);
        }
        TextView tvItem = convertView.findViewById(R.id.tvItem);
        if (entrant != null) {
            String displayName = getUserDisplayName(entrant);
            if (displayName != null && !displayName.isEmpty()) {
                tvItem.setText(displayName);
            } else {
                int anonymousNumber = getConsistentAnonymousNumber(entrant);
                tvItem.setText("Anonymous" + anonymousNumber);
            }
        }
        return convertView;
    }

    /**
     * Gets the consistent anonymous number for an entrant based on their unique ID
     * This ensures the same entrant gets the same anonymous number across different filtered lists
     *
     * @param entrant The WaitlistEntry to get the anonymous number for
     * @return The consistent anonymous number for this entrant
     */
    private int getConsistentAnonymousNumber(WaitlistEntry entrant) {
        if (entrant == null || entrant.getId() == null) {
            return 1; // fallback
        }

        String entrantId = entrant.getId();
        if (anonymousIdMap != null && anonymousIdMap.containsKey(entrantId)) {
            return anonymousIdMap.get(entrantId);
        }

        // If for some reason this entrant wasn't in our map, assign a new number
        int newNumber = anonymousIdMap.size() + 1;
        anonymousIdMap.put(entrantId, newNumber);
        return newNumber;
    }

    /**
     * Gets the display name for an entrant.
     *
     * @param entrant The WaitlistEntry to get the display name for
     * @return The display name, or null if no name is available
     */
    private String getUserDisplayName(WaitlistEntry entrant) {
        if (entrant == null) {
            return null;
        }

        if (entrant.getUser() != null && entrant.getUser().getName() != null && !entrant.getUser().getName().isEmpty()) {
            return entrant.getUser().getName();
        }
        if (entrant.getEntrantName() != null && !entrant.getEntrantName().isEmpty()) {
            return entrant.getEntrantName();
        }
        return null;
    }

    /**
     * Checks if an entrant is anonymous (has no display name)
     *
     * @param entrant The WaitlistEntry to check
     * @return true if the entrant is anonymous, false otherwise
     */
    private boolean isAnonymous(WaitlistEntry entrant) {
        if (entrant == null) {
            return true;
        }

        boolean hasUserName = entrant.getUser() != null &&
                entrant.getUser().getName() != null &&
                !entrant.getUser().getName().isEmpty();

        boolean hasEntrantName = entrant.getEntrantName() != null &&
                !entrant.getEntrantName().isEmpty();

        return !hasUserName && !hasEntrantName;
    }
}
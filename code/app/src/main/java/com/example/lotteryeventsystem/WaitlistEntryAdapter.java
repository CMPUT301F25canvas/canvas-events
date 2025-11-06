package com.example.lotteryeventsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.lotteryeventsystem.model.WaitlistEntry;

import java.util.ArrayList;

public class WaitlistEntryAdapter extends ArrayAdapter<WaitlistEntry> {
    public WaitlistEntryAdapter(Context context, ArrayList<WaitlistEntry> entrants) {
        super(context, 0, entrants);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WaitlistEntry entrant = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.organizer_entrant_list_item, parent, false);
        }

        TextView tvItem = convertView.findViewById(R.id.tvItem);

        // Combine name and ID with a space
        tvItem.setText(entrant.getEntrantName());

        return convertView;
    }
}

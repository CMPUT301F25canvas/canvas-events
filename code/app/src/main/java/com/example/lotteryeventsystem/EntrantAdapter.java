package com.example.lotteryeventsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class EntrantAdapter extends ArrayAdapter<Entrant> {
    public EntrantAdapter(Context context, ArrayList<Entrant> entrants) {
        super(context, 0, entrants);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Entrant entrant = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView tvItem = convertView.findViewById(R.id.tvItem);

        // Combine name and ID with a space
        tvItem.setText(entrant.getName() + " " + entrant.getId());

        return convertView;
    }
}

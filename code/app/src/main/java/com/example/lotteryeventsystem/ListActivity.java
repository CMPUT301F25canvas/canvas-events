package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private ListView listView;
    private TextView tvTitle;
    private ImageButton btnBack;

    private ArrayList<Entrant> enrolledEntrants = new ArrayList<>();
    private ArrayList<Entrant> canceledEntrants = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listView);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.back_button);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this activity and return to MainActivity
                finish();
            }
        });

        initializeData();

        String listType = getIntent().getStringExtra("LIST_TYPE");

        ArrayList<Entrant> dataToShow;

        if ("canceled".equals(listType)) {
            dataToShow = canceledEntrants;
            tvTitle.setText("Canceled Entrants");
        } else {
            dataToShow = enrolledEntrants;
            tvTitle.setText("Enrolled Entrants");
        }

        EntrantAdapter adapter = new EntrantAdapter(this, dataToShow);
        listView.setAdapter(adapter);
    }

    private void initializeData() {
        // Enrolled entrants
        enrolledEntrants.add(new Entrant("Alice Johnson", "001"));
        enrolledEntrants.add(new Entrant("Bob Smith", "002"));
        enrolledEntrants.add(new Entrant("Carol Davis", "003"));
        enrolledEntrants.add(new Entrant("David Wilson", "004"));
        enrolledEntrants.add(new Entrant("Emma Brown", "005"));

        // Canceled entrants
        canceledEntrants.add(new Entrant("George Taylor", "101"));
        canceledEntrants.add(new Entrant("Helen White", "102"));
        canceledEntrants.add(new Entrant("Ian Clark", "103"));
        canceledEntrants.add(new Entrant("Julia Lee", "104"));
        canceledEntrants.add(new Entrant("Kevin Martin", "105"));
    }
}

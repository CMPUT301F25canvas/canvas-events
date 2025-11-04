package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
    private Button btnDeleteSelectedEntrant;
    int entrantPosition = -1;
    private EntrantAdapter adapter;
    // private final FirebaseWaitlistRepository repository = new FirebaseWaitlistRepository();

    private ArrayList<Entrant> enrolledEntrants = new ArrayList<>();
    private ArrayList<Entrant> canceledEntrants = new ArrayList<>();
    private ArrayList<Entrant> unenrolledEntrants = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listView);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.back_button);
        btnDeleteSelectedEntrant = findViewById(R.id.btnDeleteSelectedEntrant);

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
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
        } else if ("unenrolled".equals(listType)) {
            dataToShow = unenrolledEntrants;
            tvTitle.setText("Unenrolled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.VISIBLE);
        } else {
            dataToShow = enrolledEntrants;
            tvTitle.setText("Enrolled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
        }

        adapter = new EntrantAdapter(this, dataToShow);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Track which entrant is selected
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                entrantPosition = position;
            }
        });
        btnDeleteSelectedEntrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entrantPosition != -1) {
                    Entrant entrantToDelete = unenrolledEntrants.get(entrantPosition);
                    deleteEntrantFromFirebase(entrantToDelete);
                }
            }
        });
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

        // Unenrolled entrants
        unenrolledEntrants.add(new Entrant("Sleepy Hollow", "106"));
        unenrolledEntrants.add(new Entrant("Tim Burton", "107"));
        unenrolledEntrants.add(new Entrant("Rainy Friday", "108"));
        unenrolledEntrants.add(new Entrant("Rock Lee", "109"));
        unenrolledEntrants.add(new Entrant("Cherry Stem", "110"));
    }

    private void deleteEntrantFromFirebase(Entrant entrant) {
        String eventId = "exampleEventId"; // replace with actual event ID

        //repository.updateEntrantStatus(eventId, entrant.getId(), WaitlistStatus.DELETED, (updatedEntry, error) -> {

        // Move entrant from unenrolled → canceled
        unenrolledEntrants.remove(entrant);
        canceledEntrants.add(entrant);

        entrantPosition = -1; // reset selection
        adapter.notifyDataSetChanged(); // refresh UI
        //});
    }
}

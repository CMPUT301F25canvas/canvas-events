package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotteryeventsystem.data.FirebaseWaitlistRepository;
import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.model.WaitlistEntry;
import com.example.lotteryeventsystem.model.WaitlistStatus;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private ListView listView;
    private TextView tvTitle;
    private ImageButton btnBack;
    private Button btnDeleteSelectedEntrant;
    int entrantPosition = -1;
    private WaitlistEntryAdapter adapter;
    private final FirebaseWaitlistRepository repository = new FirebaseWaitlistRepository();

    private ArrayList<WaitlistEntry> entrantsList = new ArrayList<>();
    private String eventId;
    private String listType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_view_entrant_list);

        listView = findViewById(R.id.listView);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.back_button);
        btnDeleteSelectedEntrant = findViewById(R.id.btnDeleteSelectedEntrant);
        eventId = getIntent().getStringExtra("EVENT_ID");
        listType = getIntent().getStringExtra("LIST_TYPE");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this activity and return to MainActivity
                finish();
            }
        });

        adapter = new WaitlistEntryAdapter(this, entrantsList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        loadDataFromFirebase();

        if ("canceled".equals(listType)) {
            tvTitle.setText("Canceled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
        } else if ("unenrolled".equals(listType)) {
            tvTitle.setText("Unenrolled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText("Enrolled Entrants");
            btnDeleteSelectedEntrant.setVisibility(View.GONE);
        }

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
                    WaitlistEntry entrantToDelete = entrantsList.get(entrantPosition);
                    deleteEntrantFromFirebase(entrantToDelete);
                }
            }
        });
    }

    private void loadDataFromFirebase() {
        //String testEventId = "event_id2";
        List<WaitlistStatus> statusesToLoad = new ArrayList<>();

        switch (listType) {
            case "canceled":
                statusesToLoad.add(WaitlistStatus.CANCELLED);
                statusesToLoad.add(WaitlistStatus.DECLINED);
                break;
            case "enrolled":
                statusesToLoad.add(WaitlistStatus.CONFIRMED);
                break;
            case "unenrolled":
                statusesToLoad.add(WaitlistStatus.INVITED);
                break;
        }

        repository.getEntrantsByStatus(eventId, statusesToLoad, new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onComplete(List<WaitlistEntry> result, Exception error) {
                if (error == null && result != null) {
                    entrantsList.clear();
                    // Convert WaitlistEntry to Entrant
                    entrantsList.addAll(result);
                    adapter.notifyDataSetChanged();
                    //System.out.println("DEBUG: Loaded " + result.size() + " entrants");
                    //for (WaitlistEntry entry : result) {
                        //System.out.println("DEBUG: " + entry.getEntrantName() + " - " + entry.getStatus());
                    }
                //} else {
                    //System.out.println("DEBUG: Error: " + error);
                    //error.printStackTrace();
                //}
            }
        });
    }


    private void deleteEntrantFromFirebase(WaitlistEntry entrant) {
        //String testEventId = "event_id2";
        // Update status to CANCELED in Firebase
        repository.updateEntrantStatus(eventId, entrant.getId(), WaitlistStatus.CANCELLED,
                new RepositoryCallback<WaitlistEntry>() {
                    @Override
                    public void onComplete(WaitlistEntry updatedEntry, Exception error) {
                        if (error == null) {
                            // Success - remove from current list and refresh
                            entrantsList.remove(entrantPosition);
                            entrantPosition = -1;
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}

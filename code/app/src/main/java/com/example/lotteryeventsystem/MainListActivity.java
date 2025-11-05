package com.example.lotteryeventsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainListActivity extends AppCompatActivity {
    private Button btnCanceled, btnEnrolled, btnNotify, btnCancelEntrant, btnReplace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        btnCanceled = findViewById(R.id.btnCanceled);
        btnEnrolled = findViewById(R.id.btnEnrolled);
        btnCancelEntrant = findViewById(R.id.btnCancelEntrant);

        btnCanceled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainListActivity.this, ListActivity.class);
                intent.putExtra("LIST_TYPE", "canceled");
                startActivity(intent);
            }
        });

        btnCancelEntrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainListActivity.this, ListActivity.class);
                intent.putExtra("LIST_TYPE", "unenrolled");
                startActivity(intent);
            }
        });

        btnEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainListActivity.this, ListActivity.class);
                intent.putExtra("LIST_TYPE", "enrolled");
                startActivity(intent);
            }
        });
    }
}
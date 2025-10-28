package com.example.lotteryeventsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Button btnCanceled, btnEnrolled, btnNotify, btnCancelEntrant, btnReplace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCanceled = findViewById(R.id.btnCanceled);
        btnEnrolled = findViewById(R.id.btnEnrolled);
        btnNotify = findViewById(R.id.btnNotify);
        btnCancelEntrant = findViewById(R.id.btnCancelEntrant);
        btnReplace = findViewById(R.id.btnReplace);

        btnCanceled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra("LIST_TYPE", "canceled");
                startActivity(intent);
            }
        });

        btnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Chosen Entrants Notified!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelEntrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "2 Unenrolled Entrants Canceled!", Toast.LENGTH_SHORT).show();
            }
        });

        btnReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "2 Replacement Entrants Drawn!", Toast.LENGTH_SHORT).show();
            }
        });

        btnEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra("LIST_TYPE", "enrolled");
                startActivity(intent);
            }
        });
    }
}
package com.example.lotteryeventsystem;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class App extends Application {
    @Override public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(new FirebaseFirestoreSettings
                .Builder()
                .setPersistenceEnabled(true)
                .build());

        // Anonymous sign-in (US 01.07.01)
        FirebaseAuth.getInstance().signInAnonymously();
    }
}

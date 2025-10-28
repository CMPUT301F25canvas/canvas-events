package com.example.lotteryeventsystem;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class FirestoreSmokeTest {
    @Test public void writeReadOk() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String,Object> ev = new HashMap<>();
        ev.put("title","Smoke");
        Tasks.await(db.collection("events").document("smoke").set(ev));
        assertTrue(Tasks.await(db.collection("events").document("smoke").get()).exists());
    }
}

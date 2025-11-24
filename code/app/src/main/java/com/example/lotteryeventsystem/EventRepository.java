package com.example.lotteryeventsystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class EventRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();


    /**
     * Adds an Event object to the Firestore "events" collection
     * @param event the event to add to the database
     */
    public void addEvent(Event event) {
        db.collection("events")
                .document(event.getEventID())
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Event Repository", "Event added successfully: " + event.getEventID());
                });
    }

    /**
     * Adds an entrants ID + status to an events waitlist
     * @param eventID event the entrant registered for
     * @param entrantID the entrant
     */
    public void addEntrantToEventWaitlist(String eventID, String entrantID) {
        var waitlistEntry = new java.util.HashMap<String, Object>();
        waitlistEntry.put("entrant", entrantID);
        waitlistEntry.put("status", "waiting"); // Default value when initially joining waitlist

        db.collection("events")
                .document(eventID)
                .collection("waitlist")
                .add(waitlistEntry);
    }

    /**
     * Generates a new eventID by counting the number of events and adding 1 to the end
     * @return The new event_ID string
     */
    public Task<AggregateQuerySnapshot> generateEventID() {
        return db.collection("events")
                .count()
                .get(AggregateSource.SERVER);
    }

    /**
     * Retrieves the list of events created by the given organizer
     * @param organizerID Android device ID of the organizer
     */
    public Task<QuerySnapshot> getEventsByOrganizer(String organizerID) {
        return db.collection("events")
                .whereEqualTo("organizerID", organizerID)
                .get();
    }

    /**
     * Retreieves the list of events that are of the given
     * @param category the category to search for
     * @return - unimplemented
     */
    public Task<QuerySnapshot> getEventsByCategory(String category) {
        return null;
    }


    /**
     * Retrieves the waitlist for the given event
     * @param eventID the event to get retrieve waitlist from
     * @return - the waitlist of the given event
     */
    public Task<QuerySnapshot> getEntrants(String eventID) {
        return db.collection("events")
                .document(eventID)
                .collection("waitlist")
                .get();
    }

    /**
     * Uploads an image to firebase and calls a callback function to save that URL to the event
     * @param uri Image URI to upload to firebase
     * @param eventID eventID of the event linked to the poster
     * @param callback Callback function to handle the URL
     */
    public void uploadPosterToFirebase(Context context, Uri uri, String eventID, OrganizerEventCreateFragment.ImageUploadCallback callback) {
        if (uri == null) return;

        StorageReference ref = storage.getReference()
                .child("event_poster/" + eventID + ".jpg"
        );

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String downloadUrl = downloadUri.toString();
                                callback.onUploaded(downloadUrl);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("EventRepository", "Failed to get download URL", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Failed to upload poster", e);
                    Toast.makeText(context, "Failed to upload poster: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public void uploadQRCodeToFirebase(Bitmap qrBitmap, String eventID, OrganizerEventCreateFragment.QRCodeUploadCallback callback) {
        if (qrBitmap == null) return;

        StorageReference ref = storage.getReference()
                .child("qrcodes/" + eventID + ".jpg");


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        byte[] data = bytes.toByteArray();

        ref.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            callback.onQRCodeUploaded(uri.toString());
                        }));

    }

    /**
     * Get event by ID from Firestore
     * @author Emily Lan
     */
    public Task<DocumentSnapshot> getEventById(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get();
    }

}

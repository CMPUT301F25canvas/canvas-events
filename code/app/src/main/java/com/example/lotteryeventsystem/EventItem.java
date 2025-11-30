package com.example.lotteryeventsystem;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventItem {

    public String id;
    public String name;
    public String description;
    public String dateHighlight;
    public String dateRange;
    public ArrayList<String> category;
    public Double latitude;
    public Double longitude;
    public String posterUrl;
    public String location;

    // Main constructor
    public EventItem(String id,
                     String name,
                     String description,
                     String dateHighlight,
                     String dateRange,
                     ArrayList<String> category,
                     Double latitude,
                     Double longitude,
                     String posterUrl,
                     String location) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dateHighlight = dateHighlight;
        this.dateRange = dateRange;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.posterUrl = posterUrl;
        this.location = location;
    }

    @Override
    public String toString() {
        return name;
    }

    public static EventItem queryDocumentSnapshotToEventItem(QueryDocumentSnapshot doc) {
        String name = doc.getString("name");
        String description = doc.getString("description");
        String date = doc.getString("date");
        String registrationStart = doc.getString("registrationStart");
        String registrationEnd = doc.getString("registrationEnd");
        @SuppressWarnings("unchecked")
        ArrayList<String> category = (ArrayList<String>) doc.get("category");
        Double latitude = doc.getDouble("latitude");
        Double longitude = doc.getDouble("longitude");
        String posterUrl = doc.getString("posterURL");
        String location = doc.getString("location");


        String highlight = registrationStart != null && !registrationStart.isEmpty() ? registrationStart : date;
        String range = buildRange(registrationStart, registrationEnd, date);

        return new EventItem(
                doc.getId(),
                name != null ? name : "Untitled Event",
                description != null ? description : "",
                highlight,
                range,
                category,
                latitude,
                longitude,
                posterUrl,
                location
        );
    }

    private static String buildRange(String start, String end, String fallbackDate) {
        if (start != null && end != null && !start.isEmpty() && !end.isEmpty()) {
            return formatRange(start) + " - " + formatRange(end);
        }
        if (fallbackDate != null && !fallbackDate.isEmpty()) {
            return formatRange(fallbackDate);
        }
        return "TBD";
    }

    private static String formatRange(String raw) {
        String[] patterns = {"yyyy-MM-dd", "MMM dd, yyyy", "MMM dd yyyy"};
        for (String pattern : patterns) {
            try {
                LocalDate date = LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
                return date.format(DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH));
            } catch (DateTimeParseException ignored) {
            }
        }
        return raw;
    }
}

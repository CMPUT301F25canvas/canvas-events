package com.example.lotteryeventsystem;

public class EventItem {

    public String id;
    public String name;
    public String description;
    public String dateHighlight;
    public String dateRange;
    public String category;
    public Double latitude;
    public Double longitude;

    public EventItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public EventItem(String id,
                     String name,
                     String description,
                     String dateHighlight,
                     String dateRange) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dateHighlight = dateHighlight;
        this.dateRange = dateRange;
    }

    public EventItem(String id,
                     String name,
                     String description,
                     String dateHighlight,
                     String dateRange,
                     String category,
                     Double latitude,
                     Double longitude) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dateHighlight = dateHighlight;
        this.dateRange = dateRange;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return name;
    }
}

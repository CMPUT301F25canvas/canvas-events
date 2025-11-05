package com.example.lotteryeventsystem;

public class EventItem {

    public String id;
    public String name;

    public EventItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
